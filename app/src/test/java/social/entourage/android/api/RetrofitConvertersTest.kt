package social.entourage.android.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import social.entourage.android.api.json.AppJson
import java.io.File
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Filet de sécurité : chaque type de retour (et chaque `@Body` typé) des interfaces `api/request`
 * doit avoir un serializer kotlinx. Sans `@Serializable`, Retrofit lève
 * `IllegalArgumentException: Unable to create converter` au premier appel de la méthode,
 * ce qui fait planter l'écran concerné (ex. la carte, `ClusterPoiResponse`).
 */
class RetrofitConvertersTest {

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://localhost/")
        .addConverterFactory(AppJson.json.asConverterFactory(AppJson.CONTENT_TYPE.toMediaType()))
        .build()

    @Test
    fun everyRequestInterfaceType_hasAKotlinxSerializer() {
        val offenders = sortedSetOf<String>()

        requestInterfaces().forEach { itf ->
            itf.declaredMethods.forEach { method ->
                val where = "${itf.simpleName}.${method.name}"
                val returned = (method.genericReturnType as? ParameterizedType)
                    ?.takeIf { it.rawType == Call::class.java }
                    ?.actualTypeArguments?.firstOrNull()
                if (returned != null && !isHandledElsewhere(returned)) {
                    try {
                        retrofit.responseBodyConverter<Any>(returned, method.annotations)
                    } catch (e: IllegalArgumentException) {
                        offenders += "$where retourne $returned"
                    }
                }
                method.genericParameterTypes.forEachIndexed { i, type ->
                    if (method.parameterAnnotations[i].none { it is Body } || isHandledElsewhere(type)) {
                        return@forEachIndexed
                    }
                    try {
                        retrofit.requestBodyConverter<Any>(
                            type, method.parameterAnnotations[i], method.annotations
                        )
                    } catch (e: IllegalArgumentException) {
                        offenders += "$where prend en @Body $type"
                    }
                }
            }
        }

        if (offenders.isNotEmpty()) {
            fail(
                "Types sans serializer kotlinx (ajouter @Serializable) :\n" +
                    offenders.joinToString("\n") { " - $it" }
            )
        }
    }

    /** Types gérés par Retrofit lui-même ou par `AnyMapConverterFactory`, hors kotlinx. */
    private fun isHandledElsewhere(type: Type): Boolean {
        val raw = (type as? ParameterizedType)?.rawType ?: type
        return raw in setOf<Type>(
            Void::class.java, Void.TYPE, Unit::class.java,
            ResponseBody::class.java, RequestBody::class.java, MultipartBody::class.java,
        ) || raw.toString().let { it.contains("ArrayMap") || it.contains("java.util.Map") }
    }

    private fun requestInterfaces(): List<Class<*>> {
        val dir = listOf("src/main", "app/src/main")
            .map { File(it, "java/social/entourage/android/api/request") }
            .first { it.isDirectory }
        val declaration = Regex("""^interface\s+(\w+)""", RegexOption.MULTILINE)
        return dir.listFiles { f -> f.extension == "kt" }!!.flatMap { f ->
            declaration.findAll(f.readText()).map {
                Class.forName("social.entourage.android.api.request.${it.groupValues[1]}")
            }.toList()
        }
    }
}
