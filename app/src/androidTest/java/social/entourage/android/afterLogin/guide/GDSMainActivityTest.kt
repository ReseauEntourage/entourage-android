package social.entourage.android.afterLogin.guide

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.guide.GDSMainActivity
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class GDSMainActivityTest : EntourageTestAfterLogin() {

    @get:Rule
    var activityRule = ActivityScenarioRule(GDSMainActivity::class.java)

    @Before
    fun setUp() {
         activityRule.scenario.onActivity { activity ->
             super.setUp(activity)
         }
    }

    @Test
    fun testDisplayMap() {
        val latch = CountDownLatch(1)

        // On configure l'observation sur le thread UI
        activityRule.scenario.onActivity { activity ->
            if (activity.guideFg.isMapReady.value == true) {
                latch.countDown()
            } else {
                activity.guideFg.isMapReady.observeForever { ready ->
                    if (ready == true) {
                        latch.countDown()
                    }
                }
            }
        }

        // On attend que la carte soit prête sur le thread de TEST
        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail("La LiveData isMapReady n'a pas reçu la valeur 'true' dans le délai imparti")
        }

        // Espresso synchronise automatiquement et doit être appelé depuis le thread de test
        onView(withText(R.string.gds_title)).check(matches(isDisplayed()))
    }
}
