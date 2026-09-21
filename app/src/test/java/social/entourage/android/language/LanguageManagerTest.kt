package social.entourage.android.language

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Locale

@RunWith(MockitoJUnitRunner::class)
class LanguageManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setup() {
        whenever(context.getSharedPreferences(any(), any())).thenReturn(sharedPreferences)
        whenever(sharedPreferences.edit()).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)
    }

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

    @Test
    fun `saveLanguageToPreferences saves the correct language code`() {
        LanguageManager.saveLanguageToPreferences(context, "en")
        
        verify(editor).putString("selected_language", "en")
        verify(editor).apply()
    }

    @Test
    fun `loadLanguageFromPreferences returns saved language`() {
        whenever(sharedPreferences.getString("selected_language", "fr")).thenReturn("es")
        
        val result = LanguageManager.loadLanguageFromPreferences(context)
        assertEquals("es", result)
    }

    @Test
    fun `loadLanguageFromPreferences returns default fr when nothing saved`() {
        whenever(sharedPreferences.getString("selected_language", "fr")).thenReturn("fr")
        
        val result = LanguageManager.loadLanguageFromPreferences(context)
        assertEquals("fr", result)
    }

    @Test
    fun `isLanguagePreferenceAlreadySet returns true when key exists`() {
        whenever(sharedPreferences.contains("selected_language")).thenReturn(true)
        
        assertTrue(LanguageManager.isLanguagePreferenceAlreadySet(context))
    }

    @Test
    fun `isLanguagePreferenceAlreadySet returns false when key missing`() {
        whenever(sharedPreferences.contains("selected_language")).thenReturn(false)
        
        assertFalse(LanguageManager.isLanguagePreferenceAlreadySet(context))
    }

    @Test
    fun `getLocaleFromPreferences returns correct Locale object`() {
        whenever(sharedPreferences.getString("selected_language", "fr")).thenReturn("uk")
        
        val locale = LanguageManager.getLocaleFromPreferences(context)
        assertEquals(Locale.forLanguageTag("uk"), locale)
    }
}
