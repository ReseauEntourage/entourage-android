package social.entourage.android.e2e

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
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
import social.entourage.android.home.HomeFragment
import social.entourage.android.home.HomeWelcomeJourneyAdapter
import social.entourage.android.home.NationalGroupsActivity
import social.entourage.android.home.WelcomeJourneyApi
import social.entourage.android.home.WelcomeJourneyStep

/**
 * Scénario E2E "connecté" : parcours de bienvenue facultatif de la home (EN-9653).
 *
 * Le summary est simulé (`HomeFragment.summaryEventsOverride`) et l'appel réseau
 * POST users/onboarding_step_skipped est intercepté (`WelcomeJourneyApi.skipHandler`) :
 * rien n'est modifié sur le compte de test côté serveur. On vérifie que
 *  - aucune étape n'est verrouillée,
 *  - chaque bouton / lien envoie vers le bon écran,
 *  - chaque "Passer cette étape" envoie le bon argument et passe l'étape en "Passée",
 *  - le menu "Masquer le parcours" demande confirmation puis masque le parcours,
 *  - le tableau de debug (clic sur le logo) se met à jour à chaque clic.
 *
 * La carte du parcours est un seul item du RecyclerView de la home : on la fait défiler avec
 * `scrollBy` sur `rv_home`. À régler sur device si la hauteur de l'écran change.
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
    private val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(permissionRule)
        .around(activityRule)

    private val originalSkipHandler = WelcomeJourneyApi.skipHandler
    private val skipRequests = mutableListOf<String>()

    @Before
    fun setUp() {
        checkUserIsLoggedIn()
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
        skipRequests.clear()
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
        HomeFragment.summaryEventsOverride = null
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
            scrollTo(skipId(step))
            onView(withId(buttonId(step))).check(matches(allOf(isDisplayed(), isEnabled())))
            onView(withId(skipId(step))).check(matches(isDisplayed()))
        }

        stubDestinations()

        // Étape 4 (jadis verrouillée tant que les précédentes n'étaient pas faites)
        scrollTo(R.id.btn_step_4)
        onView(withId(R.id.btn_step_4)).perform(click())
        intended(
            allOf(
                hasComponent(WelcomeEventsListActivity::class.java.name),
                hasExtra("TYPE", "papotages")
            )
        )

        scrollTo(R.id.btn_step_3)
        onView(withId(R.id.btn_step_3)).perform(click())
        intended(
            allOf(
                hasComponent(WelcomeEventsListActivity::class.java.name),
                hasExtra("TYPE", "welcome")
            )
        )

        scrollTo(R.id.btn_step_2)
        onView(withId(R.id.btn_step_2)).perform(click())
        intended(hasComponent(NationalGroupsActivity::class.java.name))
        screenshot.shoot("destinations_verifiees")

        // Étape 1 : ouvre la vidéo de bienvenue (bottom sheet)
        scrollTo(R.id.btn_step_1)
        onView(withId(R.id.btn_step_1)).perform(click())
        assertTrue(
            "La vidéo de bienvenue ne s'est pas ouverte",
            device.wait(Until.hasObject(By.res(packageName(), "webview_video")), 10_000)
        )
        screenshot.shoot("video_bienvenue")
        device.pressBack()
    }

    @Test
    fun passerChaqueEtapeEnvoieLeBonArgumentEtPasseLEtape() {
        showJourney(emptyList())

        WelcomeJourneyStep.entries.forEach { step ->
            scrollTo(skipId(step))
            onView(withId(skipId(step))).perform(click())
            onIdle()

            if (step != WelcomeJourneyStep.PAPOTAGES) {
                // L'étape passe en "Passée" avec le lien "Découvrir cette étape", sans bouton ni "Passer"
                onView(withId(redoId(step))).check(matches(isDisplayed()))
                onView(withId(skipId(step))).check(matches(not(isDisplayed())))
                onView(withId(buttonId(step))).check(matches(not(isDisplayed())))
                screenshot.shoot("etape_${step.index}_passee")
            }
        }

        assertEquals(
            "Arguments envoyés à POST users/onboarding_step_skipped",
            listOf("welcome_watched", "neighborhood_national", "webinar_or_first_steps", "papotages"),
            skipRequests
        )
        // Tout est passé : même encart de félicitations que si tout était réussi
        scrollToJourneyTop()
        onView(withId(R.id.tv_step_counter)).check(matches(withText("4/4")))
        onView(withText(containsString("Intégration complète"))).check(matches(isDisplayed()))
        screenshot.shoot("tout_passe_encart_vert")
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
        onView(withId(R.id.tv_step_counter)).check(matches(withText("1/4")))
        onView(withId(R.id.tv_badge_step_1)).check(matches(withText("Terminé")))
        scrollTo(R.id.tv_badge_step_2)
        onView(withId(R.id.tv_badge_step_2)).check(matches(withText(R.string.welcome_journey_badge_skipped)))
        onView(withId(R.id.btn_redo_step_2)).check(matches(isDisplayed()))
        // Les étapes restantes restent actives et passables
        scrollTo(R.id.btn_skip_step_3)
        onView(withId(R.id.btn_step_3)).check(matches(allOf(isDisplayed(), isEnabled())))
        onView(withId(R.id.btn_skip_step_3)).check(matches(isDisplayed()))
        scrollTo(R.id.btn_skip_step_4)
        onView(withId(R.id.btn_step_4)).check(matches(allOf(isDisplayed(), isEnabled())))
        onView(withId(R.id.btn_skip_step_4)).check(matches(isDisplayed()))
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

        scrollTo(R.id.btn_redo_step_3)
        onView(withId(R.id.btn_redo_step_3)).perform(click())
        intended(
            allOf(
                hasComponent(WelcomeEventsListActivity::class.java.name),
                hasExtra("TYPE", "welcome")
            )
        )

        scrollTo(R.id.btn_redo_step_2)
        onView(withId(R.id.btn_redo_step_2)).perform(click())
        intended(hasComponent(NationalGroupsActivity::class.java.name))
    }

    @Test
    fun menuMasquerLeParcoursDemandeConfirmation() {
        showJourney(emptyList())
        scrollToJourneyTop()

        // Annuler : le parcours reste affiché
        onView(withId(R.id.btn_more)).perform(click())
        onView(withText(R.string.welcome_journey_hide_menu)).inRoot(isPlatformPopup())
            .check(matches(isDisplayed()))
        screenshot.shoot("menu_options")
        onView(withText(R.string.welcome_journey_hide_menu)).inRoot(isPlatformPopup()).perform(click())

        onView(withText(R.string.welcome_journey_hide_title)).check(matches(isDisplayed()))
        screenshot.shoot("confirmation_masquer")
        onView(withId(R.id.btn_hide_cancel)).perform(click())
        onView(withText(R.string.welcome_journey_hide_title)).check(doesNotExist())
        onView(withId(R.id.btn_more)).check(matches(isDisplayed()))

        // Confirmer : le parcours disparaît
        onView(withId(R.id.btn_more)).perform(click())
        onView(withText(R.string.welcome_journey_hide_menu)).inRoot(isPlatformPopup()).perform(click())
        onView(withId(R.id.btn_hide_confirm)).perform(click())
        onIdle()
        onView(withId(R.id.btn_more)).check(doesNotExist())
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

        scrollTo(R.id.btn_skip_step_2)
        onView(withId(R.id.btn_skip_step_2)).perform(click())
        onIdle()
        onView(withText(containsString("Dernière action : Passer étape 2"))).check(matches(isDisplayed()))
        onView(withText(containsString("2 Groupe national"))).check(matches(withText(containsString("SKIPPED"))))
        screenshot.shoot("debug_apres_skip")
    }

    @Test
    fun parcoursCompletAfficheLEncartDeReussite() {
        showJourney(WelcomeJourneyStep.entries.map { it.doneEvent })
        scrollToJourneyTop()
        onView(withId(R.id.tv_step_counter)).check(matches(withText("4/4")))
        onView(withText(containsString("Intégration complète"))).check(matches(isDisplayed()))
        screenshot.shoot("parcours_termine")
    }

    // ---------------------------------------------------------------- helpers

    /** Clic sur le logo (debug) pour forcer l'affichage, puis injection du faux summary. */
    private fun showJourney(events: List<String>) {
        waitForHome()
        onView(withId(R.id.iv_logo_home)).perform(click())
        activityRule.scenario.onActivity { activity ->
            HomeFragment.summaryEventsOverride = events
            val home = findHomeFragment(activity.supportFragmentManager)
                ?: error("HomeFragment introuvable")
            home.handleWelcomeJourneyState(Summary())
        }
        onIdle()
        waitForJourneyAdapter()
        scrollToJourneyTop()
        assertTrue(
            "Le parcours de bienvenue n'est pas affiché",
            device.wait(Until.hasObject(By.res(packageName(), "tv_step_counter")), 15_000)
        )
    }

    /** Le parcours n'est ajouté à la home qu'une fois les 4 premières requêtes terminées (skeleton). */
    private fun waitForJourneyAdapter() {
        val deadline = System.currentTimeMillis() + 30_000
        while (System.currentTimeMillis() < deadline) {
            var attached = false
            activityRule.scenario.onActivity { activity ->
                val rv = findHomeFragment(activity.supportFragmentManager)
                    ?.view?.findViewById<RecyclerView>(R.id.rv_home)
                attached = (rv?.adapter as? ConcatAdapter)?.adapters
                    ?.any { it is HomeWelcomeJourneyAdapter && it.itemCount > 0 } == true
            }
            if (attached) return
            Thread.sleep(250)
        }
        error("Le parcours de bienvenue n'a jamais été ajouté à la home")
    }

    private fun waitForHome() {
        assertTrue(
            "La home ne s'est pas chargée",
            device.wait(Until.hasObject(By.res(packageName(), "iv_logo_home")), 40_000)
        )
    }

    private fun findHomeFragment(fm: FragmentManager): HomeFragment? {
        for (fragment: Fragment in fm.fragments) {
            if (fragment is HomeFragment) return fragment
            findHomeFragment(fragment.childFragmentManager)?.let { return it }
        }
        return null
    }

    /**
     * La carte du parcours est un seul item du RecyclerView, plus haut que l'écran : on repart du
     * haut du parcours puis on descend par pas de 300px jusqu'à ce que la vue soit visible.
     */
    private fun scrollTo(viewId: Int) {
        scrollToJourneyTop()
        repeat(15) {
            try {
                onView(withId(viewId)).check(matches(isDisplayingAtLeast(90)))
                return
            } catch (e: Throwable) {
                scrollHomeBy(300)
            }
        }
        onView(withId(viewId)).check(matches(isDisplayingAtLeast(90)))
    }

    /** Haut du parcours, avec une marge pour passer sous l'en-tête de la home. */
    private fun scrollToJourneyTop() {
        activityRule.scenario.onActivity { activity ->
            val home = findHomeFragment(activity.supportFragmentManager)
            val rv = home?.view?.findViewById<RecyclerView>(R.id.rv_home)
            val adapter = rv?.adapter as? ConcatAdapter
            val offset = adapter?.adapters
                ?.takeWhile { it !is HomeWelcomeJourneyAdapter }
                ?.sumOf { it.itemCount } ?: 0
            rv?.scrollToPosition(offset)
            rv?.scrollBy(0, -300)
        }
        onIdle()
    }

    private fun scrollHomeBy(dy: Int) {
        activityRule.scenario.onActivity { activity ->
            findHomeFragment(activity.supportFragmentManager)
                ?.view?.findViewById<RecyclerView>(R.id.rv_home)?.scrollBy(0, dy)
        }
        onIdle()
    }

    private fun stubDestinations() {
        val cancelled = Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)
        intending(hasComponent(WelcomeEventsListActivity::class.java.name)).respondWith(cancelled)
        intending(hasComponent(NationalGroupsActivity::class.java.name)).respondWith(cancelled)
    }

    private fun packageName() = InstrumentationRegistry.getInstrumentation().targetContext.packageName

    private fun buttonId(step: WelcomeJourneyStep) = when (step) {
        WelcomeJourneyStep.VIDEO -> R.id.btn_step_1
        WelcomeJourneyStep.NATIONAL_GROUP -> R.id.btn_step_2
        WelcomeJourneyStep.WEBINAR -> R.id.btn_step_3
        WelcomeJourneyStep.PAPOTAGES -> R.id.btn_step_4
    }

    private fun skipId(step: WelcomeJourneyStep) = when (step) {
        WelcomeJourneyStep.VIDEO -> R.id.btn_skip_step_1
        WelcomeJourneyStep.NATIONAL_GROUP -> R.id.btn_skip_step_2
        WelcomeJourneyStep.WEBINAR -> R.id.btn_skip_step_3
        WelcomeJourneyStep.PAPOTAGES -> R.id.btn_skip_step_4
    }

    private fun redoId(step: WelcomeJourneyStep) = when (step) {
        WelcomeJourneyStep.VIDEO -> R.id.btn_redo_step_1
        WelcomeJourneyStep.NATIONAL_GROUP -> R.id.btn_redo_step_2
        WelcomeJourneyStep.WEBINAR -> R.id.btn_redo_step_3
        WelcomeJourneyStep.PAPOTAGES -> R.id.btn_redo_step_4
    }

    private fun clearHiddenFlag() {
        val id = EntourageApplication.get().me()?.id ?: return
        EntourageApplication.get().sharedPreferences.edit(commit = true) {
            remove("PREF_WELCOME_JOURNEY_HIDDEN_$id")
        }
    }
}
