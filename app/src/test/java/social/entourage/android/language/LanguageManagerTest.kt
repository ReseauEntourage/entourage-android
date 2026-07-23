package social.entourage.android.language

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageManagerTest {

    @Test
    fun `mapLanguageToCode returns correct code for all mapped languages`() {
        LanguageManager.languageMap.forEach { (name, code) ->
            assertEquals("Language $name should map to code $code", code, LanguageManager.mapLanguageToCode(name))
        }
    }

    @Test
    fun `mapLanguageToCode returns fr for unknown language`() {
        val unknownLanguage = "Unknown"
        assertEquals("fr", LanguageManager.mapLanguageToCode(unknownLanguage))
    }

    @Test
    fun `mapLanguageToCode returns fr for empty string`() {
        assertEquals("fr", LanguageManager.mapLanguageToCode(""))
    }
}
