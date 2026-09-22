package social.entourage.android.afterLogin

import android.Manifest
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.e2e.E2EScreenshot
import social.entourage.android.guide.GDSMainActivity
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class GDSMainActivityTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("gds_main")

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION))

    @Test
    fun testDisplayMap() {
        ActivityScenario.launch(GDSMainActivity::class.java).use { scenario ->
            val latch = CountDownLatch(1)

            // On configure l'observation sur le thread UI
            scenario.onActivity { activity ->
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
                Assert.fail("La LiveData isMapReady n'a pas reçu la valeur 'true' dans le délai imparti")
            }

            // Espresso synchronise automatiquement et doit être appelé depuis le thread de test
            Espresso.onView(ViewMatchers.withText(R.string.gds_title))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            screenshot.shoot("carte_guide_solidaire")
        }
    }
}