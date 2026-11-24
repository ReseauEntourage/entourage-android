package social.entourage.android.api

import okhttp3.Interceptor
import okhttp3.Response
import social.entourage.android.BuildConfig
import java.io.IOException

object ApiErrorInterceptor : Interceptor {

    /**
     * Routes dont une erreur HTTP n'ouvre jamais l'écran d'erreur global : l'appelant l'absorbe lui-même.
     * « Votre prochain pas » (EN-9473) : une erreur du moteur de suggestion reste invisible
     * (`HomeSuggestionViewModel` garde le dernier contenu, l'envoi des gestes ignore toute erreur).
     * Chemins relatifs à `BuildConfig.ENTOURAGE_URL` : la route elle-même et tout ce qu'elle contient.
     */
    private val SILENT_ROUTES = listOf("next_steps")

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        if (!url.startsWith(BuildConfig.ENTOURAGE_URL)) {
            return chain.proceed(request)
        }
        val response = chain.proceed(request)
        val code = response.code
        // 401 is handled by AuthenticationInterceptor (redirects to login)
        if (code != 401 && code in 400..599 && !isSilentRoute(url.removePrefix(BuildConfig.ENTOURAGE_URL))) {
            ApiErrorBus.emit(HttpApiError(code))
        }
        return response
    }

    /** [relativePath] : chemin sous l'URL de base, éventuellement suivi d'une query (`next_steps/current?x=1`). */
    private fun isSilentRoute(relativePath: String): Boolean {
        val path = relativePath.substringBefore('?').substringBefore('#')
        return SILENT_ROUTES.any { path == it || path.startsWith("$it/") }
    }
}
