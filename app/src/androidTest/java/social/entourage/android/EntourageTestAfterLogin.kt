package social.entourage.android

import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import social.entourage.android.api.OnboardingAPI
import timber.log.Timber

open class EntourageTestAfterLogin : EntourageTestWithAPI() {
    private val login = "0651234145"
    private val password = "108674"

    protected fun checkUserIsLoggedIn(activity: AppCompatActivity?) {
        activity?.let {
            if (EntourageApplication[activity].authenticationController.isAuthenticated == false)
                login(login, password)
        }
    }

    private fun login(phoneNumber: String, codePwd: String) {
        OnboardingAPI.getInstance().login(phoneNumber, codePwd) { isOK, loginResponse, _ ->
            if (isOK) {
                throw Exception("Login should fail")
            }
        }
    }

    protected fun checkFirstConnectionScreen() {
        try {
            onView(withId(R.id.ui_button_login)).apply {
                check(matches(isDisplayed()))
                perform(click())
            }
        } catch (e: NoMatchingViewException) {
            Timber.w(e)
        }
    }

    override fun setUp(activity: AppCompatActivity) {
        checkUserIsLoggedIn(activity)
        super.setUp(activity)
        Intents.init()
    }

    override fun tearDown() {
        Intents.release()
        super.tearDown()
    }

    protected fun checkLoginSuccessful() {
        intended(hasComponent(MainActivity::class.java.name))
    }

}
