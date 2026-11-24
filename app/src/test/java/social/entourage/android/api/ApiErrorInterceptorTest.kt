package social.entourage.android.api

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import social.entourage.android.BuildConfig

/**
 * Écran d'erreur global (`ApiErrorBus` → `ApiErrorBottomSheet`) : une erreur HTTP l'ouvre, sauf pour les
 * routes `next_steps` (EN-9473), dont l'erreur doit rester invisible pour l'utilisateur.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ApiErrorInterceptorTest {

    private val base = BuildConfig.ENTOURAGE_URL

    /** Appelle [url] avec l'intercepteur, le serveur répondant [code] ; renvoie les erreurs émises sur le bus. */
    private fun emittedBy(method: String, url: String, code: Int): List<HttpApiError> {
        val emitted = mutableListOf<HttpApiError>()
        runTest {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { ApiErrorBus.errors.toList(emitted) }
            val client = OkHttpClient.Builder()
                .addInterceptor(ApiErrorInterceptor)
                .addInterceptor(Interceptor { chain -> respond(chain.request(), code) })
                .build()
            val body = if (method == "POST") "{}".toRequestBody() else null
            val response = client.newCall(Request.Builder().url(url).method(method, body).build()).execute()
            assertEquals("la réponse n'est jamais altérée", code, response.code)
            response.close()
        }
        return emitted
    }

    private fun respond(request: Request, code: Int): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message("test")
        .body("".toResponseBody())
        .build()

    @Test
    fun errorOnAnotherRouteStillOpensTheGlobalErrorScreen() {
        assertEquals(listOf(HttpApiError(500)), emittedBy("GET", "${base}home/summary", 500))
        assertEquals(listOf(HttpApiError(404)), emittedBy("GET", "${base}groups/1", 404))
        assertEquals(listOf(HttpApiError(400)), emittedBy("POST", "${base}users", 400))
    }

    @Test
    fun errorOnTheLegacySuggestionsRouteStillOpensTheGlobalErrorScreen() {
        assertEquals(listOf(HttpApiError(500)), emittedBy("GET", "${base}suggestions", 500))
    }

    @Test
    fun errorOnNextStepsRoutesIsSilent() {
        assertEquals(emptyList<HttpApiError>(), emittedBy("GET", "${base}next_steps/current", 500))
        assertEquals(emptyList<HttpApiError>(), emittedBy("GET", "${base}next_steps/current", 404))
        assertEquals(emptyList<HttpApiError>(), emittedBy("GET", "${base}next_steps/others", 503))
        assertEquals(emptyList<HttpApiError>(), emittedBy("POST", "${base}next_steps/12/ignore", 404))
        assertEquals(emptyList<HttpApiError>(), emittedBy("POST", "${base}next_steps/interactions", 404))
        assertEquals(emptyList<HttpApiError>(), emittedBy("POST", "${base}next_steps/interactions", 400))
    }

    @Test
    fun aQueryStringDoesNotChangeTheRouteExemption() {
        assertEquals(emptyList<HttpApiError>(), emittedBy("GET", "${base}next_steps/others?limit=3", 500))
    }

    @Test
    fun aRouteThatOnlyStartsLikeNextStepsIsNotExempt() {
        assertEquals(listOf(HttpApiError(500)), emittedBy("GET", "${base}next_steps_history", 500))
    }

    @Test
    fun unauthorizedStaysHandledByTheAuthenticationInterceptor() {
        assertEquals(emptyList<HttpApiError>(), emittedBy("GET", "${base}home/summary", 401))
        assertEquals(emptyList<HttpApiError>(), emittedBy("GET", "${base}next_steps/current", 401))
    }

    @Test
    fun successfulResponsesEmitNothing() {
        assertEquals(emptyList<HttpApiError>(), emittedBy("GET", "${base}home/summary", 200))
        assertEquals(emptyList<HttpApiError>(), emittedBy("POST", "${base}next_steps/interactions", 204))
    }

    @Test
    fun hostsOutsideTheEntourageApiAreNeverReported() {
        assertEquals(emptyList<HttpApiError>(), emittedBy("GET", "https://example.org/home/summary", 500))
    }
}
