package social.entourage.android.afterLogin

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.allOf
import org.junit.After
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageTestWithAPI
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI

open class EntourageTestAfterLogin : EntourageTestWithAPI() {
    private val login: String = BuildConfig.TEST_ACCOUNT_LOGIN
    private val password: String = BuildConfig.TEST_ACCOUNT_PWD


    protected fun checkUserIsLoggedIn() {
        if (!EntourageApplication.get().authenticationController.isAuthenticated) {
            login(login, password)
        }
    }

    private fun login(phoneNumber: String? = null, codePwd: String? = null) {
        val phoneNumber = phoneNumber ?: BuildConfig.TEST_ACCOUNT_LOGIN
        val codePwd = codePwd ?: BuildConfig.TEST_ACCOUNT_PWD
        OnboardingAPI.getInstance().syncLogin(phoneNumber, codePwd) { isOK, _, _ ->
            if (!isOK) {
                throw Exception("Login should not fail")
            }
        }
    }

    open fun closeAutofill() {
    }

    override fun setUp(activity: Context) {
        super.setUp(activity)
        Intents.init()
        checkUserIsLoggedIn()
    }


    @After
    override fun tearDown() {
        Intents.release()
        super.tearDown()
    }

    protected fun checkNoOnboarding() {
        try {
            onView(allOf(withText(R.string.onboarding_presentation_btn_negative),isDisplayed()))
                .perform(click())
        } catch (e: Exception) {
            //No onboarding
        }
    }
}