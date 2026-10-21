package social.entourage.android.e2e

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.actions.create.CreateActionActivity
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.api.model.ActionSection
import social.entourage.android.api.model.ActionCancel
import social.entourage.android.api.request.ContribCancelWrapper
import social.entourage.android.api.request.DemandCancelWrapper
import social.entourage.android.tools.utils.Const
import timber.log.Timber

/**
 * Scenario E2E "connecte" complet, sur le modele de ConversationScenarioTest / GroupCommentScenarioTest :
 * cree reellement une demande, puis une contribution, de bout en bout contre le vrai backend de
 * staging (charte -> categorie -> titre/description -> localisation -> partage -> creation ->
 * ecran de succes -> retour a l'accueil).
 *
 * Auto-nettoyant : chaque test annule (DELETE) l'action qu'il vient de creer via l'API
 * `cancelContribution`/`cancelDemand`, retrouvee par son titre unique horodate via
 * `GET users/me/actions`. On s'appuie ici sur un appel API direct plutot que sur le parcours
 * de suppression dans l'IU (`ActionDetailFragment`, bouton supprimer + 2 boites de dialogue) :
 * il n'existe pas, dans cette suite, de parcours d'IU deja exerce pour retrouver puis ouvrir
 * "mon" action fraichement creee dans une liste, et un appel API cible par id est plus fiable
 * qu'un enchainement a deux boites de dialogue sur un vrai appareil. C'est une deviation
 * deliberee par rapport a la convention "nettoyage via l'IU" des autres scenarios de ce dossier.
 *
 * L'ecran de charte (`CreateActionCGUFragment`) est rendu en Jetpack Compose : Espresso ne peut
 * pas traverser un `ComposeView` via `onView`. On le pilote donc via UiAutomator (`By.res`),
 * comme dans `CreateActionCharterScenarioTest`.
 *
 * L'assistant de creation est lui aussi en Compose : ses elements se ciblent par `testTag`
 * ("create_action_*", voir CreateActionScreen / CreateActionSteps) via la regle Compose. Le
 * selecteur d'adresse (`CreateActionPlaceFragment`) reste une View, pilotee par Espresso.
 * L'etape de partage n'apparait qu'une fois le groupe par defaut charge : on l'attend.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class CreateActionFullFlowScenarioTest : EntourageTestAfterLogin() {

    private val device: UiDevice by lazy {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    private val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    // Regle "vide" : l'activite est lancee par chaque test (demande ou contribution).
    private val composeTestRule = createEmptyComposeRule()

    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(permissionRule)
        .around(composeTestRule)

    companion object {
        private const val TITLE_PREFIX = "Unit test "
    }

    private var createdActionTitle: String? = null
    private var createdActionIsDemand: Boolean = false

    private fun launchWizard(isDemand: Boolean): ActivityScenario<CreateActionActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, CreateActionActivity::class.java).apply {
            putExtra(Const.IS_ACTION_DEMAND, isDemand)
        }
        val scenario = ActivityScenario.launch<CreateActionActivity>(intent)
        scenario.onActivity { activity -> setUp(activity) }
        return scenario
    }

    private fun tap(tag: String) {
        composeTestRule.onNodeWithTag(tag).performClick()
    }

    private fun type(tag: String, text: String) {
        composeTestRule.onNodeWithTag(tag).performTextInput(text)
        closeSoftKeyboard()
    }

    @After
    fun cleanupCreatedAction() {
        if (createdActionTitle == null) return
        try {
            val actionsRequest = EntourageApplication.get().apiModule.actionsRequest
            // Annule l'action creee par ce test, ainsi que les restes d'executions interrompues
            // ("Unit test Demande ..." / "Unit test Contribution ...") : le prefixe permet aussi
            // un nettoyage en masse cote staging.
            val leftovers = (1..3).flatMap { page ->
                actionsRequest.getMyActions(page, 50).execute().body()?.allActions ?: emptyList()
            }.filter { it.title?.startsWith(TITLE_PREFIX) == true && !it.isCancel() }
            leftovers.forEach { action ->
                val id = action.id ?: return@forEach
                if (action.isDemand()) {
                    actionsRequest.cancelDemand(id, DemandCancelWrapper(ActionCancel(outcome = true))).execute()
                } else {
                    actionsRequest.cancelContribution(id, ContribCancelWrapper(ActionCancel(outcome = true))).execute()
                }
            }
        } catch (e: Exception) {
            Timber.tag("E2E").e(e, "Echec du nettoyage de l'action creee par le test")
        }
    }

    private fun waitForLocationText() {
        val deadline = System.currentTimeMillis() + 30_000
        while (System.currentTimeMillis() < deadline) {
            try {
                onView(allOf(withId(R.id.ui_onboard_place_tv_location), isDisplayed(), withText(not(""))))
                    .check(matches(isDisplayed()))
                return
            } catch (e: androidx.test.espresso.NoMatchingViewException) {
                Thread.sleep(1_000)
            } catch (e: AssertionError) {
                Thread.sleep(1_000)
            }
        }
    }

    private fun runFullCreationFlow(isDemand: Boolean) {
        val screenshot = E2EScreenshot(if (isDemand) "creation_demande" else "creation_contribution")
        createdActionIsDemand = isDemand

        val scenario = launchWizard(isDemand)

        device.wait(Until.hasObject(By.res("banner_title")), 5_000)
        screenshot.shoot("charte")
        // EN-9620 : le CTA est en bas du contenu scrollable, plus dans un footer sticky.
        device.scrollCharterToAccept().click()
        onIdle()

        // Categorie "Temps de partage" : c'est elle qui ajoute l'etape de partage (4 etapes au lieu de 3).
        tap("create_action_category_${ActionSection.social}")
        screenshot.shoot("categorie_choisie")
        tap("create_action_next")

        val uniqueTitle = "$TITLE_PREFIX${if (isDemand) "Demande" else "Contribution"} ${System.currentTimeMillis()}"
        createdActionTitle = uniqueTitle
        type("create_action_name", uniqueTitle)
        type("create_action_description", "Action creee automatiquement par le test E2E.")
        screenshot.shoot("titre_description_remplis")
        tap("create_action_next")

        tap("create_action_location")
        onView(allOf(withId(R.id.ui_onboard_bt_location), isDisplayed())).perform(click())
        // La geolocalisation est asynchrone (FusedLocationProviderClient, aucune idling resource) :
        // on attend que l'adresse soit affichee avant de valider (jusqu'a 30 s, le premier fix
        // d'un emulateur fraichement demarre est lent).
        waitForLocationText()
        onView(withText(R.string.validate)).check(matches(isDisplayed())).perform(click())
        screenshot.shoot("localisation_validee")
        tap("create_action_next")

        // L'etape de partage est ajoutee a la liste une fois le groupe par defaut charge.
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule.onAllNodesWithTag("create_action_share_no").fetchSemanticsNodes().isNotEmpty()
        }
        screenshot.shoot("avant_partage")
        tap("create_action_share_no")
        screenshot.shoot("partage_refuse")
        tap("create_action_next")
        composeTestRule.onNodeWithTag("create_action_preview_title").assertIsDisplayed()
        screenshot.shoot("apercu")

        // Dernier bouton : "Publier" (l'apercu precede l'envoi).
        tap("create_action_next")
        onIdle()
        screenshot.shoot("creation_soumise")

        // Le titre de succes est un gabarit ("Votre %s a bien ete creee !") : on s'appuie sur le bouton
        // de fin, propre a l'ecran de succes.
        onView(withText(R.string.action_create_end_finish_bt)).check(matches(isDisplayed()))
        screenshot.shoot("succes")
        onView(withText(R.string.action_create_end_finish_bt)).perform(click())

        checkNoOnboarding()
        onView(withText(R.string.home_title)).check(matches(isDisplayed()))
        scenario.close()
    }

    @Test
    fun creationDemande() {
        runFullCreationFlow(isDemand = true)
    }

    @Test
    fun creationContribution() {
        runFullCreationFlow(isDemand = false)
    }
}
