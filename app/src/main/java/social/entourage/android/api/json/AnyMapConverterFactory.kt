package social.entourage.android.api.json

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Corps de requête `@Body ArrayMap<String, Any>` / `Map<String, Any?>` : kotlinx.serialization ne sait
 * pas sérialiser `Any`, donc ces corps sont convertis valeur par valeur en [JsonElement].
 *
 * Valeurs acceptées : null (clé omise, comme Gson), String, Number, Boolean, Map, Iterable/Array et tout
 * objet `@Serializable`. Étape transitoire : à terme chaque corps devrait devenir un DTO `@Serializable`.
 */
class AnyMapConverterFactory : Converter.Factory() {

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        val raw = when (type) {
            is Class<*> -> type
            is ParameterizedType -> type.rawType as? Class<*>
            else -> null
        } ?: return null
        if (!Map::class.java.isAssignableFrom(raw)) return null
        return Converter<Map<*, *>, RequestBody> { map ->
            AppJson.json.encodeToString(JsonElement.serializer(), toJsonElement(map))
                .toRequestBody(AppJson.CONTENT_TYPE.toMediaType())
        }
    }

    companion object {
        fun toJsonElement(value: Any?): JsonElement = when (value) {
            null -> JsonNull
            is JsonElement -> value
            is String -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Char -> JsonPrimitive(value.toString())
            is Map<*, *> -> {
                // Copie clé/valeur à chaque itération : l'ArrayMap d'androidx réutilise le même objet
                // `Map.Entry` d'un tour à l'autre, donc on ne doit pas accumuler les entrées.
                val content = LinkedHashMap<String, JsonElement>()
                for (entry in value.entries) {
                    val v = entry.value
                    if (v != null) content[entry.key.toString()] = toJsonElement(v)
                }
                JsonObject(content)
            }
            is Iterable<*> -> JsonArray(value.map { toJsonElement(it) })
            is Array<*> -> JsonArray(value.map { toJsonElement(it) })
            else -> try {
                AppJson.json.encodeToJsonElement(AppJson.json.serializersModule.serializer(value.javaClass), value)
            } catch (e: SerializationException) {
                throw IllegalArgumentException(
                    "Valeur non sérialisable dans un corps de requête : ${value.javaClass.name}", e
                )
            }
        }
    }
}
