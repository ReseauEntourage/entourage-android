package social.entourage.android.api

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import social.entourage.android.BuildConfig
import timber.log.Timber
import java.io.IOException

class CurlLoggingInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        logCurlCommand(request)
        return chain.proceed(request)
    }

    private fun logCurlCommand(request: Request) {
        val curlCmd = StringBuilder()

        // Ajoute la méthode HTTP (GET, POST, etc.)
        curlCmd.append("curl -X ").append(request.method).append(" ")

        // Ajoute les headers
        for (header in request.headers) {
            curlCmd.append("-H \"").append(header.first).append(": ").append(header.second).append("\" ")
        }

        // Ajoute le body si présent
        request.body?.let { body ->
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            curlCmd.append("-d '").append(buffer.readUtf8()).append("' ")
        }

        // Ajoute l'URL
        curlCmd.append("\"").append(request.url).append("\"")

        if (BuildConfig.DEBUG) {
            Timber.wtf("Generated cURL: $curlCmd")
        }
    }
}