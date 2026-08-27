package social.entourage.android.sockets

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Post
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * ActionCable client for the `ConversationChannel` (see backend doc: subscribes with
 * `{"channel":"ConversationChannel","instance_type":"...","instance_id":...}`).
 * Delivers chat_message_created / chat_message_updated (content edits AND soft-deletes,
 * distinguished by `data.status == "deleted"`) / user_reaction_added / user_reaction_removed
 * events. member_joined / member_left are part of the same channel but have no UI consumer
 * yet, so they're only logged (see handleChannelMessage).
 */
object ConversationSocketManager {

    private const val TAG = "ConvSocket"

    sealed class ChatEvent {
        data class MessageCreated(val message: Post) : ChatEvent()
        data class MessageUpdated(val message: Post) : ChatEvent()
        data class ReactionAdded(val chatMessageId: Int, val reactionId: Int, val userId: Int) : ChatEvent()
        data class ReactionRemoved(val chatMessageId: Int, val reactionId: Int, val userId: Int) : ChatEvent()
        /** Subscription re-confirmed after a drop (network loss, app backgrounded, …).
         * No history is replayed by the server, so consumers should refetch via REST to
         * fill whatever gap happened while disconnected. */
        object Reconnected : ChatEvent()
    }

    private val gson: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create()

    private val _events = MutableSharedFlow<ChatEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<ChatEvent> = _events.asSharedFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var reconnectRunnable: Runnable? = null
    private var reconnectAttempts = 0
    private var wantsConnection = false

    private var webSocket: WebSocket? = null
    private var currentInstanceType: String? = null
    private var currentInstanceId: Int? = null

    // Vrai dès la première confirm_subscription reçue pour la cible courante. Une
    // confirm_subscription suivante (après coupure réseau / mise en veille) signale donc
    // une vraie reconnexion : le serveur ne rejouant aucun historique, on prévient les
    // consommateurs via ChatEvent.Reconnected pour qu'ils recomblent le trou via REST.
    private var hasConfirmedSubscriptionOnce = false

    /**
     * Maps the app's own conversation "type" discriminator (`detailConversation.type`,
     * raw values "private" / "neighborhood" / "outing" / "small_talk") to the
     * ActionCable `instance_type`. These must match the backend's Rails class names
     * exactly (see doc §05 — "Entourage" / all-caps / plural forms are all rejected):
     * Outing, Conversation, Solicitation, Contribution, Neighborhood, Smalltalk.
     */
    fun mapConversationTypeToInstanceType(rawType: String?): String {
        val resolved = when (rawType) {
            "private" -> "Conversation"
            "neighborhood" -> "Neighborhood"
            "small_talk" -> "Smalltalk"
            "outing" -> "Outing"
            null -> "Neighborhood"
            else -> {
                Timber.tag(TAG).w("Unrecognized conversation type '%s', defaulting instance_type to Neighborhood", rawType)
                "Neighborhood"
            }
        }
        Timber.tag(TAG).d("mapConversationTypeToInstanceType('%s') -> '%s'", rawType, resolved)
        return resolved
    }

    private fun buildWsUrl(): String? {
        val token = EntourageApplication.get().authenticationController.me?.token
        if (token.isNullOrBlank()) {
            Timber.tag(TAG).w("buildWsUrl(): no token on authenticationController.me, aborting (me=%s)", EntourageApplication.get().authenticationController.me)
            return null
        }
        val httpBase = BuildConfig.ENTOURAGE_URL // e.g. https://api-preprod.entourage.social/api/v1/
        val httpHost = httpBase.substringBefore("/api/") // e.g. https://api-preprod.entourage.social
        Timber.tag(TAG).d("buildWsUrl(): ENTOURAGE_URL=%s -> host=%s (token length=%d)", httpBase, httpHost, token.length)

        // Build with HttpUrl (http/https only — it doesn't understand ws/wss schemes)
        // so the token gets properly percent-encoded, matching what
        // AuthenticationInterceptor does for every REST call via addQueryParameter.
        // A raw-concatenated token containing '+', '/', '=' could otherwise be sent
        // as something subtly different from the real value.
        val encodedHttpUrl = "$httpHost/cable".toHttpUrl()
            .newBuilder()
            .addQueryParameter("token", token)
            .build()
            .toString()

        // Swap the scheme back to ws(s):// for the actual WebSocket request.
        val wsUrl = encodedHttpUrl
            .replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://")
        Timber.tag(TAG).d("buildWsUrl(): resolved wsUrl=%s", wsUrl.substringBefore("?token=") + "?token=<redacted>")
        return wsUrl
    }

