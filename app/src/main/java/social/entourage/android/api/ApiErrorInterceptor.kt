package social.entourage.android.api

import okhttp3.Interceptor
import okhttp3.Response
import social.entourage.android.BuildConfig
import java.io.IOException

object ApiErrorInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!request.url.toString().startsWith(BuildConfig.ENTOURAGE_URL)) {
            return chain.proceed(request)
        }
        val response = chain.proceed(request)
        val code = response.code
        // 401 is handled by AuthenticationInterceptor (redirects to login)
        if (code != 401 && code in 400..599) {
            ApiErrorBus.emit(HttpApiError(code))
        }
        return response
    }
}
