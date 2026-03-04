package social.entourage.android.beforeLogin

import android.content.Context
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import org.junit.After
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageTestWithAPI
import social.entourage.android.MainActivity
import social.entourage.android.R
import timber.log.Timber

open class EntourageTestBeforeLogin : EntourageTestWithAPI() {
    //TODO have a proper method to find a unused number
    val unused_phone_number = "0699990002"

    protected fun checkNoUserIsLoggedIn(activity: Context?) {
        activity?.let {
            EntourageApplication.Companion[activity].authenticationController.logOutUser()
        }
    }

    protected fun checkFirstConnectionScreen() {
        try {
            Espresso.onView(ViewMatchers.withId(R.id.ui_button_login)).apply {
                check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                perform(ViewActions.click())
            }
        } catch (e: NoMatchingViewException) {
            Timber.Forest.w(e)
        }
    }

    override fun setUp(activity: Context) {
        checkNoUserIsLoggedIn(activity)
        super.setUp(activity)
        Intents.init()
    }

    @After
    override fun tearDown() {
        Intents.release()
        super.tearDown()
    }

    protected fun checkLoginSuccessful() {
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
    }

    protected fun checkLoginFailure(
        titleId: Int = R.string.login_error_title,
        actionId: Int = R.string.login_retry_label
    ) {
        Espresso.onView(ViewMatchers.withText(titleId))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText(actionId)).perform(ViewActions.click())
    }
}