    fun connect(instanceType: String, instanceId: Int) {
        Timber.tag(TAG).d(
            "connect() called for instance_type=%s instance_id=%s (already connected=%s, current=%s/%s)",
            instanceType, instanceId, webSocket != null, currentInstanceType, currentInstanceId
        )
        wantsConnection = true
        if (webSocket != null && currentInstanceType == instanceType && currentInstanceId == instanceId) {
            Timber.tag(TAG).d("connect(): already connected to this exact target, no-op")
            return
        }
        closeCurrentSocket()
        currentInstanceType = instanceType
        currentInstanceId = instanceId
        reconnectAttempts = 0
        hasConfirmedSubscriptionOnce = false
        openSocket()
    }

    fun disconnect() {
        Timber.tag(TAG).d("disconnect() called (was connected to %s/%s)", currentInstanceType, currentInstanceId)
        wantsConnection = false
        cancelReconnect()
        closeCurrentSocket()
        currentInstanceType = null
        currentInstanceId = null
    }

    private fun closeCurrentSocket() {
        webSocket?.close(1000, "bye")
        webSocket = null
    }

    private fun cancelReconnect() {
        reconnectRunnable?.let { mainHandler.removeCallbacks(it) }
        reconnectRunnable = null
        reconnectAttempts = 0
    }

    private fun scheduleReconnect() {
        if (!wantsConnection) return
        cancelReconnectKeepAttempts()
        reconnectAttempts++
        val delayMs = minOf(30_000L, 2_000L * reconnectAttempts)
        Timber.tag(TAG).d("scheduleReconnect(): attempt #%d in %dms", reconnectAttempts, delayMs)
        val runnable = Runnable { if (wantsConnection) openSocket() }
        reconnectRunnable = runnable
        mainHandler.postDelayed(runnable, delayMs)
    }

    private fun cancelReconnectKeepAttempts() {
        reconnectRunnable?.let { mainHandler.removeCallbacks(it) }
        reconnectRunnable = null
    }

