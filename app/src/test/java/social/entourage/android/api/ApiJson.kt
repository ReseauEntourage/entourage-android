package social.entourage.android.api

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.Json as KxJson
import org.junit.Assert.fail

/**
 * Point d'entrée unique des tests de parsing JSON : seul ce fichier connaît la bibliothèque
 * de (dé)sérialisation. Lors de la migration Gson -> kotlinx.serialization, il suffit de
 * changer [decode] / [encode] ; les assertions des tests restent identiques.
 *
 * Les comparaisons structurelles passent par kotlinx.serialization.json pour ne pas dépendre de Gson.
 */
object ApiJson {
    // Instance neuve a chaque appel : Gson fige le fuseau horaire par defaut a sa creation, et les tests
    // changent ce fuseau (UTC / Europe-Paris) ; un singleton rendrait les resultats dependants de l'ordre.
    private fun gson() = createApiGson()

    fun <T> decode(json: String, type: Class<T>): T = gson().fromJson(json, type)

    inline fun <reified T> decode(json: String): T = decode(json, T::class.java)

    fun encode(value: Any): String = gson().toJson(value)

    fun fixture(name: String): String =
        checkNotNull(ApiJson::class.java.classLoader?.getResourceAsStream("api/$name")) {
            "Fixture introuvable : api/$name"
        }.bufferedReader(Charsets.UTF_8).use { it.readText() }

    fun tree(json: String): JsonElement = KxJson.parseToJsonElement(json)

    /**
     * Vérifie que tout ce qui figure dans [expectedJson] se retrouve à l'identique dans
     * [encodedJson] (l'encodage peut contenir des champs en plus : champs sans @SerializedName,
     * valeurs par défaut...). Les listes doivent avoir la même taille.
     */
    fun assertContainsAll(expectedJson: String, encodedJson: String) {
        val problems = mutableListOf<String>()
        compare("$", tree(expectedJson), tree(encodedJson), problems)
        if (problems.isNotEmpty()) {
            fail("Sérialisation différente de la fixture :\n" + problems.joinToString("\n"))
        }
    }

    private fun compare(path: String, expected: JsonElement, actual: JsonElement?, problems: MutableList<String>) {
        when (expected) {
            is JsonObject -> {
                if (actual !is JsonObject) {
                    problems += "$path : objet attendu, obtenu $actual"
                    return
                }
                expected.forEach { (key, value) ->
                    if (value is JsonNull) return@forEach
                    if (!actual.containsKey(key)) problems += "$path.$key : clé absente de la sortie"
                    else compare("$path.$key", value, actual[key], problems)
                }
            }
            is JsonArray -> {
                if (actual !is JsonArray || actual.size != expected.size) {
                    problems += "$path : tableau attendu de taille ${expected.size}, obtenu $actual"
                    return
                }
                expected.forEachIndexed { i, e -> compare("$path[$i]", e, actual[i], problems) }
            }
            is JsonPrimitive -> {
                if (actual !is JsonPrimitive || actual.content != expected.content || actual.isString != expected.isString) {
                    problems += "$path : attendu $expected, obtenu $actual"
                }
            }
        }
    }
}
