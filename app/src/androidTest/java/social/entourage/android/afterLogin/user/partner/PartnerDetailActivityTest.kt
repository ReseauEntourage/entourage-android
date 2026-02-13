package social.entourage.android.afterLogin.user.partner

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.tools.utils.Const
import social.entourage.android.user.partner.PartnerDetailActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class PartnerDetailActivityTest : EntourageTestAfterLogin() {

    @get:Rule
    val activityRule = ActivityScenarioRule<PartnerDetailActivity>(
        Intent(
            ApplicationProvider.getApplicationContext(),
            PartnerDetailActivity::class.java
        ).apply {
            // Set to true for a Demand, false for a Contribution
            putExtra(Const.PARTNER_ID, 1)
        }
    )

    @Before
    fun setUp() {
        activityRule.scenario.onActivity {activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun testPartnerDetails() {
        Espresso.onView(ViewMatchers.withId(R.id.asso_profile_image_association))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.asso_profile_name))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        //first we subscribe
        Espresso.onView(
            CoreMatchers.allOf(
                ViewMatchers.withId(R.id.asso_profile_subscribe),
                ViewMatchers.withText(R.string.follow)
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())
        //then we unsubscribe
        Espresso.onView(
            CoreMatchers.allOf(
                ViewMatchers.withId(R.id.asso_profile_subscribe),
                ViewMatchers.withText(R.string.following)
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.yes))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.asso_profile_icon_back))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(ViewActions.click())
    }
}