    /**
     * Dedicated, interceptor-free client. Deliberately NOT derived from
     * `ApiModule.okHttpClient`: that client carries `HttpLoggingInterceptor` at BODY
     * level in debug builds, which is known to hang/break on WebSocket upgrades because
     * it tries to read the response body after the socket has already been handed off
     * to WebSocket framing. Auth is handled ourselves via the `?token=` query param, so
     * none of the REST interceptors (auth/HMAC/error-bus) are needed here anyway.
     */
    private val webSocketClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .pingInterval(20, TimeUnit.SECONDS)
            .build()
    }

    private fun openSocket() {
        val url = buildWsUrl()
        if (url == null) {
            Timber.tag(TAG).w("No auth token available, cannot open websocket")
            return
        }
        val originHeader = "https://${BuildConfig.DEEP_LINKS_URL}"
        Timber.tag(TAG).d(
            "opening socket: %s (Origin=%s, target instance_type=%s instance_id=%s, reconnectAttempts=%d)",
            url.substringBefore("?token=") + "?token=<redacted>", originHeader, currentInstanceType, currentInstanceId, reconnectAttempts
        )
        try {
            // ActionCable rejects the upgrade with a 404 when there's no Origin header
            // (confirmed live) — OkHttp doesn't send one by default for a native client,
            // unlike a browser. Reuse the app's own public domain, already whitelisted
            // backend-side and already used elsewhere in the app (mention links).
            val request = Request.Builder()
                .url(url)
                .header("Origin", originHeader)
                .build()
            webSocket = webSocketClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Timber.tag(TAG).d(
                        "socket OPEN (HTTP %s %s), headers=%s",
                        response.code, response.message, response.headers
                    )
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Timber.tag(TAG).d("<< frame received: %s", text)
                    handleMessage(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Timber.tag(TAG).d("socket CLOSING: code=%d reason=%s", code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Timber.tag(TAG).d("socket CLOSED: code=%d reason=%s (wantsConnection=%s)", code, reason, wantsConnection)
                    if (wantsConnection) scheduleReconnect()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    // response is non-null when the failure happens during/after the HTTP
                    // handshake (e.g. a plain 404/401 instead of a 101 Switching Protocols) —
                    // that response's body is where ActionCable/nginx put the real reason.
                    val bodySnippet = try {
                        response?.peekBody(2048)?.string()
                    } catch (bodyReadError: Exception) {
                        "unavailable: ${bodyReadError.message}"
                    }
                    Timber.tag(TAG).e(
                        t,
                        "socket FAILURE: %s (HTTP %s %s), body=%s, headers=%s",
                        t.javaClass.simpleName + ": " + t.message,
                        response?.code, response?.message, bodySnippet, response?.headers
                    )
                    if (wantsConnection) scheduleReconnect()
                }
            })
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to open websocket for url=%s", url.substringBefore("?token="))
        }
    }

    private fun sendSubscribe() {
        val type = currentInstanceType
        val id = currentInstanceId
        if (type == null || id == null) {
            Timber.tag(TAG).w("sendSubscribe(): no current target (type=%s id=%s), skipping", type, id)
            return
        }
        val identifier = """{"channel":"ConversationChannel","instance_type":"$type","instance_id":$id}"""
        val payload = JsonObject().apply {
            addProperty("command", "subscribe")
            addProperty("identifier", identifier)
        }
        val frame = gson.toJson(payload)
        val sent = webSocket?.send(frame)
        Timber.tag(TAG).d(">> subscribe sent (queued=%s): %s", sent, frame)
    }

    private fun handleMessage(text: String) {
        try {
            val root = JsonParser.parseString(text).asJsonObject
            val topLevelType = root.get("type")?.takeIf { it.isJsonPrimitive }?.asString
            when (topLevelType) {
                "welcome" -> {
                    reconnectAttempts = 0
                    sendSubscribe()
                }
                "ping" -> Unit
                "confirm_subscription" -> {
                    Timber.tag(TAG).d("subscription confirmed")
                    if (hasConfirmedSubscriptionOnce) {
                        _events.tryEmit(ChatEvent.Reconnected)
                    } else {
                        hasConfirmedSubscriptionOnce = true
                    }
                }
                // Pas de raison détaillée sur un rejet (type inconnu, id introuvable, ou
                // simplement pas membre) : à traiter comme "accès refusé", pas comme une
                // erreur réseau — donc pas de scheduleReconnect() ici.
                "reject_subscription" -> Timber.tag(TAG).w("subscription REJECTED for %s/%s: %s", currentInstanceType, currentInstanceId, text)
                "disconnect" -> Timber.tag(TAG).w("server requested disconnect: %s", text)
                else -> {
                    val messageObj = root.getAsJsonObject("message")
                    if (messageObj == null) {
                        Timber.tag(TAG).w("frame has no top-level 'type' and no 'message' key, ignoring: %s", text)
                        return
                    }
                    handleChannelMessage(messageObj, text)
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to parse socket message: %s", text)
        }
    }

    private fun handleChannelMessage(messageObj: JsonObject, rawText: String) {
        val eventType = messageObj.get("type")?.asString
        if (eventType == null) {
            Timber.tag(TAG).w("channel message has no 'type', ignoring: %s", rawText)
            return
        }
        Timber.tag(TAG).d("channel event received: type=%s", eventType)
        val dataObj = messageObj.getAsJsonObject("data")
        when (eventType) {
            "chat_message_created" -> {
                val post = dataObj?.let { gson.fromJson(it, Post::class.java) }
                if (post == null) {
                    Timber.tag(TAG).w("chat_message_created with no/unparseable data: %s", rawText)
                    return
                }
                Timber.tag(TAG).d("emitting MessageCreated id=%s content=%s", post.id, post.content)
                _events.tryEmit(ChatEvent.MessageCreated(post))
            }
            "chat_message_updated" -> {
                // Deletion is represented as an update where data.status == "deleted".
                val post = dataObj?.let { gson.fromJson(it, Post::class.java) }
                if (post == null) {
                    Timber.tag(TAG).w("chat_message_updated with no/unparseable data: %s", rawText)
                    return
                }
                Timber.tag(TAG).d("emitting MessageUpdated id=%s status=%s", post.id, post.status)
                _events.tryEmit(ChatEvent.MessageUpdated(post))
            }
            "user_reaction_added", "user_reaction_removed" -> {
                val actingUserId = messageObj.get("user_id")?.takeIf { it.isJsonPrimitive }?.asInt
                val currentUserId = EntourageApplication.get().me()?.id
                if (actingUserId != null && currentUserId != null && actingUserId == currentUserId) {
                    Timber.tag(TAG).d("%s: echo of our own action (user_id=%d), ignoring", eventType, actingUserId)
                    return
                }
                val chatMessageId = dataObj?.get("chat_message_id")?.takeIf { it.isJsonPrimitive }?.asInt
                    ?: dataObj?.getAsJsonObject("chat_message")?.get("id")?.takeIf { it.isJsonPrimitive }?.asInt
                val reactionId = dataObj?.get("reaction_id")?.takeIf { it.isJsonPrimitive }?.asInt
                if (chatMessageId != null && reactionId != null) {
                    val event = if (eventType == "user_reaction_added") {
                        ChatEvent.ReactionAdded(chatMessageId, reactionId, actingUserId ?: 0)
                    } else {
                        ChatEvent.ReactionRemoved(chatMessageId, reactionId, actingUserId ?: 0)
                    }
                    Timber.tag(TAG).d("emitting %s", event)
                    _events.tryEmit(event)
                } else {
                    Timber.tag(TAG).w("Unrecognized %s payload: %s", eventType, rawText)
                }
            }
            // Diffusés quand une demande de participation devient/quitte "accepted".
            // Pas de surface UI qui affiche une liste de membres en direct aujourd'hui
            // (MembersConversationFragment recharge via REST) — juste tracés pour l'instant.
            "member_joined", "member_left" -> Timber.tag(TAG).d("%s: %s", eventType, rawText)
            else -> Timber.tag(TAG).d("Unhandled channel event type=%s, raw=%s", eventType, rawText)
        }
    }
}
