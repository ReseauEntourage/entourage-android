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
import androidx.test.espresso.matcher.ViewMatchers.withText
import timber.log.Timber

open class EntourageTestBeforeLogin : EntourageTestWithAPI() {
    //private var activity: AppCompatActivity? = null

    protected fun checkNoUserIsLoggedIn(activity: AppCompatActivity?) {
        activity?.let {
            EntourageApplication[activity].authenticationController.logOutUser()
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
        checkNoUserIsLoggedIn(activity)
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

    protected fun checkLoginFailure(
        titleId: Int = R.string.login_error_title,
        actionId: Int = R.string.login_retry_label
    ) {
        onView(withText(titleId)).check(matches(isDisplayed()))
        onView(withText(actionId)).perform(click())
    }
}
