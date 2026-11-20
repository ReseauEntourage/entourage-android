package social.entourage.android.e2e

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.uiautomator.UiDevice
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.api.model.Post
import social.entourage.android.events.details.feed.EventCommentActivity
import social.entourage.android.groups.details.feed.GroupFeedActivity
import social.entourage.android.notifications.InAppNotificationsActivity
import social.entourage.android.notifications.NotificationActionManager
import social.entourage.android.notifications.PushNotificationManager

/**
 * Scénario E2E "connecté" : renvoi depuis une notification (push ou notification in-app).
 *
 * Trois choses sont vérifiées, car elles ont déjà régressé l'une au détriment de l'autre
 * (cf. b8a1e7f2e "navigation au retour d'une notification push") :
 *  1. le renvoi : taper la notification ouvre bien l'écran ciblé ;
 *  2. le scroll : le fil s'ouvre sur le post ciblé, y compris quand il est loin dans le fil ;
 *  3. le retour : le back depuis l'écran ouvert ramène sur le bon onglet de MainActivity, et la
 *     notification n'est pas rejouée en boucle (une push n'est consommée qu'une fois).
 *
 * Une push tapée est simulée comme le fait FCM quand l'app est en arrière-plan : un intent
 * ACTION_MAIN vers MainActivity dont l'extra "content" porte le JSON de la notification
 * (cf. MainActivity.checkIntentAction). Le tap d'une notification in-app appelle directement
 * NotificationActionManager.presentAction, comme InAppNotificationListFragment.
 *
 * Le groupe, l'événement et les posts sont résolus par l'API en amont ; les tests sont skippés
 * (org.junit.Assume) si le compte de test n'a pas de groupe/événement avec assez de posts.
 *
 * ATTENTION : à lancer sur un appareil/émulateur (staging, compte de test) — les contrôles de
 * scroll dépendent du contenu réel du premier groupe du compte.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class NotificationRedirectScenarioTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("notification_redirect")
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private var inAppScenario: ActivityScenario<InAppNotificationsActivity>? = null

    // Sans cette permission, la boîte de dialogue système passe devant MainActivity et ActivityScenario
    // ne la voit jamais reprendre le focus.
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @Before
    fun setUp() {
        checkUserIsLoggedIn()
        forceOnboarding(true)
    }

    @After
    fun closeScenarios() {
        inAppScenario?.close()
        finishAllActivities()
    }

    // ------------------------------------------------------------------ push : post de groupe

    /** App fermée : MainActivity est créée par la notification (onCreate). */
    @Test
    fun pushPostDeGroupeOuvreLeFilScrolleSurLePostPuisRetourSurLOngletGroupes() {
        verifyGroupPushRedirect(appInBackground = false)
    }

    /**
     * App ouverte puis passée en arrière-plan : MainActivity (singleTask) est stoppée et reçoit
     * la notification par onNewIntent — le cas le plus courant, et celui où une transaction de
     * fragments lancée trop tôt (goGroup() avant onResume) peut échouer.
     */
    @Test
    fun pushPostDeGroupeAppEnArrierePlanOuvreLeFilScrolleSurLePostPuisRetourSurLOngletGroupes() {
        verifyGroupPushRedirect(appInBackground = true)
    }

    private fun verifyGroupPushRedirect(appInBackground: Boolean) {
        val target = resolveGroupPostTarget()

        if (appInBackground) {
            // Pas d'ActivityScenario : il ne voit pas MainActivity reprendre le focus (cf. tapPush).
            context.startActivity(
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            waitForResumed(MainActivity::class.java)
            // Laisse la home finir de charger (skeleton, popups) avant de passer en arrière-plan.
            SystemClock.sleep(3_000)
            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressHome()
            SystemClock.sleep(1_000)
        }

        tapPush(
            pushTapIntent(
                instance = "neighborhood",
                instanceId = target.groupId,
                // Contrat serveur (EN-9375) pour un nouveau post de groupe : pas de post_id, la cible
                // est le chat_message_id.
                targetKey = "chat_message_id",
                postId = target.post.id!!,
                tracking = "post_on_create_to_neighborhood"
            )
        )

        // 1. renvoi
        waitForResumed(GroupFeedActivity::class.java)
        waitForFeedLoaded()
        screenshot.shoot("push_fil_du_groupe")

        // 2. scroll
        assertPostDisplayed(target.snippet)
        screenshot.shoot("push_post_cible_visible")

        // 3. back -> onglet Groupes, sans rejouer la notification
        pressBack()
        val main = waitForResumed(MainActivity::class.java)
        assertEquals("Le back doit revenir sur l'onglet Groupes", R.id.navigation_groups, selectedTab(main))
        assertNotReplayed(GroupFeedActivity::class.java)
        screenshot.shoot("push_retour_onglet_groupes")
    }

    // ------------------------------------------------------------------ notif in-app : post de groupe

    @Test
    fun notifInAppPostDeGroupeOuvreLeFilScrolleSurLePostPuisRetourSurLaListe() {
        val target = resolveGroupPostTarget()

        inAppScenario = ActivityScenario.launch(InAppNotificationsActivity::class.java)
        val inApp = waitForResumed(InAppNotificationsActivity::class.java)

        // Même appel que le clic sur une notification de la liste (InAppNotificationListFragment).
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            NotificationActionManager.presentAction(
                inApp,
                inApp.supportFragmentManager,
                "neighborhood",
                target.groupId,
                // Comme l'API (EN-9375) pour un nouveau post de groupe : post_id null, cible dans chat_message_id.
                null,
                stage = null,
                notifContext = "chat_message_created",
                chatMessageId = target.post.id
            )
        }

        waitForResumed(GroupFeedActivity::class.java)
        waitForFeedLoaded()
        assertPostDisplayed(target.snippet)
        screenshot.shoot("inapp_post_cible_visible")

        // Le back revient à la liste des notifications (l'écran d'où on est parti).
        pressBack()
        waitForResumed(InAppNotificationsActivity::class.java)
        assertNotReplayed(GroupFeedActivity::class.java)
        screenshot.shoot("inapp_retour_liste")
    }

    // ------------------------------------------------------------------ push : post d'événement

    @Test
    fun pushPostDEvenementOuvreLeFilPuisRetourSurLOngletEvenements() {
        val (eventId, post) = resolveEventPostTarget()

        tapPush(
            pushTapIntent(
                instance = "outing_post",
                instanceId = eventId,
                postId = post.id!!,
                tracking = "comment_on_create_to_outing"
            )
        )

        waitForResumed(EventCommentActivity::class.java)
        screenshot.shoot("push_post_evenement")

        pressBack()
        val main = waitForResumed(MainActivity::class.java)
        assertEquals("Le back doit revenir sur l'onglet Événements", R.id.navigation_events, selectedTab(main))
        assertNotReplayed(EventCommentActivity::class.java)
        screenshot.shoot("push_retour_onglet_evenements")
    }

    // ------------------------------------------------------------------ helpers : cibles

    private class GroupPostTarget(val groupId: Int, val post: Post, val snippet: String)

    /**
     * Premier groupe du compte et un post assez loin dans le fil (rang >= 4 de la 1re page) pour
     * que le scroll soit nécessaire. Le snippet est le début de son texte, repéré à l'écran.
     */
    private fun resolveGroupPostTarget(): GroupPostTarget {
        val api = EntourageApplication.get().apiModule.groupRequest
        val userId = EntourageApplication.get().me()?.id ?: 0
        val groups = api.getMyGroups(userId, 1, 20).execute().body()?.allGroups ?: emptyList()
        assumeTrue("E2E: le compte de test n'est membre d'aucun groupe", groups.isNotEmpty())

        for (group in groups) {
            val groupId = group.id ?: continue
            val posts = api.getGroupPosts(groupId, 1, 10).execute().body()?.posts
                ?.filter { it.status != "deleted" }
                ?: continue
            val candidates = posts.withIndex().filter { (index, post) ->
                index >= 4 && post.id != null && snippetOf(post) != null
            }
            val (_, post) = candidates.firstOrNull() ?: continue
            return GroupPostTarget(groupId, post, snippetOf(post)!!)
        }
        assumeTrue("E2E: aucun groupe du compte n'a un post au rang 5 ou plus avec un texte simple", false)
        error("unreachable")
    }

    /** Début de ligne du texte du post, sans balisage ; null s'il n'est pas exploitable. */
    private fun snippetOf(post: Post): String? {
        val firstLine = post.content?.lineSequence()?.firstOrNull { it.isNotBlank() }?.trim() ?: return null
        val snippet = firstLine.take(30).trim()
        val isPlain = snippet.length >= 8 && snippet.all { it.isLetterOrDigit() || it == ' ' || it in ".,'’!?-" }
        return snippet.takeIf { isPlain }
    }

    private fun resolveEventPostTarget(): Pair<Int, Post> {
        // Les événements dont le compte est membre : un non-membre n'a pas forcément accès aux posts.
        val api = EntourageApplication.get().apiModule.eventsRequest
        val userId = EntourageApplication.get().me()?.id ?: 0
        val events = api.getMyEvents(userId, 1, 20, null, null, null)
            .execute().body()?.allEvents ?: emptyList()
        for (event in events) {
            val eventId = event.id ?: continue
            val post = api.getEventPosts(eventId, 1, 5).execute().body()?.posts
                ?.firstOrNull { it.id != null && it.status != "deleted" }
                ?: continue
            return eventId to post
        }
        assumeTrue("E2E: aucun événement avec au moins un post pour le compte de test", false)
        error("unreachable")
    }

    // ------------------------------------------------------------------ helpers : intents et attentes

    /**
     * Tap d'une push : intent lancé par le système quand l'app est en arrière-plan. Le JSON de la
     * notification est dans l'extra "content" (PushNotificationManager.KEY_CONTENT).
     */
    private fun pushTapIntent(
        instance: String,
        instanceId: Int,
        postId: Int,
        tracking: String,
        targetKey: String = "post_id"
    ): Intent {
        val content = """
            {
              "extra": {
                "joinable_id": $instanceId,
                "joinable_type": "Entourage",
                "instance": "$instance",
                "instance_id": $instanceId,
                "$targetKey": $postId,
                "tracking": "$tracking"
              },
              "message": "Notification de test E2E"
            }
        """.trimIndent()
        return Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            putExtra(PushNotificationManager.KEY_CONTENT, content)
        }
    }

    /**
     * Tap sur la notification : le système lance MainActivity dans sa propre tâche. Pas
     * d'ActivityScenario ici, MainActivity ouvrant aussitôt l'écran ciblé par-dessus.
     */
    private fun tapPush(intent: Intent) {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun finishAllActivities() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val monitor = ActivityLifecycleMonitorRegistry.getInstance()
            Stage.values().forEach { stage ->
                monitor.getActivitiesInStage(stage).forEach { it.finish() }
            }
        }
    }

    private fun <T : Activity> currentActivity(type: Class<T>): T? {
        var found: T? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            found = ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED)
                .filterIsInstance(type)
                .firstOrNull()
        }
        return found
    }

    private fun <T : Activity> waitForResumed(type: Class<T>, timeoutMs: Long = 40_000): T {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            currentActivity(type)?.let { return it }
            SystemClock.sleep(250)
        }
        error("${type.simpleName} n'est jamais passé au premier plan")
    }

    /** Le feed reste masqué par le skeleton jusqu'à ce que le post cible soit positionné. */
    private fun waitForFeedLoaded(timeoutMs: Long = 60_000) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            var loaded = false
            val activity = currentActivity(GroupFeedActivity::class.java)
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                val skeleton = activity?.findViewById<View>(R.id.feed_skeleton_overlay)
                loaded = skeleton != null && skeleton.visibility == View.GONE
            }
            if (loaded) {
                onIdle()
                SystemClock.sleep(500)
                return
            }
            SystemClock.sleep(250)
        }
        error("Le fil du groupe est resté sur le skeleton")
    }

    private fun assertPostDisplayed(snippet: String) {
        onView(allOf(withText(containsString(snippet)), isDisplayed()))
            .check(matches(isDisplayed()))
    }

    private fun selectedTab(main: MainActivity): Int {
        var tab = -1
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            tab = main.findViewById<BottomNavigationView>(R.id.nav_view).selectedItemId
        }
        return tab
    }

    /** La push ne doit être consommée qu'une fois : après le back, l'écran ciblé ne se rouvre pas. */
    private fun <T : Activity> assertNotReplayed(type: Class<T>) {
        SystemClock.sleep(3_000)
        assertNull("${type.simpleName} s'est rouvert après le back (notification rejouée)", currentActivity(type))
    }
}
