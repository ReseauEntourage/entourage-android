package social.entourage.android.afterLogin

import android.Manifest
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import social.entourage.android.MainActivity
import social.entourage.android.e2e.E2EScreenshot

@LargeTest
@RunWith(AndroidJUnit4::class)
class MyEntouragesTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("my_entourages")

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS))

    @Before
    fun customSetUp() {
        super.setUp(InstrumentationRegistry.getInstrumentation().targetContext)
        forceOnboarding(true)
    }

    @After
    fun customTearDown() {
        forceOnboarding(false)
    }

    @Test
    fun retrieveEntourages() {
        ActivityScenario.launch(MainActivity::class.java).use {
            checkNoPopUpOnHome()

            myEntouragesRobot {
                goToDonations()
                clickMyGroupsTab()
            } verify {
                isMyGroupsTabSelected()
            }
            screenshot.shoot("mes_groupes_tab")

            myEntouragesRobot {
                clickFirstAction()
            } verify {
                isActionDetailDisplayed("Demande")
                isCategoryDisplayed("Demande")
            }
            screenshot.shoot("action_detail")

            myEntouragesRobot(composeTestRule) {
                clickModify()
                clickAcceptCharte()
                selectCategoryAt(1) // Assuming index 1 in the category list
                clickNext()
                clickNext()
                clickNext()
            } verify {
                isCategoryDisplayed("Service")
            }
            screenshot.shoot("categorie_modifiee")
        }
    }

    @Test
    fun retrieveFeedsFailureNoInternetConnection() {
        ActivityScenario.launch(MainActivity::class.java).use {
            checkNoOnboarding()
            enableWifiAndData(false)

            myEntouragesRobot {
                goToDonations()
            } verify {
                isNetworkErrorDisplayed()
            }
            screenshot.shoot("erreur_reseau")
        }
    }
}
