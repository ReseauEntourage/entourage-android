package social.entourage.android.e2e

import android.Manifest
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.api.OnboardingAPI
import java.util.Calendar

/**
 * Scénario E2E "connecté" : création d'un événement simple (en ligne, le lendemain du test)
 * via la liste des événements -> bouton "+" -> assistant en 5 étapes.
 * Un screenshot est pris à chaque vue / action pour vérifier visuellement le déroulé.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class CreationEvenementScenarioTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("creation_evenement")

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
        ensureLoggedIn()
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    private fun ensureLoggedIn() {
        if (!EntourageApplication.get().authenticationController.isAuthenticated) {
            OnboardingAPI.getInstance()
                .syncLogin(E2ECredentials.PHONE, E2ECredentials.PASSWORD) { isOK, _, _ ->
                    if (!isOK) throw Exception("E2E: la connexion prealable a echoue")
                }
        }
    }

    @Test
    fun creationEvenement() {
        checkNoPopUpOnHome()
        screenshot.shoot("accueil")

        onView(withId(R.id.navigation_events)).perform(click())
        onIdle()
        screenshot.shoot("liste_evenements")

        onView(allOf(withId(R.id.create_event_expanded), isDisplayed())).perform(click())
        onIdle()
        screenshot.shoot("etape1_vide")

        val titre = "Evenement test ${System.currentTimeMillis()}"
        onView(withId(R.id.event_name)).perform(typeText(titre), closeSoftKeyboard())
        onView(withId(R.id.event_description)).perform(
            typeText("Evenement cree automatiquement par le test E2E."),
            closeSoftKeyboard()
        )
        screenshot.shoot("etape1_texte_rempli")

        onView(withId(R.id.add_photo_layout)).perform(click())
        onIdle()
        screenshot.shoot("etape1_choix_photo")
        onView(allOf(withId(R.id.recycler_view), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(0, click())
        )
        onView(withId(R.id.validate)).perform(click())
        screenshot.shoot("etape1_photo_choisie")

        onView(withText(R.string.next)).perform(click())
        screenshot.shoot("etape2_date_et_heure")

        val demain = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        onView(withId(R.id.event_date)).perform(click())
        onView(withClassName(`is`(android.widget.DatePicker::class.java.name))).perform(
            PickerActions.setDate(
                demain.get(Calendar.YEAR),
                demain.get(Calendar.MONTH) + 1,
                demain.get(Calendar.DAY_OF_MONTH)
            )
        )
        onView(withId(android.R.id.button1)).perform(click())
        screenshot.shoot("etape2_date_choisie")

        onView(withId(R.id.start_time)).perform(click())
        onView(withClassName(`is`(android.widget.TimePicker::class.java.name))).perform(
            PickerActions.setTime(10, 0)
        )
        onView(withId(android.R.id.button1)).perform(click())
        screenshot.shoot("etape2_heure_debut_choisie")

        onView(withId(R.id.end_time)).perform(click())
        onView(withClassName(`is`(android.widget.TimePicker::class.java.name))).perform(
            PickerActions.setTime(12, 0)
        )
        onView(withId(android.R.id.button1)).perform(click())
        screenshot.shoot("etape2_heure_fin_choisie")

        onView(withText(R.string.next)).perform(click())
        screenshot.shoot("etape3_lieu")

        onView(withId(R.id.online)).perform(click())
        onView(withId(R.id.event_url)).perform(
            typeText("https://www.entourage.social/"),
            closeSoftKeyboard()
        )
        screenshot.shoot("etape3_en_ligne_rempli")

        onView(withText(R.string.next)).perform(click())
        screenshot.shoot("etape4_categories")

        onView(allOf(withId(R.id.egs2_recycler_view), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(0, click())
        )
        screenshot.shoot("etape4_categorie_choisie")

        onView(withText(R.string.next)).perform(click())
        screenshot.shoot("etape5_partage")

        onView(withId(R.id.dont_share)).perform(click())
        screenshot.shoot("etape5_ne_pas_partager")

        onView(withText(R.string.create)).perform(click())
        onIdle()
        screenshot.shoot("apres_clic_creer")

        onView(withText(R.string.event_success_title)).check(matches(isDisplayed()))
        screenshot.shoot("confirmation_succes")
    }
}
