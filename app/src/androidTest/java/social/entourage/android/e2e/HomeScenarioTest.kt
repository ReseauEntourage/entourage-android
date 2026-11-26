package social.entourage.android.e2e

import android.Manifest
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToIndex
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.home.HomeComposeFragment
import social.entourage.android.home.compose.HomeUiState

/**
 * Scénario E2E "connecté" : affichage de la home (Compose, [HomeComposeFragment]).
 * Couvre EN-9355 (suppression de la card "Hors Zone") : vérifie que la home
 * s'affiche sans erreur et que le wording de l'ancienne card HZ n'est plus présent.
 * Un screenshot est pris à chaque vue / action pour vérifier visuellement le déroulé.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class HomeScenarioTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("home")

    private val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )
    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(permissionRule)
        .around(composeRule)

    private fun string(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Before
    fun setUp() {
        checkUserIsLoggedIn()
        composeRule.activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun affichageHomeSansCardHorsZone() {
        checkNoPopUpOnHome()
        waitForHome()
        screenshot.shoot("accueil")

        // La home Compose est affichée : le logo de l'en-tête est visible
        composeRule.onAllNodesWithContentDescription(string(R.string.app_name))[0].assertIsDisplayed()

        // EN-9355 : la card "Hors Zone" ("Tiens, ça semble un peu calme par ici…") a été retirée de la home.
        composeRule.onNodeWithText("Tiens, ça semble un peu calme par ici…").assertDoesNotExist()
        screenshot.shoot("accueil_sans_card_hz")
    }

    /**
     * Le titre "Contenus à découvrir" ne s'affiche que s'il reste au moins un contenu pédagogique
     * non lu (même règle que sur iOS) : jamais de titre orphelin au-dessus d'une liste vide.
     * Les contenus déjà lus dépendent du compte de test : on vérifie la cohérence titre / contenus
     * dans les deux cas, sans supposer qu'il en reste.
     */
    @Test
    fun titreContenusADecouvrirSeulementSiDesContenusSontAffiches() {
        checkNoPopUpOnHome()
        val contentCount = waitForPedagoSection()
        val titre = string(R.string.home_title_discover_contents)
        val list = composeRule.onNode(homeListMatcher())

        // Le titre et les contenus sont tout en bas de la liste : on la fait défiler jusqu'à eux.
        val titleDisplayed = if (contentCount > 0) {
            list.performScrollToNode(hasText(titre))
            composeRule.onNodeWithText(titre).assertIsDisplayed()
            true
        } else {
            val lastIndex = lastIndexOfHomeList()
            list.performScrollToIndex(lastIndex)
            composeRule.waitForIdle()
            composeRule.onAllNodesWithText(titre).fetchSemanticsNodes().isNotEmpty()
        }
        screenshot.shoot("accueil_contenus_a_decouvrir")

        assertEquals(
            "Le titre \"Contenus à découvrir\" doit être affiché si et seulement s'il reste des contenus " +
                "(contenus : $contentCount, titre affiché : $titleDisplayed)",
            contentCount > 0,
            titleDisplayed
        )
    }

    // ---------------------------------------------------------------- helpers

    private fun homeListMatcher() = hasScrollToIndexAction() and
        SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)

    private fun lastIndexOfHomeList(): Int {
        val info = composeRule.onNode(homeListMatcher()).fetchSemanticsNode()
            .config.getOrNull(SemanticsProperties.CollectionInfo)
        return ((info?.rowCount ?: 1) - 1).coerceAtLeast(0)
    }

    private fun waitForHome() {
        composeRule.waitUntil(40_000) {
            composeRule.onAllNodesWithContentDescription(string(R.string.app_name)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * Le contenu remplace le squelette une fois les 4 premières requêtes terminées, puis la section est
     * alimentée par les ressources pédagogiques. Retourne le nombre de contenus affichés.
     */
    private fun waitForPedagoSection(): Int {
        val start = System.currentTimeMillis()
        var contentShownAt: Long? = null
        while (System.currentTimeMillis() - start < 40_000) {
            var shown = false
            var count = 0
            composeRule.activityRule.scenario.onActivity { activity ->
                val ui = findHomeFragment(activity.supportFragmentManager)?.let { homeUi(it) }
                if (ui != null && ui.contentShown) {
                    shown = true
                    count = ui.pedagos.size
                    // Titre et contenus doivent toujours aller ensemble
                    assertEquals(ui.pedagos.isNotEmpty(), ui.pedagoTitleVisible)
                }
            }
            if (shown) {
                val since = contentShownAt ?: System.currentTimeMillis().also { contentShownAt = it }
                // Laisse arriver la réponse des contenus pédagogiques si elle n'était pas encore là
                if (count > 0 || System.currentTimeMillis() - since > 8_000) return count
            }
            Thread.sleep(250)
        }
        error("La section \"Contenus à découvrir\" n'a jamais été alimentée sur la home")
    }

    /** `ui` est privé dans le fragment : lecture par réflexion, sans toucher au code de l'app. */
    private fun homeUi(fragment: HomeComposeFragment): HomeUiState =
        HomeComposeFragment::class.java.getDeclaredField("ui")
            .apply { isAccessible = true }
            .get(fragment) as HomeUiState

    private fun findHomeFragment(fm: FragmentManager): HomeComposeFragment? {
        for (fragment: Fragment in fm.fragments) {
            if (fragment is HomeComposeFragment) return fragment
            findHomeFragment(fragment.childFragmentManager)?.let { return it }
        }
        return null
    }
}
