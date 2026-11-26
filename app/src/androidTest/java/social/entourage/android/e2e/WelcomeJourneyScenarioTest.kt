package social.entourage.android.e2e

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.api.model.Summary
import social.entourage.android.events.list.WelcomeEventsListActivity
import social.entourage.android.home.HomeComposeFragment
import social.entourage.android.home.NationalGroupsActivity
import social.entourage.android.home.WelcomeJourneyApi
import social.entourage.android.home.WelcomeJourneyStep

/**
 * Scénario E2E "connecté" : parcours de bienvenue facultatif de la home Compose (EN-9653).
 *
 * Le summary est simulé (`HomeComposeFragment.summaryEventsOverride`) et l'appel réseau
 * POST users/onboarding_step_skipped est intercepté (`WelcomeJourneyApi.skipHandler`) :
 * rien n'est modifié sur le compte de test côté serveur. On vérifie que
 *  - aucune étape n'est verrouillée,
 *  - chaque bouton / lien envoie vers le bon écran,
 *  - chaque "Passer cette étape" envoie le bon argument et passe l'étape en "Passée",
 *  - le menu "Masquer le parcours" demande confirmation puis masque le parcours,
 *  - le tableau de debug (clic sur le logo) se met à jour à chaque clic.
 *
 * La carte du parcours est un seul item de la liste Compose de la home : les nœuds sont ciblés par
 * leur texte / contentDescription et amenés à l'écran avec `performScrollTo`.
 * Seule l'étape 1 a un effet de bord réel : ouvrir la vidéo appelle `resources/welcome`
 * côté serveur (comportement existant, hors périmètre de ce test).
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class WelcomeJourneyScenarioTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("parcours_bienvenue")
    private val device: UiDevice by lazy {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    private val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )
    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(permissionRule)
        .around(composeRule)

    private val originalSkipHandler = WelcomeJourneyApi.skipHandler
    private val originalHideHandler = WelcomeJourneyApi.hideHandler
    private val skipRequests = mutableListOf<String>()
    private var hideRequests = 0

    private fun string(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Before
    fun setUp() {
        checkUserIsLoggedIn()
        composeRule.activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
        skipRequests.clear()
        hideRequests = 0
        // POST users/onboarding_journey_hidden est intercepté : pas de masquage réel du compte de test
        WelcomeJourneyApi.hideHandler = { onResult ->
            hideRequests++
            onResult(true)
        }
        WelcomeJourneyApi.skipHandler = { step, onResult ->
            skipRequests += step.apiStep
            onResult(true)
        }
        clearHiddenFlag()
        checkNoPopUpOnHome()
        onIdle()
    }

    @After
    override fun tearDown() {
        WelcomeJourneyApi.skipHandler = originalSkipHandler
        WelcomeJourneyApi.hideHandler = originalHideHandler
        HomeComposeFragment.summaryEventsOverride = null
        clearHiddenFlag()
        super.tearDown()
    }

    // ---------------------------------------------------------------- scénarios

    @Test
    fun aucuneEtapeVerrouilleeEtBoutonsAmenentAuBonEcran() {
        showJourney(emptyList())
        screenshot.shoot("parcours_vierge")

        // Aucune étape verrouillée : les 4 boutons et liens "Passer" sont actifs dès le premier affichage
        WelcomeJourneyStep.entries.forEach { step ->
            val skip = skipNode(step)
            skip.performScrollTo().assertIsDisplayed()
            buttonNode(step).assertIsDisplayed().assertIsEnabled()
        }

        stubDestinations()

        buttonNode(WelcomeJourneyStep.WEBINAR).performScrollTo().performClick()
        intended(
            allOf(
                hasComponent(WelcomeEventsListActivity::class.java.name),
                hasExtra("TYPE", "welcome")
            )
        )

        buttonNode(WelcomeJourneyStep.NATIONAL_GROUP).performScrollTo().performClick()
        intended(hasComponent(NationalGroupsActivity::class.java.name))
        screenshot.shoot("destinations_verifiees")

        // Étape 1 : ouvre la vidéo de bienvenue (bottom sheet)
        buttonNode(WelcomeJourneyStep.VIDEO).performScrollTo().performClick()
        assertTrue(
            "La vidéo de bienvenue ne s'est pas ouverte",
            device.wait(Until.hasObject(By.res(packageName(), "webview_video")), 10_000)
        )
        screenshot.shoot("video_bienvenue")
        device.pressBack()

        // Étape 4 « premier événement » : page événements classique (onglet Événements), sans filtre
        buttonNode(WelcomeJourneyStep.PAPOTAGES).performScrollTo().performClick()
        onView(withId(R.id.navigation_events)).check(matches(isSelected()))
    }

    @Test
    fun passerChaqueEtapeEnvoieLeBonArgumentEtPasseLEtape() {
        showJourney(emptyList())

        WelcomeJourneyStep.entries.forEach { step ->
            skipNode(step).performScrollTo().performClick()
            composeRule.waitForIdle()

            // L'étape passe en "Passée" avec le lien "Découvrir cette étape", sans bouton ni "Passer"
            redoNode(step).assertExists()
            skipNode(step).assertDoesNotExist()
            buttonNode(step).assertDoesNotExist()
            screenshot.shoot("etape_${step.index}_passee")
        }

        assertEquals(
            "Arguments envoyés à POST users/onboarding_step_skipped",
            listOf("welcome_watched", "neighborhood_national", "webinar_or_first_steps", "papotages"),
            skipRequests
        )
        // Tout est passé : les 4 étapes sont "Passées", aucune n'est comptée comme faite
        scrollToJourneyTop()
        composeRule.onAllNodesWithText(string(R.string.welcome_journey_badge_skipped)).assertCountEquals(4)
        composeRule.onNodeWithContentDescription("0 étape faite sur 4").assertExists()
        screenshot.shoot("tout_passe")
    }

    @Test
    fun etapeFaiteEtEtapePasseeSontAfficheesSelonLeSummary() {
        showJourney(
            listOf(
                WelcomeJourneyStep.VIDEO.doneEvent,
                WelcomeJourneyStep.NATIONAL_GROUP.skippedEvent
            )
        )
        screenshot.shoot("une_faite_une_passee")

        scrollToJourneyTop()
        composeRule.onNodeWithContentDescription("1 étape faite sur 4").assertExists()
        composeRule.onNode(hasText(STEP_TITLES[0]) and hasText("Terminé")).assertExists()
        composeRule.onNode(hasText(STEP_TITLES[1]) and hasText(string(R.string.welcome_journey_badge_skipped)))
            .performScrollTo().assertExists()
        redoNode(WelcomeJourneyStep.NATIONAL_GROUP).assertIsDisplayed()
        // Les étapes restantes restent actives et passables
        skipNode(WelcomeJourneyStep.WEBINAR).performScrollTo().assertIsDisplayed()
        buttonNode(WelcomeJourneyStep.WEBINAR).assertIsDisplayed().assertIsEnabled()
        skipNode(WelcomeJourneyStep.PAPOTAGES).performScrollTo().assertIsDisplayed()
        buttonNode(WelcomeJourneyStep.PAPOTAGES).assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun derniereEtapeFaiteNAffichePlusDeBouton() {
        showJourney(listOf(WelcomeJourneyStep.PAPOTAGES.doneEvent))
        screenshot.shoot("derniere_etape_faite")

        composeRule.onNode(hasText(STEP_TITLES[3]) and hasText("Terminé")).performScrollTo().assertExists()
        buttonNode(WelcomeJourneyStep.PAPOTAGES).assertDoesNotExist()
        skipNode(WelcomeJourneyStep.PAPOTAGES).assertDoesNotExist()
    }

    @Test
    fun decouvrirUneEtapePasseeOuvreLeBonEcran() {
        showJourney(
            listOf(
                WelcomeJourneyStep.NATIONAL_GROUP.skippedEvent,
                WelcomeJourneyStep.WEBINAR.skippedEvent
            )
        )
        stubDestinations()

        redoNode(WelcomeJourneyStep.WEBINAR).performScrollTo().performClick()
        intended(
            allOf(
                hasComponent(WelcomeEventsListActivity::class.java.name),
                hasExtra("TYPE", "welcome")
            )
        )

        redoNode(WelcomeJourneyStep.NATIONAL_GROUP).performScrollTo().performClick()
        intended(hasComponent(NationalGroupsActivity::class.java.name))
    }

    @Test
    fun menuMasquerLeParcoursDemandeConfirmation() {
        showJourney(emptyList())
        scrollToJourneyTop()
        val options = string(R.string.welcome_journey_options)

        // Annuler : le parcours reste affiché
        composeRule.onNodeWithContentDescription(options).performClick()
        composeRule.onNodeWithText(string(R.string.welcome_journey_hide_menu)).assertIsDisplayed()
        screenshot.shoot("menu_options")
        composeRule.onNodeWithText(string(R.string.welcome_journey_hide_menu)).performClick()

        onView(withText(R.string.welcome_journey_hide_title)).check(matches(isDisplayed()))
        screenshot.shoot("confirmation_masquer")
        onView(withId(R.id.btn_hide_cancel)).perform(click())
        onView(withText(R.string.welcome_journey_hide_title)).check(doesNotExist())
        composeRule.onNodeWithContentDescription(options).assertIsDisplayed()

        // Confirmer : le parcours disparaît
        composeRule.onNodeWithContentDescription(options).performClick()
        composeRule.onNodeWithText(string(R.string.welcome_journey_hide_menu)).performClick()
        onView(withId(R.id.btn_hide_confirm)).perform(click())
        composeRule.waitForIdle()
        composeRule.onAllNodesWithContentDescription(options).assertCountEquals(0)
        assertEquals("Appels à POST users/onboarding_journey_hidden", 1, hideRequests)
        screenshot.shoot("parcours_masque")
    }

    @Test
    fun tableauDeDebugSeMetAJourACadaClic() {
        showJourney(emptyList())
        assertTrue(
            "Le tableau de debug n'apparaît pas après l'clic sur le logo",
            device.wait(Until.hasObject(By.textContains("DEBUG parcours de bienvenue")), 5_000)
        )
        onView(withText(containsString("1 Vidéo"))).check(matches(withText(containsString("TODO"))))

        skipNode(WelcomeJourneyStep.NATIONAL_GROUP).performScrollTo().performClick()
        composeRule.waitForIdle()
        onView(withText(containsString("Dernière action : Passer étape 2"))).check(matches(isDisplayed()))
        onView(withText(containsString("2 Groupe national"))).check(matches(withText(containsString("SKIPPED"))))
        screenshot.shoot("debug_apres_skip")
    }

    @Test
    fun parcoursCompletAfficheLEncartDeReussite() {
        showJourney(WelcomeJourneyStep.entries.map { it.doneEvent })
        scrollToJourneyTop()
        composeRule.onNodeWithContentDescription("4 étapes faites sur 4").assertExists()
        composeRule.onNode(hasText("Intégration complète", substring = true)).assertIsDisplayed()
        screenshot.shoot("parcours_termine")
    }

    // ---------------------------------------------------------------- helpers

    /** Clic sur le logo (debug) pour forcer l'affichage, puis injection du faux summary. */
    private fun showJourney(events: List<String>) {
        waitForHome()
        composeRule.onAllNodesWithContentDescription(string(R.string.app_name))[0].performClick()
        composeRule.activityRule.scenario.onActivity { activity ->
            HomeComposeFragment.summaryEventsOverride = events
            val home = findHomeFragment(activity.supportFragmentManager)
                ?: error("HomeComposeFragment introuvable")
            home.handleWelcomeJourneyState(Summary())
        }
        composeRule.waitForIdle()
        // Le parcours n'est affiché qu'une fois les 4 premières requêtes terminées (skeleton)
        composeRule.waitUntil(30_000) {
            composeRule.onAllNodesWithText(JOURNEY_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        scrollToJourneyTop()
    }

    private fun waitForHome() {
        composeRule.waitUntil(40_000) {
            composeRule.onAllNodesWithContentDescription(string(R.string.app_name)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun findHomeFragment(fm: FragmentManager): HomeComposeFragment? {
        for (fragment: Fragment in fm.fragments) {
            if (fragment is HomeComposeFragment) return fragment
            findHomeFragment(fragment.childFragmentManager)?.let { return it }
        }
        return null
    }

    /** Le parcours est le premier item de la liste Compose de la home. */
    private fun scrollToJourneyTop() {
        composeRule.onNode(
            hasScrollToIndexAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        ).performScrollToIndex(0)
        composeRule.waitForIdle()
    }

    private fun stubDestinations() {
        val cancelled = Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
        intending(hasComponent(WelcomeEventsListActivity::class.java.name)).respondWith(cancelled)
        intending(hasComponent(NationalGroupsActivity::class.java.name)).respondWith(cancelled)
    }

    private fun packageName() = InstrumentationRegistry.getInstrumentation().targetContext.packageName

    // Les libellés de bouton / lien sont uniques par étape (les liens "Passer" et "Découvrir" se répètent :
    // on retrouve la carte de l'étape par son titre).
    private fun buttonNode(step: WelcomeJourneyStep) = composeRule.onNodeWithText(
        when (step) {
            WelcomeJourneyStep.VIDEO -> string(R.string.welcome_video_cta)
            WelcomeJourneyStep.NATIONAL_GROUP -> "Voir les groupes"
            WelcomeJourneyStep.WEBINAR -> "Voir les prochaines dates"
            WelcomeJourneyStep.PAPOTAGES -> string(R.string.welcome_step4_cta)
        }
    )

    private fun skipNode(step: WelcomeJourneyStep) = linkNode(step, string(R.string.welcome_journey_skip_step))

    private fun redoNode(step: WelcomeJourneyStep) = linkNode(step, string(R.string.welcome_journey_redo_step))

    /**
     * Lien "Passer" / "Découvrir" de l'étape : les liens sont dans l'ordre des étapes, et seuls ceux dont
     * l'état s'y prête sont affichés. On les distingue donc par leur position parmi les cartes.
     */
    private fun linkNode(step: WelcomeJourneyStep, label: String) =
        composeRule.onNode(hasText(label) and hasAncestorCard(step))

    private fun hasAncestorCard(step: WelcomeJourneyStep) =
        androidx.compose.ui.test.hasAnyAncestor(hasText(STEP_TITLES[step.index - 1]))

    private fun clearHiddenFlag() {
        val id = EntourageApplication.get().me()?.id ?: return
        EntourageApplication.get().sharedPreferences.edit(commit = true) {
            remove("PREF_WELCOME_JOURNEY_HIDDEN_$id")
        }
    }

    private companion object {
        const val JOURNEY_TITLE = "Votre parcours de bienvenue"
        val STEP_TITLES = listOf(
            "1 min pour tout comprendre",
            "Rejoignez un groupe qui vous ressemble",
            "Rencontrez l'équipe en visio",
            "Participez à un premier événement"
        )
    }
}
