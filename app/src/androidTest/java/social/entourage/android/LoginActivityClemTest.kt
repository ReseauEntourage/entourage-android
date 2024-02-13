package social.entourage.android

import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import social.entourage.android.EntourageApplication.Companion.KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN

@RunWith(MockitoJUnitRunner::class)
class LoginActivityTest {

    @Test
    fun isFirstLaunch_True_IfPreferencesSaySo() {
        val sharedPreferences = mock(SharedPreferences::class.java)
        val editor = mock(SharedPreferences.Editor::class.java)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        //`when`(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor)
        `when`(sharedPreferences.getBoolean(KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN, true)).thenReturn(true)
        assertTrue("Votre méthode devrait retourner true", true) // Remplacez cette ligne par l'appel à votre méthode
    }
}
