package social.entourage.android.e2e

import android.Manifest
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.v2.AndroidComposeTestRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.click
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
 * via la liste des événements -> bouton "+" -> assistant en 5 étapes -> aperçu -> publication.
 * Un screenshot est pris à chaque vue / action pour vérifier visuellement le déroulé.
 *
 * L'assistant est en Compose : ses éléments se ciblent par `testTag` ("create_event_*", voir
 * CreateEventScreen / CreateEventSteps) via la règle Compose ; les sélecteurs système (date, heure,
 * galerie de photos) et la liste des événements restent des Views, pilotées par Espresso.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class CreationEvenementScenarioTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("creation_evenement")

    private val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )
    private val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private val composeTestRule = AndroidComposeTestRule(
        activityRule = activityRule,
        activityProvider = { rule ->
            var activity: MainActivity? = null
            rule.scenario.onActivity { activity = it }
            activity ?: error("Activity was not created")
        }
    )

    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(permissionRule)
        .around(composeTestRule)

    private fun tap(tag: String) {
        composeTestRule.onNodeWithTag(tag).performClick()
    }

    private fun type(tag: String, text: String) {
        composeTestRule.onNodeWithTag(tag).performTextInput(text)
        closeSoftKeyboard()
    }

    private val isInterestChip = SemanticsMatcher("est une pastille de catégorie") { node ->
        node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("create_event_interest_") == true
    }

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
        screenshot.shoot("liste_evenements")

        onView(allOf(withId(R.id.create_event_expanded), isDisplayed())).perform(click())
        screenshot.shoot("etape1_vide")

        val titre = "Evenement test ${System.currentTimeMillis()}"
        type("create_event_name", titre)
        type("create_event_description", "Evenement cree automatiquement par le test E2E.")
        screenshot.shoot("etape1_texte_rempli")

        tap("create_event_add_photo")
        screenshot.shoot("etape1_choix_photo")
        onView(allOf(withId(R.id.recycler_view), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(0, click())
        )
        onView(withId(R.id.validate)).perform(click())
        screenshot.shoot("etape1_photo_choisie")

        tap("create_event_next")
        screenshot.shoot("etape2_date_et_heure")

        val demain = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
        tap("create_event_date")
        onView(withClassName(`is`(android.widget.DatePicker::class.java.name))).perform(
            PickerActions.setDate(
                demain.get(Calendar.YEAR),
                demain.get(Calendar.MONTH) + 1,
                demain.get(Calendar.DAY_OF_MONTH)
            )
        )
        onView(withId(android.R.id.button1)).perform(click())
        screenshot.shoot("etape2_date_choisie")

        tap("create_event_start_time")
        onView(withClassName(`is`(android.widget.TimePicker::class.java.name))).perform(
            PickerActions.setTime(10, 0)
        )
        onView(withId(android.R.id.button1)).perform(click())
        screenshot.shoot("etape2_heure_debut_choisie")

        tap("create_event_end_time")
        onView(withClassName(`is`(android.widget.TimePicker::class.java.name))).perform(
            PickerActions.setTime(12, 0)
        )
        onView(withId(android.R.id.button1)).perform(click())
        screenshot.shoot("etape2_heure_fin_choisie")

        tap("create_event_next")
        screenshot.shoot("etape3_lieu")

        tap("create_event_online")
        type("create_event_url", "https://www.entourage.social/")
        screenshot.shoot("etape3_en_ligne_rempli")

        tap("create_event_next")
        screenshot.shoot("etape4_categories")

        // Les catégories viennent de l'API : on attend qu'elles soient affichées.
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodes(isInterestChip).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodes(isInterestChip).onFirst().performClick()
        screenshot.shoot("etape4_categorie_choisie")

        tap("create_event_next")
        screenshot.shoot("etape5_partage")

        // Aucun groupe coché : l'événement n'est pas partagé.
        tap("create_event_next")
        screenshot.shoot("apercu")

        tap("create_event_next")
        screenshot.shoot("apres_clic_creer")

        onView(withText(R.string.event_success_title)).check(matches(isDisplayed()))
        screenshot.shoot("confirmation_succes")
    }
}
