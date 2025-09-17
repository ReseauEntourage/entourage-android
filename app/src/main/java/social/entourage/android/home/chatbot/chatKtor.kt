package social.entourage.android.home.chatbot

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

object MistralApi {
    private const val API_KEY = ""
    private const val URL = "https://api.mistral.ai/v1/chat/completions"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun ask(userMessage: String): String {
        //TODO replace URLS with resources
        val contextPrompt = """
        Voici les sections de l'application Entourage :
        
        - <b>Outings</b> : des événements conviviaux en présence réelle — <a href="https://preprod.entourage.social/app/outings">ce lien</a>
        - <b>Contribution</b> : pour aider quelqu’un matériellement ou par son temps — <a href="https://preprod.entourage.social/app/contributions">ce lien</a>
        - <b>Solicitation</b> : pour demander de l’aide — <a href="https://preprod.entourage.social/app/solicitations">ce lien</a>
        - <b>Group</b> : pour rencontrer ses voisins — <a href="https://preprod.entourage.social/app/groups">ce lien</a>
        
        Tu es un assistant virtuel dans une application Android. L'utilisateur va te poser une question, et tu dois l’orienter vers la section appropriée de l’application.
        
        Ta réponse doit être au format HTML et commencer par :
        <b>Ok, vous pourriez aller voir :</b> <a href="...">ce lien</a>
        
        Choisis le lien approprié parmi ceux-ci :
        https://preprod.entourage.social/app/outings  
        https://preprod.entourage.social/app/contributions  
        https://preprod.entourage.social/app/solicitations  
        https://preprod.entourage.social/app/groups
        
        Message utilisateur : $userMessage
        """.trimIndent()

        val response: HttpResponse = client.post(URL) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $API_KEY")
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            setBody(
                buildJsonObject {
                    put("model", "mistral-small-latest")
                    putJsonArray("messages") {
                        addJsonObject {
                            put("role", "system")
                            put("content", contextPrompt)
                        }
                    }
                }
            )
        }

        val json = response.body<JsonObject>()
        return json["choices"]?.jsonArray?.get(0)?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.content
            ?: "Désolé, je n'ai pas compris."
    }
}
