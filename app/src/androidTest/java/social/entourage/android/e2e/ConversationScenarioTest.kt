package social.entourage.android.e2e

import android.Manifest
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.IdlingPolicies
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.comment.CommentActivity
import java.util.concurrent.TimeUnit

/**
 * Scénario E2E "connecté" : liste des conversations -> ouverture de la première conversation
 * -> envoi d'un message -> appui long sur ce message pour exercer les 3 actions du menu
 * qui s'affichent pour un message qu'on a soi-même envoyé : Copier, Modifier, Supprimer.
 * (Le "Signaler" n'apparaît que sur les messages des autres, et n'est donc pas testé ici ;
 * l'appui long sur un item de la liste des conversations, lui, ne déclenche actuellement
 * aucun menu côté appli.)
 * Nécessite que le compte de test E2E ait déjà au moins une conversation : ce test est
 * autoentretenu puisqu'il y écrit un message à chaque exécution.
 * Un screenshot est pris à chaque vue / action pour vérifier visuellement le déroulé.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class ConversationScenarioTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("conversation")

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        // La connexion doit être effective AVANT le lancement de MainActivity : sinon
        // BaseSecuredActivity.checkConnexion() la redirige vers l'onboarding et la détruit
        // immédiatement (cas d'une install fraîche, sans session déjà sauvegardée).
        ensureLoggedIn()
        // Le chargement des avatars (Glide) sur un écran de conversation avec beaucoup de
        // messages fait que le looper principal ne se déclare "idle" que rarement ; le
        // timeout par défaut d'Espresso (60s) est trop court dans ce cas précis.
        IdlingPolicies.setMasterPolicyTimeout(180, TimeUnit.SECONDS)
        IdlingPolicies.setIdlingResourceTimeout(180, TimeUnit.SECONDS)
        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @After
    fun tearDownScenario() {
        scenario.close()
    }

    private fun ensureLoggedIn() {
        if (!EntourageApplication.get().authenticationController.isAuthenticated) {
            OnboardingAPI.getInstance()
                .syncLogin(E2ECredentials.PHONE, E2ECredentials.PASSWORD) { isOK, _, _ ->
                    if (!isOK) throw Exception("E2E: la connexion prealable a echoue")
                }
        }
    }

    /**
     * Position du dernier message de la conversation actuellement affichée, pour cibler
     * notre propre message (celui qu'on vient d'envoyer/modifier) via l'appui long.
     */
    private fun lastMessagePosition(): Int {
        var position = 0
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val activity = ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<CommentActivity>()
                .firstOrNull()
            position = (activity?.commentsList?.size ?: 1) - 1
        }
        return position
    }

    /**
     * La capture d'écran (UiDevice.takeScreenshot) fait parfois perdre brièvement le focus
     * fenêtre sur cet environnement, ce qui fait échouer l'interaction Espresso suivante
     * (RootViewWithoutFocusException) ; on laisse un court instant au focus pour se rétablir.
     */
    private fun shoot(label: String) {
        screenshot.shoot(label)
        SystemClock.sleep(500)
    }

    private fun currentMessageEditTextContent(): String {
        var text = ""
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val activity = ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<CommentActivity>()
                .firstOrNull()
            text = activity?.binding?.commentMessage?.text?.toString() ?: ""
        }
        return text
    }

    /**
     * Sur cet environnement, typeText() ne tape parfois que le premier caractère (l'appui sur
     * une touche déclenche un changement de layout — cf. handleSendButtonState() qui échange le
     * drawable du bouton d'envoi dès que le champ n'est plus vide — qui interrompt la frappe en
     * cours). On vérifie le contenu réellement saisi et on retape si besoin.
     */
    private fun typeMessageReliably(text: String) {
        var attempts = 0
        while (currentMessageEditTextContent() != text && attempts < 6) {
            onView(withId(R.id.comment_message)).perform(clearText())
            onView(withId(R.id.comment_message)).perform(typeText(text))
            onIdle()
            attempts++
        }
        onView(withId(R.id.comment_message)).perform(closeSoftKeyboard())
    }

    /**
     * Les bulles de message sont en Compose, sans id ni testTag : le centre par défaut de
     * l'item (utilisé par longClick()) tombe souvent dans la zone vide à gauche de la bulle
     * pour un message "à moi" (bulle + avatar alignés à droite, cf. MessageBubbleItem).
     * On vise donc un point situé à 80% de la largeur de l'item, dans la bulle, à gauche
     * de l'avatar (~8% de large, collé au bord droit).
     */
    private fun longClickOnMessageBubble(): ViewAction = GeneralClickAction(
        Tap.LONG,
        GeneralLocation.translate(GeneralLocation.CENTER_RIGHT, -0.20f, 0f),
        Press.FINGER,
        InputDevice.SOURCE_UNKNOWN,
        MotionEvent.BUTTON_PRIMARY
    )

    private fun longClickOnOwnMessage() {
        onView(allOf(withId(R.id.comments), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(lastMessagePosition(), longClickOnMessageBubble())
        )
        onIdle()
    }

    @Test
    fun conversation() {
        checkNoPopUpOnHome()
        shoot("accueil")

        onView(withId(R.id.navigation_messages)).perform(click())
        onIdle()
        shoot("liste_conversations")

        onView(allOf(withId(R.id.recycler_view), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(0, click())
        )
        onIdle()
        shoot("conversation_ouverte")

        val message = "Message test e2e ${System.currentTimeMillis()}"
        typeMessageReliably(message)
        shoot("message_saisi")

        onView(withId(R.id.comment)).perform(click())
        onIdle()
        shoot("message_envoye")

        // Appui long sur notre message -> menu d'actions -> Copier le texte
        longClickOnOwnMessage()
        shoot("menu_actions_copier")
        onView(withText(R.string.message_action_copy)).perform(click())
        onIdle()
        shoot("apres_copier")

        // Appui long -> menu d'actions -> Modifier
        longClickOnOwnMessage()
        shoot("menu_actions_modifier")
        onView(withText(R.string.message_action_edit)).perform(click())
        onIdle()
        shoot("mode_edition")

        val messageModifie = "$message (modifie)"
        typeMessageReliably(messageModifie)
        shoot("message_modifie_saisi")

        onView(withId(R.id.comment)).perform(click())
        onIdle()
        shoot("message_modifie_envoye")

        // Appui long -> menu d'actions -> Supprimer mon message
        longClickOnOwnMessage()
        shoot("menu_actions_supprimer")
        onView(withText(R.string.message_action_delete)).perform(click())
        onIdle()
        shoot("message_supprime")
    }
}
