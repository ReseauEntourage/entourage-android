package social.entourage.android.chatbot.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

object MistralApi {
    private const val API_KEY = ""
    private const val URL = "https://api.mistral.ai/v1/chat/completions"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }


    suspend fun ask(userMessage: String): String {
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
