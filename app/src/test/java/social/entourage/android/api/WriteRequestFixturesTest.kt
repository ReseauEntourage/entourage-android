package social.entourage.android.api

import androidx.collection.ArrayMap
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ActionCancel
import social.entourage.android.api.model.Address
import social.entourage.android.api.model.Group
import social.entourage.android.api.request.ContribCancelWrapper
import social.entourage.android.api.request.ContribWrapper
import social.entourage.android.api.request.CreateEventWrapper
import social.entourage.android.api.request.DemandCancelWrapper
import social.entourage.android.api.request.DemandWrapper
import social.entourage.android.api.request.GroupWrapper
import social.entourage.android.events.create.CreateEventForm
import java.util.TimeZone

/**
 * Fixtures "en écriture" : chaque fichier de src/test/resources/api/requests/ est un corps de requête
 * RÉEL capturé dans les logs OkHttp lors des scénarios e2e de création sur staging (identifiants et
 * horodatages remplacés par des valeurs fixes). Le test reconstruit le même payload avec le code de
 * production et vérifie que le JSON produit est identique (comparaison structurelle, indépendante
 * de l'ordre des clés). Après la migration vers kotlinx.serialization, ces tests garantissent que le
 * serveur reçoit exactement les mêmes corps qu'avant.
 */
class WriteRequestFixturesTest {

    private lateinit var previousTimeZone: TimeZone

    @Before
    fun fixTimeZone() {
        previousTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"))
    }

    @After
    fun restoreTimeZone() = TimeZone.setDefault(previousTimeZone)

    private fun assertMatchesFixture(fixture: String, payload: Any) {
        assertEquals(
            "Corps de requête différent de la fixture $fixture",
            ApiJson.tree(ApiJson.fixture("requests/$fixture")),
            ApiJson.tree(ApiJson.encode(payload))
        )
    }

    private fun map() = ArrayMap<String, Any>()

    // -------------------------------------------------------------- création

    @Test
    fun createGroup() {
        // CreateGroupFragment -> GroupPresenter.createGroup(GroupWrapper(group))
        val group = Group().name("Unit test group 1").description("Groupe cree automatiquement par le test E2E.")
        group.interests = mutableListOf("activites")
        group.latitude = 48.8566
        group.longitude = 2.3522
        group.displayAddress = "Paris - 75004"
        group.neighborhoodImageId = 15
        assertMatchesFixture("create_group.json", GroupWrapper(group))
    }

    @Test
    fun createGroupPost_htmlContent() {
        // CreatePostActivity : le texte saisi est converti en HTML avant envoi.
        val html = "<p dir=\"ltr\" style=\"margin-top:0; margin-bottom:0;\">Unit test post 1</p>\n"
        val messageChat = map().apply { put("content", html) }
        val request = map().apply { put("chat_message", messageChat) }
        assertMatchesFixture("create_group_post.json", request)
    }

    private fun action(title: String) = Action(
        title = title,
        description = "Action creee automatiquement par le test E2E.",
        sectionName = "social",
        location = Address(48.8566, 2.3521983, ""),
        autoPostAtCreate = false,
        hasConsent = true
    )

    @Test
    fun createDemand() = assertMatchesFixture("create_demand.json", DemandWrapper(action("Unit test Demande 1")))

    @Test
    fun createContribution() =
        assertMatchesFixture("create_contribution.json", ContribWrapper(action("Unit test Contribution 1")))

    @Test
    fun createEvent() {
        val form = CreateEventForm(
            title = "Unit test Event 1",
            description = "Evenement cree automatiquement par le test E2E.",
            entourageImageId = 32,
            date = "2026-10-21",
            startTime = "10:00",
            endTime = "12:00",
            online = true,
            eventUrl = "https://www.entourage.social/",
            interests = mutableListOf("activites"),
        )
        assertMatchesFixture(
            "create_event.json",
            CreateEventWrapper(form.toCreateEvent(datePattern = "yyyy-MM-dd HH:mm:ss Z"))
        )
    }

    @Test
    fun createConversation() {
        val userParam = map().apply { put("user_id", "42") }
        assertMatchesFixture("create_conversation.json", map().apply { put("conversation", userParam) })
    }

    @Test
    fun conversationMessage() {
        val messageChat = map().apply { put("content", "Unit test message 1") }
        assertMatchesFixture("conversation_message.json", map().apply { put("chat_message", messageChat) })
    }

    // ----------------------------------------------------- annulation / profil

    @Test
    fun cancelDemand() = assertMatchesFixture("cancel_demand.json", DemandCancelWrapper(ActionCancel(outcome = true)))

    @Test
    fun cancelContribution() =
        assertMatchesFixture("cancel_contribution.json", ContribCancelWrapper(ActionCancel(outcome = true)))

    @Test
    fun patchUser_willingToEngageLocally() {
        // UserPresenter : user["willing_to_engage_locally"] = willing
        val user = map().apply { put("willing_to_engage_locally", false) }
        assertMatchesFixture("patch_user_engage.json", map().apply { put("user", user) })
    }
}
