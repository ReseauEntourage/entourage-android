package social.entourage.android.deeplinks

import android.content.Intent
import androidx.core.net.toUri
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.MainActivity
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.e2e.E2EScreenshot

/**
 * One @Test per [DeeplinkTestData] entry (hardcoded, not a loop): each launches and tears down
 * its own MainActivity via [runDeeplinkTestCase], so tests don't share activity/navigation state
 * and a failure in one case can't cascade into the next.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class DeepLinksEspressoTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("deeplinks")
    private val testCases = DeeplinkTestData.getTestCases()

    // ----------------------------------
    // Custom schemes
    // ----------------------------------
    @Test
    fun customSchemeGuide() = runDeeplinkTestCase(testCases[0])

    @Test
    fun customSchemeEvents() = runDeeplinkTestCase(testCases[1])

    @Test
    fun customSchemeProfile() = runDeeplinkTestCase(testCases[2])

    @Test
    fun customSchemeBadge() = runDeeplinkTestCase(testCases[3])

    @Test
    fun customSchemeCreateAction() = runDeeplinkTestCase(testCases[4])

    @Test
    fun customSchemeTutorial() = runDeeplinkTestCase(testCases[5])

    @Test
    fun customSchemeGuideMap() = runDeeplinkTestCase(testCases[6])

    @Test
    fun customSchemeWebviewWithUrl() = runDeeplinkTestCase(testCases[7])

    // ----------------------------------
    // Official web links (/deeplink/ & /app/)
    // ----------------------------------
    @Test
    fun httpDeeplinkGuide() = runDeeplinkTestCase(testCases[8])

    @Test
    fun httpsDeeplinkProfile() = runDeeplinkTestCase(testCases[9])

    @Test
    fun httpsDeeplinkEvents() = runDeeplinkTestCase(testCases[10])

    @Test
    fun httpsDeeplinkCreateAction() = runDeeplinkTestCase(testCases[11])

    @Test
    fun httpsAppLinkHomepage() = runDeeplinkTestCase(testCases[12])

    @Test
    fun httpsAppLinkGroups() = runDeeplinkTestCase(testCases[13])

    @Test
    fun httpsAppLinkGroupDetail() = runDeeplinkTestCase(testCases[14])

    @Test
    fun httpsAppLinkOutings() = runDeeplinkTestCase(testCases[15])

    @Test
    fun httpsAppLinkOutingsNew() = runDeeplinkTestCase(testCases[16])

    @Test
    fun httpsAppLinkContributions() = runDeeplinkTestCase(testCases[17])

    @Test
    fun httpsAppLinkSolicitations() = runDeeplinkTestCase(testCases[18])

    @Test
    fun httpsAppLinkMap() = runDeeplinkTestCase(testCases[19])

    @Test
    fun httpsAppLinkBadges() = runDeeplinkTestCase(testCases[20])

    @Test
    fun httpsAppLinkResources() = runDeeplinkTestCase(testCases[21])

    @Test
    fun httpsAppLinkResourceDetail() = runDeeplinkTestCase(testCases[22])

    // ----------------------------------
    // Shared runner
    // ----------------------------------
    private fun runDeeplinkTestCase(testCase: DeeplinkTestCase) {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            onIdle()

            val intent = Intent(Intent.ACTION_VIEW, testCase.uriString.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            scenario.onActivity { activity ->
                DeepLinksManager.storeIntent(intent)
                DeepLinksManager.handleCurrentDeepLink(activity)
                //activity.useIntentForRedirection(intent)
            }

            onIdle()

            val resolvedView = DeepLinksManager.resolveDeepLinkView(testCase.uriString)
            assertEquals("Mismatch for ${testCase.description}", testCase.expectedView, resolvedView)

            screenshot.shoot(testCase.description)

            testCase.targetViewIdName?.let { viewIdName ->
                var resId = 0
                scenario.onActivity { activity ->
                    resId = activity.resources.getIdentifier(viewIdName, "id", activity.packageName)
                }
                if (resId != 0) {
                    onView(withId(resId)).check(matches(isDisplayed()))
                }
            }
        }
    }
}
