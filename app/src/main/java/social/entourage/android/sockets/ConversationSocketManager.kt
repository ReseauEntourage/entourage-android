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
 * distinguished by `data.status == "deleted"`) / user_reaction_created events.
 */
object ConversationSocketManager {

    private const val TAG = "ConvSocket"

    sealed class ChatEvent {
        data class MessageCreated(val message: Post) : ChatEvent()
        data class MessageUpdated(val message: Post) : ChatEvent()
        data class ReactionCreated(val chatMessageId: Int, val reactionId: Int, val userId: Int) : ChatEvent()
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

    /**
     * Maps the app's own conversation "type" discriminator (`detailConversation.type`,
     * raw values "private" / "neighborhood" / "outing" / "small_talk") to the
     * ActionCable `instance_type`. "private" is still being tuned — "CONVERSATIONS"
     * (all caps, plural) and "Conversations" (title case, plural) both got rejected,
     * trying "Conversation" (title case, singular) next.
     */
    fun mapConversationTypeToInstanceType(rawType: String?): String {
        return when (rawType) {
            "private" -> "Conversation"
            "neighborhood" -> "NEIGHBORHOODS"
            "small_talk" -> "SMALLTALK"
            "outing" -> "OUTING"
            null -> "NEIGHBORHOODS"
            else -> {
                Timber.tag(TAG).w("Unrecognized conversation type '%s', defaulting instance_type to NEIGHBORHOODS", rawType)
                "NEIGHBORHOODS"
            }
        }
    }

    private fun buildWsUrl(): String? {
        val token = EntourageApplication.get().authenticationController.me?.token
        if (token.isNullOrBlank()) return null
        Timber.tag(TAG).d("using token (TEMP full log for wscat comparison, remove after debugging): %s", token)
        val httpBase = BuildConfig.ENTOURAGE_URL // e.g. https://api-preprod.entourage.social/api/v1/
        val httpHost = httpBase.substringBefore("/api/") // e.g. https://api-preprod.entourage.social

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
        return encodedHttpUrl
            .replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://")
    }

    fun connect(instanceType: String, instanceId: Int) {
        Timber.tag(TAG).d("connect() called for instance_type=%s instance_id=%s (already connected=%s)", instanceType, instanceId, webSocket != null)
        wantsConnection = true
        if (webSocket != null && currentInstanceType == instanceType && currentInstanceId == instanceId) return
        closeCurrentSocket()
        currentInstanceType = instanceType
        currentInstanceId = instanceId
        reconnectAttempts = 0
        openSocket()
    }

    fun disconnect() {
        Timber.tag(TAG).d("disconnect() called")
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
        Timber.tag(TAG).d("opening socket: %s", url.substringBefore("?token=") + "?token=<redacted>")
        try {
            // ActionCable rejects the upgrade with a 404 when there's no Origin header
            // (confirmed live) — OkHttp doesn't send one by default for a native client,
            // unlike a browser. Reuse the app's own public domain, already whitelisted
            // backend-side and already used elsewhere in the app (mention links).
            val request = Request.Builder()
                .url(url)
                .header("Origin", "https://${BuildConfig.DEEP_LINKS_URL}")
                .build()
            webSocket = webSocketClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Timber.tag(TAG).d("socket open (HTTP %s)", response.code)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleMessage(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Timber.tag(TAG).d("socket closing: %s", reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Timber.tag(TAG).d("socket closed: %s", reason)
                    if (wantsConnection) scheduleReconnect()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Timber.tag(TAG).w(t, "socket failure (HTTP %s)", response?.code)
                    if (wantsConnection) scheduleReconnect()
                }
            })
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to open websocket for url=%s", url.substringBefore("?token="))
        }
    }

    private fun sendSubscribe() {
        val type = currentInstanceType ?: return
        val id = currentInstanceId ?: return
        val identifier = """{"channel":"ConversationChannel","instance_type":"$type","instance_id":$id}"""
        val payload = JsonObject().apply {
            addProperty("command", "subscribe")
            addProperty("identifier", identifier)
        }
        webSocket?.send(gson.toJson(payload))
        Timber.tag(TAG).d("subscribe sent: instance_type=%s instance_id=%s", type, id)
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
                "confirm_subscription" -> Timber.tag(TAG).d("subscription confirmed")
                "reject_subscription" -> Timber.tag(TAG).w("subscription rejected for %s/%s", currentInstanceType, currentInstanceId)
                "disconnect" -> Timber.tag(TAG).d("server requested disconnect")
                else -> {
                    val messageObj = root.getAsJsonObject("message") ?: return
                    handleChannelMessage(messageObj, text)
                }
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to parse socket message")
        }
    }

    private fun handleChannelMessage(messageObj: JsonObject, rawText: String) {
        val eventType = messageObj.get("type")?.asString ?: return
        val dataObj = messageObj.getAsJsonObject("data")
        when (eventType) {
            "chat_message_created" -> {
                val post = dataObj?.let { gson.fromJson(it, Post::class.java) } ?: return
                _events.tryEmit(ChatEvent.MessageCreated(post))
            }
            "chat_message_updated" -> {
                // Deletion is represented as an update where data.status == "deleted".
                val post = dataObj?.let { gson.fromJson(it, Post::class.java) } ?: return
                _events.tryEmit(ChatEvent.MessageUpdated(post))
            }
            "user_reaction_created" -> {
                val actingUserId = messageObj.get("user_id")?.takeIf { it.isJsonPrimitive }?.asInt
                val currentUserId = EntourageApplication.get().me()?.id
                if (actingUserId != null && currentUserId != null && actingUserId == currentUserId) {
                    // Already applied optimistically on our own side, ignore the echo.
                    return
                }
                val chatMessageId = dataObj?.get("chat_message_id")?.takeIf { it.isJsonPrimitive }?.asInt
                    ?: dataObj?.getAsJsonObject("chat_message")?.get("id")?.takeIf { it.isJsonPrimitive }?.asInt
                val reactionId = dataObj?.get("reaction_id")?.takeIf { it.isJsonPrimitive }?.asInt
                if (chatMessageId != null && reactionId != null) {
                    _events.tryEmit(ChatEvent.ReactionCreated(chatMessageId, reactionId, actingUserId ?: 0))
                } else {
                    Timber.tag(TAG).w("Unrecognized user_reaction_created payload: %s", rawText)
                }
            }
            else -> Timber.tag(TAG).d("Unhandled channel event type=%s", eventType)
        }
    }
}
