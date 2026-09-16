package social.entourage.android.authentication

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.lang.reflect.Type

/**
 * Source : https://github.com/fsilvestremorais/android-complex-preferences/blob/master/ComplexPreferences/src/br/com/kots/mob/complex/preferences/ComplexPreferences.java
 */
class ComplexPreferences(context: Context, namePreferences: String, mode: Int) {
    private val preferences: SharedPreferences
    private val editor: SharedPreferences.Editor

    fun putObject(key: String, `object`: Any?) {
        require(key.isNotEmpty()) { "key is empty or null" }
        editor.putString(key, if (`object` == null) "" else GSON.toJson(`object`))
    }

    fun commit() {
        editor.commit()
    }

    fun apply() {
        editor.apply()
    }

    fun <T> getObject(key: String, a: Class<T>?): T? {
        val gson = preferences.getString(key, null) ?: return null
        return try {
            GSON.fromJson(gson, a)
        } catch (e: Exception) {
            null
        }
    }

    fun <T> getObjectFromType(key: String, t: Type): T? {
        val gson = preferences.getString(key, null) ?: return null
        return try {
            GSON.fromJson(gson, t)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private val GSON = Gson()
    }

    init {
        val safeNamePreferences = namePreferences.ifEmpty { "complex_preferences" }
        preferences = context.getSharedPreferences(safeNamePreferences, mode)
        editor = preferences.edit()
    }
}