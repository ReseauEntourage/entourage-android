package social.entourage.android.api

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import social.entourage.android.BuildConfig
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class HmacInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        if (original.method != "POST" || !original.url.encodedPath.endsWith("/users")) {
            return chain.proceed(original)
        }

        val secret = BuildConfig.HMAC_SECRET
        if (secret.isBlank()) {
            return chain.proceed(original)
        }

        val timestamp = System.currentTimeMillis() / 1000L

        val buffer = Buffer()
        original.body?.writeTo(buffer)
        val bodyBytes = buffer.readByteArray()
        val phone = extractPhone(bodyBytes)

        val message = "POST\n/api/v1/users\n$timestamp\n$phone"
        val signature = sign(secret, message)

        val newBody = bodyBytes.toRequestBody(original.body?.contentType())
        val signed = original.newBuilder()
            .method(original.method, newBody)
            .header("X-Request-Timestamp", timestamp.toString())
            .header("X-Request-Signature", signature)
            .build()

        return chain.proceed(signed)
    }

    private fun extractPhone(bodyBytes: ByteArray): String {
        return try {
            JSONObject(String(bodyBytes, Charsets.UTF_8))
                .optJSONObject("user")
                ?.optString("phone", "") ?: ""
        } catch (e: JSONException) {
            ""
        }
    }

    private fun sign(secret: String, message: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return Base64.encodeToString(mac.doFinal(message.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    }
}
