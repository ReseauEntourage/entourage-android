package social.entourage.android.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Call
import social.entourage.android.EntourageApplication
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.api.OnboardingAPI
import timber.log.Timber
import java.io.IOException

/**
 * Smoke test de lecture : appelle les principaux endpoints GET de l'API (preprod) avec le compte de
 * test, à travers le VRAI pipeline Retrofit + kotlinx.serialization, et vérifie que chaque réponse 2xx
 * se décode. Une réponse qui ne correspond plus aux modèles (champ requis manquant, type inattendu...)
 * fait échouer le test avec le nom de l'endpoint — alors que l'app, elle, avale ces erreurs dans
 * `onFailure`. Lecture seule : rien n'est créé ni modifié.
 *
 * Les statuts 4xx/5xx ne sont pas des échecs de ce test (endpoint indisponible, compte sans données) ;
 * seuls les échecs de décodage le sont.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class ApiReadEndpointsSmokeTest : EntourageTestAfterLogin() {

    private class Outcome(val name: String, val status: String, val failure: Throwable? = null)

    private val outcomes = mutableListOf<Outcome>()
    private val api get() = EntourageApplication.get().apiModule

    @Before
    fun login() {
        if (!EntourageApplication.get().authenticationController.isAuthenticated) {
            OnboardingAPI.getInstance().syncLogin(E2ECredentials.PHONE, E2ECredentials.PASSWORD) { ok, _, _ ->
                if (!ok) throw Exception("E2E: la connexion prealable a echoue")
            }
        }
    }

    /** Exécute l'appel ; renvoie le corps décodé (ou null si statut non 2xx / échec). */
    private fun <T> call(name: String, block: () -> Call<T>): T? = try {
        val response = block().execute()
        outcomes += Outcome(name, "HTTP ${response.code()}")
        if (response.isSuccessful) response.body() else null
    } catch (e: IOException) {
        outcomes += Outcome(name, "réseau: ${e.javaClass.simpleName}") // pas un problème de modèle
        null
    } catch (e: Throwable) {
        outcomes += Outcome(name, "ÉCHEC", e)
        null
    }

    @Test
    fun principauxEndpointsDeLectureSeDecodent() {
        val me = EntourageApplication.get().me()
        val myId = me?.id ?: 0
        val lat = 48.8566
        val lng = 2.3522

        // --- Accueil / référentiels
        call("home/summary") { api.homeRequest.getSummary() }
        call("home/metadata") { api.metaDataRequest.getMetaData() }
        call("resources") { api.homeRequest.getPedagogicalResources(true) }
        call("resources/home") { api.homeRequest.getInitialPedagogicalResources(true) }
        call("resources/welcome") { api.homeRequest.getWelcomeResource() }
        call("inapp_notifications/count") { api.homeRequest.getNotificationsCount() }
        call("inapp_notifications") { api.homeRequest.getNotifications(1, 10) }
        call("notification_permissions") { api.homeRequest.getNotificationsPermissions() }
        call("entourage_images") { api.eventsRequest.getEventsImages() }
        call("neighborhood_images") { api.groupRequest.getGroupImages() }
        call("suggestions") { api.suggestionRequest.getSuggestions() }
        call("sharing/groups") { api.sharingRequest.retrieveSharing() }

        // --- Utilisateur
        call("users/{me}") { api.userRequest.getUser(myId.toString()) }
        call("users/unread") { api.userRequest.getUnreadCountForUser() }
        call("user_blocked_users") { api.discussionsRequest.getBlockedUsers() }
        call("partners") { api.associationsRequest.getAllAssociations() }

        // --- Entraide
        call("contributions") { api.actionsRequest.getAllActionsContrib(1, 10, null, 100, lat, lng) }
        call("solicitations") { api.actionsRequest.getAllActionsDemand(1, 10, null, 100, lat, lng) }
        call("users/me/actions") { api.actionsRequest.getMyActions(1, 10) }
        call("contributions?q") { api.actionsRequest.getAllContribsWithSearchQuery("a", 1, 10) }

        // --- Groupes
        val myGroups = call("users/{me}/neighborhoods") { api.groupRequest.getMyGroups(myId, 1, 20) }?.allGroups
        call("neighborhoods") { api.groupRequest.getAllGroups(1, 10) }
        call("neighborhoods/national") { api.groupRequest.getNationalGroups() }
        call("users/me/neighborhoods/default") { api.groupRequest.getDefautGroups() }
        val groupId = myGroups?.firstOrNull()?.id
        if (groupId != null) {
            call("neighborhoods/{id}") { api.groupRequest.getGroup(groupId) }
            call("neighborhoods/{id}/users") { api.groupRequest.getMembers(groupId, 1, 20) }
            call("neighborhoods/{id}/outings") { api.groupRequest.getGroupEvents(groupId) }
            val posts = call("neighborhoods/{id}/chat_messages") { api.groupRequest.getGroupPosts(groupId, 1, 20) }?.posts
            val postId = posts?.firstOrNull()?.id
            if (postId != null) {
                call("neighborhoods/{id}/chat_messages/{post}") { api.groupRequest.getPostDetail(groupId, postId, "high") }
                call("neighborhoods/{id}/chat_messages/{post}/comments") { api.groupRequest.getPostComments(groupId, postId) }
                call("neighborhoods/{id}/chat_messages/{post}/reactions/users") {
                    api.groupRequest.getDetailsReactionGroupPost(groupId, postId)
                }
            }
        }

        // --- Événements
        val events = call("outings") { api.eventsRequest.getAllEvents(1, 10, 100, lat, lng, "future") }?.allEvents
        call("users/{me}/outings") { api.eventsRequest.getMyEvents(myId, 1, 10, 100, lat, lng) }
        call("outings/week_average") { api.eventsRequest.getEventsWeekAverage(lat, lng, 100) }
        call("outings/papotages") { api.eventsRequest.getEventsPapotages() }
        call("outings/webinar") { api.eventsRequest.getEventsWebinar() }
        call("outings/firsts_steps") { api.eventsRequest.getEventListWelcome() }
        events?.firstOrNull()?.id?.let { eventId ->
            call("outings/{id}") { api.eventsRequest.getEvent(eventId.toString()) }
            call("outings/{id}/users") { api.eventsRequest.getMembers(eventId, 1, 20) }
            call("outings/{id}/chat_messages") { api.eventsRequest.getEventPosts(eventId, 1, 20) }
        }

        // --- Discussions
        val conversations = call("conversations") { api.discussionsRequest.getAllConversations(1, 20) }?.allConversations
        call("conversations/privates") { api.discussionsRequest.getPrivateConversations(1, 20) }
        call("conversations/outings") { api.discussionsRequest.getOutingConversations(1, 20) }
        call("conversations/memberships") { api.discussionsRequest.getConversationMemberships(null, 1, 20) }
        conversations?.firstOrNull()?.id?.let { convId ->
            call("conversations/{id}") { api.discussionsRequest.getDetailConversation(convId) }
            call("conversations/{id}/chat_messages") { api.discussionsRequest.getMessagesFor(convId, 1, 20) }
            call("conversations/{id}/users") { api.discussionsRequest.getUsersForConversation(convId) }
            call("conversations/{id}/images") { api.discussionsRequest.getConversationImages(convId) }
        }

        // --- Petits groupes de discussion
        call("user_smalltalks") { api.smallTalkRequest.listUserSmallTalkRequests() }
        call("user_smalltalks/almost_matches") { api.smallTalkRequest.listAlmostMatches() }
        call("smalltalks") { api.smallTalkRequest.listSmallTalks(1, 10) }

        val failures = outcomes.filter { it.failure != null }
        outcomes.forEach { Timber.tag("E2E").i("API read: %s -> %s", it.name, it.status) }
        assertTrue(
            "Endpoints dont la réponse ne se décode plus :\n" +
                failures.joinToString("\n") { "- ${it.name}: ${it.failure}" },
            failures.isEmpty()
        )
        // Garde-fou : le test doit avoir réellement lu des données (pas seulement enchaîné des 4xx).
        assertTrue("Trop peu d'endpoints en 2xx", outcomes.count { it.status == "HTTP 200" } >= 15)
    }
}
