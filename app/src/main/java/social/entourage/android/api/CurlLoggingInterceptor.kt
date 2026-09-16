package social.entourage.android.api

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
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

        // Ajoute le body si présent (pour POST, PUT, etc.)
        request.body?.let { body ->
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            val bodyContent = buffer.readUtf8()
            
            // Ajoute le header Content-Type: application/json si ce n'est pas déjà fait
            val hasContentType = request.headers.any { it.first.equals("Content-Type", ignoreCase = true) }
            if (!hasContentType) {
                curlCmd.append("-H \"Content-Type: application/json\" ")
            }
            
            // Échappe les caractères spéciaux dans le body pour éviter les problèmes
            val escapedBody = escapeForShell(bodyContent)
            curlCmd.append("-d \"").append(escapedBody).append("\" ")
        }

        // Ajoute l'URL à la fin
        curlCmd.append(request.url.toString())

        if (BuildConfig.DEBUG) {
            Timber.d("Generated cURL: $curlCmd")
        }
    }

    private fun escapeForShell(input: String): String {
        val backslash = '\\'
        return input.map { char ->
            when (char) {
                '"' -> backslash.toString() + '"'
                '$' -> backslash.toString() + '$'
                '`' -> backslash.toString() + '`'
                else -> char
            }
        }.joinToString("")
    }
}