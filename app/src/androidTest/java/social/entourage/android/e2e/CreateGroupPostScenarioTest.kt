package social.entourage.android.e2e

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.groups.details.feed.CreatePostGroupActivity
import social.entourage.android.tools.utils.Const
import timber.log.Timber

/**
 * Scénario E2E "connecté" : publie réellement un post texte dans un groupe dont le compte de test
 * est membre (écran CreatePostGroupActivity -> bouton Publier), puis vérifie via l'API que le post
 * existe côté serveur.
 *
 * Le groupe est résolu par un appel API en amont ; le test est skippé (Assume) si le compte n'est
 * membre d'aucun groupe. Auto-nettoyant : le post créé est supprimé par `deletePost`.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class CreateGroupPostScenarioTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("publication_groupe")

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    companion object {
        private const val NAME_PREFIX = "Unit test post "
    }

    private var groupId = 0
    private var marker: String? = null

    private fun myTestPosts() = EntourageApplication.get().apiModule.groupRequest
        .getGroupPosts(groupId, 1, 50).execute().body()?.posts
        ?.filter { it.content?.contains(NAME_PREFIX) == true } ?: emptyList()

    private fun findPostId(): Int? = myTestPosts().firstOrNull { it.content?.contains(marker ?: "") == true }?.id

    @After
    fun cleanupCreatedPost() {
        if (marker == null || groupId == 0) return
        try {
            // Supprime aussi les restes d'exécutions précédentes interrompues ("Unit test post ...").
            myTestPosts().forEach { post ->
                post.id?.let { EntourageApplication.get().apiModule.groupRequest.deletePost(groupId, it).execute() }
            }
        } catch (e: Exception) {
            Timber.tag("E2E").e(e, "Echec du nettoyage du post cree par le test")
        }
    }

    @Test
    fun publicationDansUnGroupe() {
        checkUserIsLoggedIn()
        val userId = EntourageApplication.get().me()?.id ?: 0
        val groups = EntourageApplication.get().apiModule.groupRequest
            .getMyGroups(userId, 1, 20).execute().body()?.allGroups ?: emptyList()
        assumeTrue("E2E: le compte de test n'est membre d'aucun groupe", groups.isNotEmpty())
        groupId = groups.first().id ?: 0

        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, CreatePostGroupActivity::class.java).apply {
            putExtra(Const.ID, groupId)
        }
        val scenario = ActivityScenario.launch<CreatePostGroupActivity>(intent)
        scenario.onActivity { activity -> setUp(activity) }
        screenshot.shoot("ecran_vide")

        marker = "$NAME_PREFIX${System.currentTimeMillis()}"
        onView(allOf(withId(R.id.message), isDisplayed())).perform(replaceText(marker!!), closeSoftKeyboard())
        screenshot.shoot("texte_saisi")

        onView(withId(R.id.validate)).perform(click())
        onIdle()
        Thread.sleep(2_000) // laisse le temps à la réponse serveur avant de vérifier
        screenshot.shoot("publie")

        assertTrue("Le post n'existe pas côté serveur", findPostId() != null)
        scenario.close()
    }

    /**
     * Mode édition (options d'une publication -> Modifier) : même écran que la création, mais avec
     * le titre « Modifier une publication » et le CTA « Modifier » (et non « Créer une publication »
     * / « Partager »). Ne valide rien : aucun appel serveur, rien à nettoyer.
     */
    @Test
    fun editionAfficheLesLibellesDeModification() {
        checkUserIsLoggedIn()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, CreatePostGroupActivity::class.java).apply {
            putExtra(Const.ID, 1)
            putExtra(Const.POST_ID, 1)
            putExtra(Const.POST_CONTENT_HTML, "<p>Unit test édition</p>")
        }
        val scenario = ActivityScenario.launch<CreatePostGroupActivity>(intent)
        scenario.onActivity { activity -> setUp(activity) }
        onIdle()

        onView(withText(R.string.edit_post)).check(matches(isDisplayed()))
        onView(withText(R.string.edit)).check(matches(isDisplayed()))
        onView(withText(R.string.create_post)).check(doesNotExist())
        onView(withText(R.string.share_post)).check(doesNotExist())
        screenshot.shoot("edition")
        scenario.close()
    }
}
