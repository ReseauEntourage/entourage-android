package social.entourage.android.afterLogin

import android.Manifest
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.e2e.E2EScreenshot

@RunWith(AndroidJUnit4::class)
class MainActivityTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("main_activity")

    private val permissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    // La home est en Compose : son titre n'est pas un TextView, Espresso ne le voit pas.
    private val composeRule = createEmptyComposeRule()

    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(permissionRule)
        .around(composeRule)

    @Before
    fun customSetUp() {
        super.setUp(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testSimpleStartActivity() {
        checkUserIsLoggedIn()
        ActivityScenario.launch(MainActivity::class.java).use {
            checkNoOnboarding()
            myTakeSnapshot(this::class.java.simpleName)
            screenshot.shoot("accueil_main_activity")
            val title = ApplicationProvider.getApplicationContext<Context>().getString(R.string.home_title)
            composeRule.waitUntil(HOME_TIMEOUT_MS) {
                composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onAllNodesWithText(title)[0].assertIsDisplayed()
        }
    }

    private companion object {
        const val HOME_TIMEOUT_MS = 20_000L
    }
}
