package social.entourage.android.afterLogin

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.e2e.E2EScreenshot
import social.entourage.android.tools.utils.Const
import social.entourage.android.user.partner.PartnerDetailActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class PartnerDetailActivityTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("partner_detail")

    private fun fetchPartnerIdFromApi(): Int {
        checkUserIsLoggedIn()

        return runBlocking(Dispatchers.IO) {
            val response = EntourageApplication.get().apiModule.partnerRequest
                .allPartners
                .execute()

            response.body()?.partners?.firstOrNull()?.id?.toInt()
        } ?: throw IllegalStateException("Aucun partenaire trouvé depuis l'API")
    }

    @Test
    fun testPartnerDetails() {
        val partnerID = fetchPartnerIdFromApi()

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            PartnerDetailActivity::class.java
        ).apply {
            putExtra(Const.PARTNER_ID, partnerID)
        }

        ActivityScenario.launch<PartnerDetailActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                super.setUp(activity)
            }

            Espresso.onView(ViewMatchers.withId(R.id.asso_profile_image_association))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Espresso.onView(ViewMatchers.withId(R.id.asso_profile_name))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            screenshot.shoot("details_partenaire")

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
}