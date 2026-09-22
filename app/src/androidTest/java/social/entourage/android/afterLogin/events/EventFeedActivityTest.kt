package social.entourage.android.afterLogin.events

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.e2e.E2EScreenshot
import social.entourage.android.events.details.feed.EventFeedActivity
import social.entourage.android.tools.utils.Const

@RunWith(AndroidJUnit4::class)
class EventFeedActivityTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("event_details")

    private fun fetchEventIdFromApi(): Int {
        checkUserIsLoggedIn()

        return runBlocking(Dispatchers.IO) {
            val response = EntourageApplication.get().apiModule.eventsRequest
                .getAllEvents(
                    page = 1,
                    per = 10,
                    travelDistance = null,
                    latitude = null,
                    longitude = null,
                    period = "all",
                )
                .execute()

            response.body()?.allEvents?.firstOrNull()?.id
        } ?: throw IllegalStateException("Aucun événement trouvé depuis l'API")
    }

    @Test
    fun test_event_feed_activity_launches() {
        val eventID = fetchEventIdFromApi()

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            EventFeedActivity::class.java,
        ).apply {
            putExtra(Const.EVENT_ID, eventID)
        }

        ActivityScenario.launch<EventFeedActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                super.setUp(activity)
            }

            Espresso.onView(ViewMatchers.withId(R.id.event_name))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            screenshot.shoot("details d'un event")
        }
    }
}