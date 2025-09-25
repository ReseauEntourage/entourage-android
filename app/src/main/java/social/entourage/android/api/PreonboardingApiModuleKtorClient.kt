import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import social.entourage.android.BuildConfig
import social.entourage.android.api.model.Metadata
import social.entourage.android.api.model.SalesforceEnterprise
import social.entourage.android.api.model.SalesforceEvent
import social.entourage.android.api.model.Summary
import timber.log.Timber

object PreonboardingApiModuleKtorClient {

    // Utilise l'URL de l'environnement courant (prod ou preprod)
    private val baseUrl: String = BuildConfig.ENTOURAGE_URL

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            })
        }
        defaultRequest {
            header("Accept", "application/json")
            header("X-API-KEY", BuildConfig.API_KEY)
            header("X-PLATFORM", "Android")
            header("X-APP-VERSION", BuildConfig.VERSION_FULL_NAME)
            url(baseUrl)
        }
    }

    /**
     * Récupère la liste des entreprises depuis Salesforce.
     * @return Liste des entreprises ou une exception en cas d'erreur.
     */
    suspend fun fetchEnterprises(): List<SalesforceEnterprise> {
        return client.get("salesforce/entreprises").body()
    }

    /**
     * Récupère les événements pour une entreprise donnée.
     * @param enterpriseId ID de l'entreprise (ex: "001AP0000139LarYAE").
     * @return Liste des événements ou une exception en cas d'erreur.
     */
    suspend fun fetchEventsForEnterprise(enterpriseId: String): List<SalesforceEvent> {
        return client.get("salesforce/entreprises/$enterpriseId/outings").body()
    }

    suspend fun fetchSummaryBeforeLogin(): Metadata {
        return client.get("home/metadata").body()
    }
}
