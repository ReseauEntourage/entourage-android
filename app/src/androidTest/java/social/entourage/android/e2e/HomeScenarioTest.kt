package social.entourage.android.e2e

import android.Manifest
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
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
import social.entourage.android.home.HomeFragment
import social.entourage.android.home.HomePedagoAdapter
import social.entourage.android.home.HomeSectionHeaderAdapter

/**
 * Scénario E2E "connecté" : affichage de la home.
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
    private val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(permissionRule)
        .around(activityRule)

    @Before
    fun setUp() {
        checkUserIsLoggedIn()
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun affichageHomeSansCardHorsZone() {
        checkNoPopUpOnHome()
        onIdle()
        screenshot.shoot("accueil")

        onView(withId(R.id.rv_home)).check(matches(isDisplayed()))

        // EN-9355 : la card "Hors Zone" ("Tiens, ça semble un peu calme par ici…") a été retirée de la home.
        onView(withText("Tiens, ça semble un peu calme par ici…")).check { view, _ ->
            assert(view == null) { "La card Hors Zone ne devrait plus être affichée sur la home (EN-9355)" }
        }
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
        val (titleDisplayed, contentCount) = waitForPedagoSection()
        screenshot.shoot("accueil_contenus_a_decouvrir")

        assertEquals(
            "Le titre \"Contenus à découvrir\" doit être affiché si et seulement s'il reste des contenus " +
                "(contenus : $contentCount, titre affiché : $titleDisplayed)",
            contentCount > 0,
            titleDisplayed
        )
    }

    // ---------------------------------------------------------------- helpers

    /**
     * La section n'est ajoutée à la home qu'une fois les 4 premières requêtes terminées (skeleton).
     * Retourne (titre affiché, nombre de contenus affichés).
     */
    private fun waitForPedagoSection(): Pair<Boolean, Int> {
        val titre = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.home_title_discover_contents)
        val deadline = System.currentTimeMillis() + 40_000
        while (System.currentTimeMillis() < deadline) {
            var result: Pair<Boolean, Int>? = null
            activityRule.scenario.onActivity { activity ->
                val adapters = (findHomeFragment(activity.supportFragmentManager)
                    ?.view?.findViewById<RecyclerView>(R.id.rv_home)
                    ?.adapter as? ConcatAdapter)?.adapters.orEmpty()
                val header = adapters.filterIsInstance<HomeSectionHeaderAdapter>().firstOrNull { it.title == titre }
                val contents = adapters.filterIsInstance<HomePedagoAdapter>().firstOrNull()
                if (header != null && contents != null) {
                    result = (header.itemCount > 0) to contents.itemCount
                }
            }
            result?.let { return it }
            Thread.sleep(250)
        }
        error("La section \"Contenus à découvrir\" n'a jamais été ajoutée à la home")
    }

    private fun findHomeFragment(fm: FragmentManager): HomeFragment? {
        for (fragment: Fragment in fm.fragments) {
            if (fragment is HomeFragment) return fragment
            findHomeFragment(fragment.childFragmentManager)?.let { return it }
        }
        return null
    }
}
