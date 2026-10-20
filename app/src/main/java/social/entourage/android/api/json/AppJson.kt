package social.entourage.android.api.json

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Configuration kotlinx.serialization commune à toute l'app (Retrofit, persistance locale,
 * sockets, notifications). Elle reproduit la tolérance historique de Gson :
 * - clés inconnues ignorées ;
 * - nombres/booléens entre guillemets acceptés (`isLenient`) ;
 * - `null` ou enum inconnue sur une propriété qui a une valeur par défaut -> valeur par défaut ;
 * - valeurs par défaut écrites dans le JSON envoyé (Gson écrivait tous les champs non nuls) ;
 * - propriétés nulles omises du JSON envoyé.
 */
object AppJson {
    const val CONTENT_TYPE = "application/json; charset=UTF-8"

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
        explicitNulls = false
    }

    inline fun <reified T> decode(text: String): T = json.decodeFromString(text)

    inline fun <reified T> encode(value: T): String = json.encodeToString(value)
}
