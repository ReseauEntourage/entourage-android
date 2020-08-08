package social.entourage.android.authentication

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.api.tape.Events.OnUnauthorizedEvent
import social.entourage.android.tools.BusProvider.instance
import java.io.IOException

/**
 * Retrofit interceptor that automatically add a params to the url when authenticated
 */
object AuthenticationInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (!request.url().toString().startsWith(BuildConfig.ENTOURAGE_URL)) {
            return chain.proceed(request)
        }
        val authenticationController: AuthenticationController? = EntourageApplication.get().entourageComponent.authenticationController
        val url: HttpUrl = if (authenticationController?.isAuthenticated == true) {
            request.url().newBuilder().addQueryParameter("token", authenticationController.me?.token).build()
        } else {
            request.url().newBuilder().build()
        }
        request = request.newBuilder()
                .header("Accept", "application/json")
                .header("X-API-KEY", BuildConfig.API_KEY)
                .header("X-PLATFORM", "Android")
                .header("X-APP-VERSION", BuildConfig.VERSION_FULL_NAME)
                .url(url).build()
        val response = chain.proceed(request)
        if (response.code() == 401) {
            if (response.message().equals("Unauthorized", ignoreCase = true)) {
                instance.post(OnUnauthorizedEvent())
            }
        }
        return response
    }
}