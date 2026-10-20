package social.entourage.android.e2e

import android.Manifest
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin

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
}
