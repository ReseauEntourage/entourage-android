package social.entourage.android.e2e

import android.content.Intent
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import androidx.core.os.bundleOf
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
import androidx.test.espresso.IdlingPolicies
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.comment.CommentActivity
import social.entourage.android.groups.details.feed.GroupCommentActivity
import social.entourage.android.tools.utils.Const
import java.util.concurrent.TimeUnit

/**
 * Scénario E2E "connecté", commentaires de publication (groupe) — pendant de
 * ConversationScenarioTest pour les posts de groupe : poster un commentaire -> 3-points sur
 * son propre commentaire (Modifier, Supprimer), et, si un commentaire d'un autre auteur existe
 * déjà sur ce post, appui long dessus pour ouvrir la barre de réactions inline et poser/retirer
 * une réaction.
 *
 * On lance directement GroupCommentActivity (comme le fait la notification "nouveau commentaire",
 * cf. Navigation.kt HomeType.NEIGHBORHOOD_POST) sur le premier post du premier groupe dont le
 * compte de test est membre, plutôt que de naviguer Groupes -> liste -> post : cette navigation
 * passe par un graphe Navigation Component séparé (R.navigation.groups_feed, cf.
 * GroupFeedActivity) non exercé ici, et le groupe/post utilisé n'est pas connu à l'avance.
 * Le groupe et le post sont donc résolus par un appel synchrone à l'API en amont ; le test est
 * skippé (org.junit.Assume) si le compte n'est membre d'aucun groupe ou si ce groupe n'a aucun
 * post — préconditions non garanties, cf. CLAUDE.md.
 *
 * ATTENTION : écrit sans émulateur/appareil Android disponible pour vérifier les coordonnées
 * d'appui (le bouton 3-points n'a pas d'id/testTag, cf. OptionsIcon dans
 * CommentComposeItems.kt) — à lancer et ajuster sur un vrai appareil avant de merger. Suppose
 * aussi que la branche end_to_end_test a été resynchronisée avec develop : elle en divergeait
 * avant ce commit (le bouton 3-points et la barre de réactions inline pour les commentaires de
 * groupe n'existaient pas encore côté app sur cette branche).
 *
 * Auto-entretenu : supprime le commentaire qu'il crée ; retire toute réaction qu'il pose.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class GroupCommentScenarioTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("group_comment")

    private lateinit var scenario: ActivityScenario<GroupCommentActivity>
    private var groupId: Int = 0
    private var postId: Int = 0

    @Before
    fun setUp() {
        ensureLoggedIn()
        IdlingPolicies.setMasterPolicyTimeout(180, TimeUnit.SECONDS)
        IdlingPolicies.setIdlingResourceTimeout(180, TimeUnit.SECONDS)

        val userId = EntourageApplication.get().me()?.id ?: 0
        val myGroups = EntourageApplication.get().apiModule.groupRequest
            .getMyGroups(userId, 1, 20)
            .execute()
            .body()
            ?.allGroups
            ?: emptyList()
        assumeTrue("E2E: le compte de test n'est membre d'aucun groupe", myGroups.isNotEmpty())
        val group = myGroups.first()
        groupId = group.id ?: 0

        val posts = EntourageApplication.get().apiModule.groupRequest
            .getGroupPosts(groupId, 1, 20)
            .execute()
            .body()
            ?.posts
            ?: emptyList()
        assumeTrue("E2E: aucun post dans le premier groupe du compte de test", posts.isNotEmpty())
        val post = posts.first()
        postId = post.id ?: 0

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, GroupCommentActivity::class.java).putExtras(
            bundleOf(
                Const.ID to groupId,
                Const.POST_ID to postId,
                Const.POST_AUTHOR_ID to (post.user?.userId ?: 0),
                Const.IS_MEMBER to true,
                Const.NAME to group.name
            )
        )
        scenario = ActivityScenario.launch(intent)
        scenario.onActivity { activity -> super.setUp(activity) }
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

    // cf. ConversationScenarioTest.shoot : même précaution focus-fenêtre après capture.
    private fun shoot(label: String) {
        screenshot.shoot(label)
        SystemClock.sleep(500)
    }

    private fun currentActivity(): CommentActivity? {
        var activity: CommentActivity? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            activity = ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance<CommentActivity>()
                .firstOrNull()
        }
        return activity
    }

    private fun lastMessagePosition(): Int = (currentActivity()?.commentsList?.size ?: 1) - 1

    private fun currentMessageEditTextContent(): String {
        var text = ""
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            text = currentActivity()?.binding?.commentMessage?.text?.toString() ?: ""
        }
        return text
    }

    // cf. ConversationScenarioTest.typeMessageReliably : même contournement du typeText()
    // qui ne saisit parfois que le premier caractère sur cet environnement.
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
     * Cible le bouton 3-points d'un message "à moi" (bulle alignée à droite, 3-points entre
     * la bulle et l'avatar, en haut de l'item — cf. MessageBubbleItem : Row(Arrangement.End)
     * { bulle ; OptionsIcon(top=8dp) ; Avatar(25dp+padding) }). Approximatif : le 3-points n'a
     * pas d'id/testTag, à recaler sur un vrai appareil (cf. avertissement en tête de fichier).
     */
    private fun tapOptionsIconOnOwnMessage(): ViewAction = GeneralClickAction(
        Tap.SINGLE,
        GeneralLocation.translate(GeneralLocation.TOP_RIGHT, -0.12f, 0.15f),
        Press.FINGER,
        InputDevice.SOURCE_UNKNOWN,
        MotionEvent.BUTTON_PRIMARY
    )

    /** Appui long sur la bulle d'un message reçu (avatar à gauche, bulle au centre) pour
     * déployer la barre de réactions inline — cf. MessageBubbleItem.handleLongPress. */
    private fun longClickOnReceivedMessageBubble(): ViewAction = GeneralClickAction(
        Tap.LONG,
        GeneralLocation.CENTER,
        Press.FINGER,
        InputDevice.SOURCE_UNKNOWN,
        MotionEvent.BUTTON_PRIMARY
    )

    @Test
    fun editAndDeleteOwnComment() {
        shoot("post_ouvert")

        val message = "Message test e2e ${System.currentTimeMillis()}"
        typeMessageReliably(message)
        shoot("commentaire_saisi")

        onView(withId(R.id.comment)).perform(click())
        onIdle()
        shoot("commentaire_envoye")

        onView(allOf(withId(R.id.comments), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(lastMessagePosition(), tapOptionsIconOnOwnMessage())
        )
        onIdle()
        shoot("menu_actions_modifier")
        onView(withText(R.string.message_action_edit)).perform(click())
        onIdle()
        shoot("mode_edition")

        val messageModifie = "$message (modifie)"
        typeMessageReliably(messageModifie)
        onView(withId(R.id.comment)).perform(click())
        onIdle()
        shoot("commentaire_modifie")

        onView(allOf(withId(R.id.comments), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(lastMessagePosition(), tapOptionsIconOnOwnMessage())
        )
        onIdle()
        shoot("menu_actions_supprimer")
        onView(withText(R.string.message_action_delete)).perform(click())
        onIdle()
        shoot("commentaire_supprime")
    }

    @Test
    fun reactToAnotherAuthorsComment() {
        val meId = EntourageApplication.get().me()?.id
        val comments = EntourageApplication.get().apiModule.groupRequest
            .getPostComments(groupId, postId)
            .execute()
            .body()
            ?.posts
            ?: emptyList()
        val othersCommentIndex = comments.indexOfFirst { it.user?.userId != meId }
        assumeTrue(
            "E2E: aucun commentaire d'un autre auteur sur ce post pour tester la réaction",
            othersCommentIndex >= 0
        )

        shoot("post_ouvert_reaction")

        // Position dans l'adapter = index dans commentsList + 1 pour le post parent affiché
        // en position 0 (cf. CommentActivity.parentPostOffset).
        val adapterPosition = othersCommentIndex + 1
        onView(allOf(withId(R.id.comments), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(adapterPosition, longClickOnReceivedMessageBubble())
        )
        onIdle()
        shoot("barre_reactions_ouverte")

        // Les icônes de réaction n'ont pas de texte/id distinct (cf. ReactionPickerRow) :
        // on tape le premier type de réaction disponible en ciblant le début de la barre,
        // affichée juste sous la bulle du message reçu (avatar à gauche).
        onView(allOf(withId(R.id.comments), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(
                adapterPosition,
                GeneralClickAction(
                    Tap.SINGLE,
                    GeneralLocation.translate(GeneralLocation.BOTTOM_LEFT, 0.15f, -0.05f),
                    Press.FINGER,
                    InputDevice.SOURCE_UNKNOWN,
                    MotionEvent.BUTTON_PRIMARY
                )
            )
        )
        onIdle()
        shoot("reaction_posee")

        // Retaper la même réaction la retire (toggle, cf. onMessageReactionClicked) :
        // nettoyage pour laisser le commentaire de l'autre auteur inchangé.
        onView(allOf(withId(R.id.comments), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(adapterPosition, longClickOnReceivedMessageBubble())
        )
        onIdle()
        onView(allOf(withId(R.id.comments), isDisplayed())).perform(
            actionOnItemAtPosition<ViewHolder>(
                adapterPosition,
                GeneralClickAction(
                    Tap.SINGLE,
                    GeneralLocation.translate(GeneralLocation.BOTTOM_LEFT, 0.15f, -0.05f),
                    Press.FINGER,
                    InputDevice.SOURCE_UNKNOWN,
                    MotionEvent.BUTTON_PRIMARY
                )
            )
        )
        onIdle()
        shoot("reaction_retiree")
    }
}
