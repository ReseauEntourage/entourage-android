package social.entourage.android.beforeLogin

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isFocusable
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.onboarding.login.LoginChangePhoneActivity
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingChoiceActivity

fun loginRobot(func: LoginRobot.() -> Unit) = LoginRobot().apply { func() }

class LoginRobot {

    fun typePhoneNumber(phoneNumber: String) {
        onView(withId(R.id.ui_login_phone_et_phone))
            .inRoot(RootMatchers.isFocusable())
            .perform(
                typeText(phoneNumber)
            )
    }

    fun typeCode(code: String) {
        onView(withId(R.id.ui_login_et_code))
            .inRoot(RootMatchers.isFocusable())
            .perform(
                typeText(code)
            )
    }

    fun clickSubmit() {
        onView(withId(R.id.ui_login_button_signup))
            .inRoot(RootMatchers.isFocusable())
            .perform(click())
    }

    fun clickResendCode() {
        onView(withId(R.id.ui_login_button_resend_code)).perform(click())
    }

    fun clickChangePhone() {
        onView(withId(R.id.ui_login_button_change_phone)).perform(click())
    }

    fun clickBack() {
        onView(withId(R.id.icon_back)).perform(click())
    }

    fun clickResendCodeAction() {
        onView(withText(R.string.login_button_resend_code_action)).perform(click())
    }

    fun clearPhoneNumber() {
        onView(withId(R.id.ui_login_phone_et_phone)).perform(clearText())
    }

    infix fun verify(func: LoginVerificationRobot.() -> Unit) =
        LoginVerificationRobot().apply { func() }
}

class LoginVerificationRobot {

    fun isLoginSuccessful() {
        intended(hasComponent(MainActivity::class.java.name))
    }

    fun isLoginFailureDisplayed(
        titleId: Int = R.string.login_error_title,
        actionId: Int = R.string.login_retry_label
    ) {
        onView(withText(titleId)).check(matches(isDisplayed()))
        onView(withText(actionId)).perform(click())
    }

    fun isNetworkErrorDisplayed() {
        onView(withText(R.string.login_error_network)).check(matches(isDisplayed()))
    }

    fun isResendCodeActionDisplayed() {
        onView(withText(R.string.login_button_resend_code_action)).check(matches(isDisplayed()))
    }

    fun isChangePhoneScreenDisplayed() {
        intended(hasComponent(LoginChangePhoneActivity::class.java.name))
    }

    fun isPreOnboardingScreenDisplayed() {
        intended(hasComponent(PreOnboardingChoiceActivity::class.java.name))
    }
}
