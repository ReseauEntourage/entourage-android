package social.entourage.android.afterLogin

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
import social.entourage.android.R
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
                isActionDetailDisplayed(R.string.action_name_Demand, R.string.action_name_Contrib)
                //TODO clean the screen so it is appearing only once before adding it back
                //isCategoryDisplayed(R.string.action_name_Demand, R.string.action_social_name)
            }
            screenshot.shoot("action_detail")

            myEntouragesRobot(composeTestRule) {
                clickModify()
                clickAcceptCharte()
                selectCategory(social.entourage.android.api.model.ActionSection.services)
                clickNext()
                clickNext()
                clickNext()
            }
            // Catégorie « Service » (hors « Temps de partage ») : pas d'étape de partage, les trois « Suivant »
            // mènent à l'aperçu, en Compose. On y vérifie la catégorie sans publier : l'action réelle du
            // compte de test n'est pas modifiée.
            val service = InstrumentationRegistry.getInstrumentation().targetContext
                .getString(R.string.action_services_name)
            composeTestRule.waitUntil(10_000) {
                composeTestRule.onAllNodesWithText(service).fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onAllNodesWithText(service)[0].assertIsDisplayed()
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