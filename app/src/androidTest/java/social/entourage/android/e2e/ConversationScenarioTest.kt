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
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.tools.utils.Const
import timber.log.Timber
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assume.assumeTrue
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
 * -> envoi d'un message -> 3-points sur ce message pour exercer les 3 actions du menu qui
 * s'affichent pour un message qu'on a soi-même envoyé : Copier, Modifier, Supprimer.
 * (Le "Signaler" n'apparaît que sur les messages des autres, et n'est donc pas testé ici ;
 * l'appui long sur un item de la liste des conversations, lui, ne déclenche actuellement
 * aucun menu côté appli.)
 *
 * MàJ : depuis "feat(discussions): 3 points + barre de réactions inline sur les messages",
 * l'appui long sur son propre message en conversation ne fait plus rien (il déploie la barre
 * de réactions inline sur les messages reçus, pas sur les siens) — le menu d'actions ne
 * s'ouvre plus que via le bouton 3-points affiché à côté de la bulle. Ce test, qui vivait sur
 * la branche end_to_end_test (divergée avant ce commit), tapait encore l'ancien appui long ;
 * corrigé ici pour taper le 3-points. À vérifier sur un vrai appareil : ce bouton n'a pas
 * d'id/testTag (cf. OptionsIcon dans CommentComposeItems.kt), la coordonnée ciblée est une
 * approximation (cf. tapOptionsIconOnOwnMessage ci-dessous) — aucun émulateur disponible
 * dans la session qui a écrit ce correctif pour la valider visuellement.
 *
 * Nécessite que le compte de test E2E ait déjà au moins une conversation : ce test est
 * autoentretenu puisqu'il y écrit un message à chaque exécution.
 * Un screenshot est pris à chaque vue / action pour vérifier visuellement le déroulé.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class ConversationScenarioTest : EntourageTestAfterLogin() {

    private val device: UiDevice by lazy {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

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

    private var conversationId = -1

    @After
    fun tearDownScenario() {
        deleteLeftoverTestMessages()
        scenario.close()
    }

    /**
     * Garde-fou si le scenario echoue avant l'etape "Supprimer" : supprime par l'API les messages
     * "Unit test message ..." laisses dans la conversation (le prefixe permet aussi un menage
     * en masse cote staging).
     */
    private fun deleteLeftoverTestMessages() {
        if (conversationId < 0) return
        try {
            val request = EntourageApplication.get().apiModule.discussionsRequest
            val mine = EntourageApplication.get().me()?.id
            request.getMessagesFor(conversationId, 1, 50).execute().body()?.posts
                ?.filter { it.content?.startsWith("Unit test message") == true && it.user?.userId == mine }
                ?.forEach { post -> post.id?.let { request.deleteMessage(conversationId, it).execute() } }
        } catch (e: Exception) {
            Timber.tag("E2E").e(e, "Echec du nettoyage des messages de test")
        }
    }

    private fun openedConversationId(): Int {
        var id = -1
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            id = ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED)
                .firstOrNull { it is DetailConversationActivity }
                ?.intent?.getIntExtra(Const.ID, -1) ?: -1
        }
        return id
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
     * Cible le bouton 3-points d'un message "à moi". Depuis 6472a5c29, l'ordre en haut de
     * l'item (Row(Arrangement.End), tout packé à droite) est : OptionsIcon, puis la bulle,
     * puis l'avatar (25dp+padding) — le 3-points n'est donc plus collé à l'avatar mais à la
     * largeur, variable, de la bulle avant lui. Ce bouton n'a pas d'id/testTag : coordonnée
     * approximative (on vise assez loin de l'avatar pour laisser de la place à une bulle
     * courte), à recaler sur un vrai appareil (cf. avertissement en tête de fichier) —
     * remplace l'ancien longClickOnMessageBubble() qui visait la bulle elle-même (obsolète
     * depuis que l'appui long sur son propre message ne fait plus rien).
     */
    private fun tapOptionsIconOnOwnMessage(): ViewAction = GeneralClickAction(
        Tap.SINGLE,
        GeneralLocation.translate(GeneralLocation.TOP_RIGHT, -0.30f, 0.15f),
        Press.FINGER,
        InputDevice.SOURCE_UNKNOWN,
        MotionEvent.BUTTON_PRIMARY
    )

    private fun openOwnMessageActions() {
        onView(allOf(withId(R.id.comments), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(lastMessagePosition(), tapOptionsIconOnOwnMessage())
        )
        onIdle()
    }

    /**
     * Creation seule : ouvre la premiere conversation, envoie un message et verifie, via l'API, qu'il
     * existe cote serveur. Le message est supprime par le nettoyage (@After). Le scenario complet
     * `conversation` ci-dessous ajoute les actions du menu 3-points, dont le ciblage par coordonnees
     * est fragile et doit etre recale sur appareil.
     */
    @Test
    fun envoiDunMessage() {
        checkNoPopUpOnHome()
        onView(withId(R.id.navigation_messages)).perform(click())
        onIdle()

        val opened = device.wait(Until.hasObject(By.res("conversation_item")), 15_000)
        assumeTrue("E2E: aucune conversation dans la liste du compte de test", opened)
        device.findObject(By.res("conversation_item")).click()
        onIdle()
        conversationId = openedConversationId()

        val message = "Unit test message ${System.currentTimeMillis()}"
        typeMessageReliably(message)
        shoot("envoi_message_saisi")
        onView(withId(R.id.comment)).perform(click())
        onIdle()
        SystemClock.sleep(2_000)
        shoot("envoi_message_envoye")

        val request = EntourageApplication.get().apiModule.discussionsRequest
        val sent = request.getMessagesFor(conversationId, 1, 50).execute().body()?.posts
            ?.any { it.content == message } == true
        org.junit.Assert.assertTrue("Le message n'existe pas cote serveur", sent)
    }

    @Test
    fun conversation() {
        checkNoPopUpOnHome()
        shoot("accueil")

        onView(withId(R.id.navigation_messages)).perform(click())
        onIdle()
        shoot("liste_conversations")

        // La liste des discussions est en Compose (DiscussionsScreen) : on la pilote via UiAutomator,
        // les testTag etant exposes en resource-id (cf. DiscussionsListScenarioTest).
        val opened = device.wait(Until.hasObject(By.res("conversation_item")), 15_000)
        assumeTrue("E2E: aucune conversation dans la liste du compte de test", opened)
        device.findObject(By.res("conversation_item")).click()
        onIdle()
        conversationId = openedConversationId()
        shoot("conversation_ouverte")

        val message = "Unit test message ${System.currentTimeMillis()}"
        typeMessageReliably(message)
        shoot("message_saisi")

        onView(withId(R.id.comment)).perform(click())
        onIdle()
        shoot("message_envoye")

        // 3-points sur notre message -> menu d'actions -> Copier le texte
        openOwnMessageActions()
        shoot("menu_actions_copier")
        onView(withText(R.string.message_action_copy)).perform(click())
        onIdle()
        shoot("apres_copier")

        // 3-points -> menu d'actions -> Modifier
        openOwnMessageActions()
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

        // 3-points -> menu d'actions -> Supprimer mon message
        openOwnMessageActions()
        shoot("menu_actions_supprimer")
        onView(withText(R.string.message_action_delete)).perform(click())
        onIdle()
        shoot("message_supprime")
    }
}
