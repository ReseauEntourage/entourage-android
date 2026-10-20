package social.entourage.android.authentication

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.serializer
import social.entourage.android.api.json.AppJson

/**
 * Stockage d'objets sérialisés en JSON dans les SharedPreferences.
 * Source d'origine : https://github.com/fsilvestremorais/android-complex-preferences
 *
 * Format : JSON kotlinx.serialization ([AppJson]), relisible depuis les valeurs écrites par les
 * versions précédentes (Gson) : clés identiques, dates relues par `ApiDateSerializer`.
 */
class ComplexPreferences(context: Context, namePreferences: String, mode: Int) {
    private val preferences: SharedPreferences
    private val editor: SharedPreferences.Editor

    fun <T> putObject(key: String, serializer: KSerializer<T>, value: T?) {
        require(key.isNotEmpty()) { "key is empty or null" }
        editor.putString(key, if (value == null) "" else AppJson.json.encodeToString(serializer, value))
    }

    inline fun <reified T> putObject(key: String, value: T?) = putObject(key, serializer<T>(), value)

    /** Efface la valeur stockée (comme l'ancien `putObject(key, null)` : chaîne vide). */
    fun removeObject(key: String) {
        require(key.isNotEmpty()) { "key is empty or null" }
        editor.putString(key, "")
    }

    fun commit() {
        editor.commit()
    }

    fun apply() {
        editor.apply()
    }

    fun <T> getObject(key: String, serializer: KSerializer<T>): T? {
        val json = preferences.getString(key, null) ?: return null
        return try {
            AppJson.json.decodeFromString(serializer, json)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    inline fun <reified T> getObject(key: String): T? = getObject(key, serializer<T>())

    init {
        val safeNamePreferences = namePreferences.ifEmpty { "complex_preferences" }
        preferences = context.getSharedPreferences(safeNamePreferences, mode)
        editor = preferences.edit()
    }
}
