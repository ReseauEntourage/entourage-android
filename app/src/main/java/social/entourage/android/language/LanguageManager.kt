package social.entourage.android.language

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_SELECTED_LANGUAGE = "selected_language"

    val languageMap = mapOf(
        "Français" to "fr",
        "English" to "en",
        "Deutsch" to "de",
        "Español" to "es",
        "Polski" to "pl",
        "Українська" to "uk",
        "Română" to "ro",
        "العربية" to "ar"
    )

    fun mapLanguageToCode(language: String): String {
        return languageMap[language] ?: "fr"  // Default to French if not found
    }

    fun saveLanguageToPreferences(context: Context, languageCode: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_SELECTED_LANGUAGE, languageCode)
        editor.apply()
    }

    fun loadLanguageFromPreferences(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Defaulting to French ("fr") if no language has been selected
        return sharedPreferences.getString(KEY_SELECTED_LANGUAGE, "fr") ?: "fr"
    }

    fun getLocaleFromPreferences(context: Context): Locale {
        val languageCode = loadLanguageFromPreferences(context)
        return Locale(languageCode)
    }
    fun setLocale(context: Context, langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getCurrentDeviceLanguage(): String {
        return Locale.getDefault().language
    }
}