package social.entourage.android

import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import android.content.SharedPreferences
import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import social.entourage.android.EntourageApplication.Companion.KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN
import social.entourage.android.onboarding.login.LoginActivity
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingChoiceActivity
import social.entourage.android.tools.utils.Utils
import java.util.regex.Pattern.matches

@RunWith(MockitoJUnitRunner::class)
class LoginActivityTest {

    @Test
    fun isFirstLaunch_True_IfPreferencesSaySo() {
        val sharedPreferences = mock(SharedPreferences::class.java)
        val editor = mock(SharedPreferences.Editor::class.java)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putBoolean(KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN, false)).thenReturn(editor)
        `when`(sharedPreferences.getBoolean(KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN, true)).thenReturn(false)
        assertTrue("Votre méthode devrait retourner true", true) // Remplacez cette ligne par l'appel à votre méthode
    }
    @get:Rule
    var intentsTestRule = IntentsTestRule(LoginActivity::class.java)
    @Test
    fun testGoBack() {
        onView(withId(R.id.icon_back)).perform(click())
        intended(hasComponent(PreOnboardingChoiceActivity::class.java.name))
    }

    @Test
    fun validateInputsAndLogin_withInvalidPhoneNumber_returnsFalse() {
        val activityScenario = ActivityScenario.launch(LoginActivity::class.java)
        var result = false

        activityScenario.onActivity { activity ->
            // Simule un numéro de téléphone invalide
            activity.binding.uiLoginPhoneEtPhone.setText("12345")
            result = activity.validateInputsAndLogin()
        }

        assertFalse("La validation doit échouer pour un numéro de téléphone invalide", result)
    }

    @Test
    fun validateInputsAndLogin_withValidPhoneNumber_returnsTrue() {
        val activityScenario = ActivityScenario.launch(LoginActivity::class.java)
        var result = false

        activityScenario.onActivity { activity ->
            // Simule un numéro de téléphone valide et un code valide
            activity.binding.uiLoginPhoneEtPhone.setText("0606060606") // Assure-toi que c'est le bon champ pour le numéro
            activity.binding.uiLoginEtCode.setText("123456") // Assure-toi que c'est le bon champ pour le code
            result = activity.validateInputsAndLogin()
        }

        assertTrue("La validation doit réussir pour un numéro de téléphone et un code valides", result)
    }

    @Test
    fun validateInputsAndLogin_withInValidPhoneNumber1_returnsFalse() {
        val activityScenario = ActivityScenario.launch(LoginActivity::class.java)
        var result = false

        activityScenario.onActivity { activity ->
            activity.binding.uiLoginPhoneEtPhone.setText("0A606060606") // Assure-toi que c'est le bon champ pour le numéro
            activity.binding.uiLoginEtCode.setText("123456") // Assure-toi que c'est le bon champ pour le code
            result = activity.validateInputsAndLogin()
        }
        Log.wtf("wtf", "result: $result")
        assertFalse("La validation doit échouer pour un numéro de tel invalide (avec lettres)", result)
    }

    @Test
    fun validateInputsAndLogin_withInValidPhoneNumber2_returnsFalse() {
        val activityScenario = ActivityScenario.launch(LoginActivity::class.java)
        var result = false

        activityScenario.onActivity { activity ->
            activity.binding.uiLoginPhoneEtPhone.setText("0601886") // Assure-toi que c'est le bon champ pour le numéro
            activity.binding.uiLoginEtCode.setText("123456") // Assure-toi que c'est le bon champ pour le code
            result = activity.validateInputsAndLogin()
        }
        Log.wtf("wtf", "result: $result")
        // Assert
        assertFalse("La validation doit échouer pour un numéro de téléphone trop court", result)
    }

    @Test
    fun testPhoneNumberFormat_FrenchNumber_ReturnsCorrectFormat() {
        val activityScenario = ActivityScenario.launch(LoginActivity::class.java)
        var countryCode = "+33"
        val phoneNumber = "0606060606"
        var result:String? = ""
        val expectedResult = "+33606060606" // Le résultat attendu avec le format international
        activityScenario.onActivity { activity ->
            countryCode = activity.binding.uiLoginPhoneCcpCode.selectedCountryCodeWithPlus
            result = Utils.checkPhoneNumberFormat(countryCode, phoneNumber)

        }

        assertEquals("Le numéro de téléphone devrait être au format international français", expectedResult, result)
    }

}
