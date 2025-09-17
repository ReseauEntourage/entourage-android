package social.entourage.android

import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.withId
import social.entourage.android.api.OnboardingAPI

open class EntourageTestAfterLogin : EntourageTestWithAPI() {
    private val login: String = BuildConfig.TEST_ACCOUNT_LOGIN
    private val password: String = BuildConfig.TEST_ACCOUNT_PWD


    protected fun checkUserIsLoggedIn() {
            if (!EntourageApplication.get().authenticationController.isAuthenticated) {
                login(login, password)
                //wait for response
                //Thread.sleep(30000)
            }
    }

    private fun login(phoneNumber: String, codePwd: String) {
        OnboardingAPI.getInstance().login(phoneNumber, codePwd) { isOK, loginResponse, _ ->
            if (!isOK) {
                throw Exception("Login should not fail")
            }
        }
    }

    open fun closeAutofill() {
    }

    protected fun forceLogIn(){
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_et_code)).perform(typeText(BuildConfig.TEST_ACCOUNT_PWD), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_button_signup)).perform(click())
        checkUserIsLoggedIn()
    }

    override fun setUp(activity: AppCompatActivity) {
        super.setUp(activity)
        Intents.init()
    }

    override fun tearDown() {
        Intents.release()
        super.tearDown()
    }

}
