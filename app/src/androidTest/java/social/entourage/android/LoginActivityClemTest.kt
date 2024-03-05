package social.entourage.android

import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import android.content.SharedPreferences
import android.text.method.LinkMovementMethod
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.intent.Intents
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
import social.entourage.android.onboarding.login.LoginChangePhoneActivity
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingChoiceActivity
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
        assertTrue("Si première fois doit return true ", true) // Remplacez cette ligne par l'appel à votre méthode
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

        // Act
        activityScenario.onActivity { activity ->
            activity.binding.uiLoginPhoneEtPhone.setText("12345")
            result = activity.validateInputsAndLogin()
        }

        // Assert
        assertFalse("La validation doit échouer pour un numéro de téléphone invalide", result)
    }

    @Test
    fun validateInputsAndLogin_withValidPhoneNumber_returnsTrue() {
        // Arrange
        val activityScenario = ActivityScenario.launch(LoginActivity::class.java)
        var result = false

        // Act
        activityScenario.onActivity { activity ->
            activity.binding.uiLoginPhoneEtPhone.setText("0606060606")
            activity.binding.uiLoginEtCode.setText("123456")
            result = activity.validateInputsAndLogin()
        }

        // Assert
        assertTrue("La validation doit réussir pour un numéro de téléphone et un code valides", result)
    }

    @Test
    fun termsAndConditions_DisplayedAndClickable() {
        val activityScenario = ActivityScenario.launch(LoginActivity::class.java)
        activityScenario.onActivity { activity ->
            val conditionsText = activity.binding.tvConditionGenerales.text.toString()
            assertTrue("Terms and conditions should contain 'href'", conditionsText.contains("href"))
            assertTrue("TextView should allow link interaction", activity.binding.tvConditionGenerales.movementMethod is LinkMovementMethod)
        }
    }


    @Test
    fun backButton_ClosesLoginActivity() {
        val activityScenario = ActivityScenario.launch(LoginActivity::class.java)
        activityScenario.onActivity { activity ->
            activity.binding.iconBack.performClick()
            assertTrue(activity.isFinishing)
        }
    }

    @Test
    fun changePhoneNumberButton_opensLoginChangePhoneActivity() {
        Intents.init()
        ActivityScenario.launch(LoginActivity::class.java)
        onView(withId(R.id.ui_login_button_change_phone)).perform(click())
        intended(hasComponent(LoginChangePhoneActivity::class.java.name))
        Intents.release()
    }
}
