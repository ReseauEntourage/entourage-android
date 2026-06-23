package social.entourage.android.api

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import social.entourage.android.BuildConfig
import social.entourage.android.api.model.Post
import timber.log.Timber

class ConversationWebSocketClient(
    private val okHttpClient: OkHttpClient,
    private val instanceType: String,
    private val instanceId: Int,
    private val token: String,
    private val onMessageCreated: (Post) -> Unit,
    private val onMessageUpdated: (Post) -> Unit,
    private val onConnected: () -> Unit,
    private val onDisconnected: () -> Unit
) {
    private var webSocket: WebSocket? = null
    private val gson = Gson()
    private val identifier = JSONObject().apply {
        put("channel", "ConversationChannel")
        put("instance_type", instanceType)
        put("instance_id", instanceId)
    }.toString()

    var isConnected = false
        private set

    fun connect() {
        if (webSocket != null) return
        val url = buildWsUrl(BuildConfig.ENTOURAGE_URL, token)
        val request = Request.Builder().url(url).build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                ws.send(JSONObject().apply {
                    put("command", "subscribe")
                    put("identifier", identifier)
                }.toString())
            }

            override fun onMessage(ws: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                isConnected = false
                webSocket = null
                onDisconnected()
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Timber.w(t, "WebSocket failure for $instanceType:$instanceId")
                isConnected = false
                webSocket = null
                onDisconnected()
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, null)
        webSocket = null
        isConnected = false
    }

    private fun handleMessage(text: String) {
        try {
            val json = JSONObject(text)
            when (json.optString("type")) {
                "confirm_subscription" -> {
                    isConnected = true
                    onConnected()
                }
                "ping", "welcome" -> {}
                else -> {
                    val message = json.optJSONObject("message") ?: return
                    val data = message.optJSONObject("data") ?: return
                    val post = gson.fromJson(data.toString(), Post::class.java) ?: return
                    when (message.optString("type")) {
                        "chat_message_created" -> onMessageCreated(post)
                        "chat_message_updated" -> onMessageUpdated(post)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "WebSocket message parse error")
        }
    }

    companion object {
        fun buildWsUrl(entourageUrl: String, token: String): String {
            val base = entourageUrl
                .replace("https://", "wss://")
                .replace("http://", "ws://")
            val apiIdx = base.indexOf("/api/")
            val root = if (apiIdx != -1) base.substring(0, apiIdx) else base.trimEnd('/')
            return "$root/cable?token=$token"
        }

        fun instanceTypeFrom(conversationType: String?): String? = when (conversationType) {
            "neighborhood" -> "Neighborhood"
            "outing" -> "Entourage"
            else -> null
        }
    }
}
