package social.entourage.android.api

import kotlinx.serialization.SerializationException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ChatMessage
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.HomeType
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.Status
import social.entourage.android.api.model.Summary
import social.entourage.android.api.request.GroupWrapper
import social.entourage.android.api.request.MetaDataResponse
import java.util.Date
import java.util.TimeZone

/**
 * Tests de caractérisation du parsing JSON de l'API : ils figent le comportement ACTUEL (Gson)
 * pour servir de filet à la migration vers kotlinx.serialization. Les fixtures sont dans
 * src/test/resources/api/. Quand un test documente un comportement "surprenant" de Gson,
 * c'est volontaire : la migration doit soit le conserver, soit le changer explicitement.
 */
class ApiJsonCharacterizationTest {

    private lateinit var previousTimeZone: TimeZone

    @Before
    fun fixTimeZone() {
        previousTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun restoreTimeZone() = TimeZone.setDefault(previousTimeZone)

    private fun millis(iso: String): Long =
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", java.util.Locale.US).parse(iso)!!.time

    // ---------------------------------------------------------------- Events

    @Test
    fun event_parsesAllFieldKinds() {
        val e = ApiJson.decode<Events>(ApiJson.fixture("event.json"))
        assertEquals(4521, e.id)
        assertEquals("e_abc123", e.uuid_v2)
        assertEquals("Café partagé place de la République", e.title)
        assertEquals(Status.OPEN, e.status)
        assertEquals(true, e.jsonMemberPublic)
        assertEquals(false, e.online)
        assertTrue(e.member)
        assertEquals(1.5, e.distance!!, 0.0)
        assertEquals(1, e.recurrence)
        assertEquals("Place de la République, 75003 Paris", e.displayAddress)
        assertEquals(listOf("sport", "culture"), e.interests)
        assertEquals(millis("2026-03-05T10:30:00.000+0000"), e.createdAt!!.time)
        assertEquals(millis("2026-04-12T14:00:00.000+0000"), e.metadata!!.startsAt!!.time)
        assertEquals(millis("2026-04-12T16:30:00.000+0000"), e.metadata!!.endsAt!!.time)
        assertEquals(20, e.metadata!!.placeLimit)
        assertEquals("Café des Arts", e.metadata!!.placeName)
        assertEquals(48.8675, e.location!!.latitude, 0.0)
        assertEquals("Paris 3e", e.location!!.displayAddress)
        assertEquals(77, e.author!!.userID)
        assertEquals(listOf("Animateur Entourage"), e.author!!.communityRoles)
        assertTrue(e.author!!.isBirthday)
        assertEquals(2, e.members!!.size)
        assertEquals("Sam L.", e.members!![1].displayName)
        assertTrue(e.members!![1].isBirthday)
        assertNull(e.members!![1].avatarUrl)
        assertEquals(12, e.neighborhoods!![0].id)
        assertEquals("Shared coffee", e.titleTranslations!!.translation)
        assertEquals("en", e.titleTranslations!!.toLang)
    }

    @Test
    fun event_roundTrip_keepsEveryFixtureField() {
        val raw = ApiJson.fixture("event.json")
        ApiJson.assertContainsAll(raw, ApiJson.encode(ApiJson.decode<Events>(raw)))
    }

    @Test
    fun event_missingFieldsKeepKotlinDefaults() {
        val e = ApiJson.decode<Events>("""{"id": 1}""")
        assertEquals(1, e.id)
        assertNull(e.title)
        assertNull(e.status)
        assertFalse(e.member)
        assertEquals(emptyList<String>(), e.interests)
        assertEquals(emptyList<Any>(), e.members)
        assertEquals(0, e.recurrence)
    }

    @Test
    fun event_unknownKeysAreIgnored() {
        val e = ApiJson.decode<Events>("""{"id": 1, "brand_new_field": {"a": [1,2,3]}}""")
        assertEquals(1, e.id)
    }

    @Test
    fun event_unknownEnumValueBecomesNull() {
        assertNull(ApiJson.decode<Events>("""{"status": "archived"}""").status)
    }

    @Test
    fun event_statusEnumSerializesToItsSerializedName() {
        val encoded = ApiJson.encode(ApiJson.decode<Events>("""{"status": "cancelled"}"""))
        ApiJson.assertContainsAll("""{"status": "cancelled"}""", encoded)
    }

    @Test
    fun event_explicitNullFallsBackToTheKotlinDefault() {
        // Différence VOLONTAIRE avec Gson : un null explicite sur une propriété non nulle qui a une valeur
        // par défaut donne cette valeur par défaut (Gson injectait null dans un type non nul).
        val e = ApiJson.decode<Events>("""{"id": 1, "interests": null}""")
        assertEquals(emptyList<String>(), e.interests)
    }

    @Test
    fun event_numberGivenAsStringIsCoerced() {
        assertEquals(12, ApiJson.decode<Events>("""{"id": "12"}""").id)
    }

    @Test
    fun event_nullDateStaysNull() {
        assertNull(ApiJson.decode<Events>("""{"created_at": null}""").createdAt)
    }

    @Test
    fun event_malformedDateThrows() {
        try {
            ApiJson.decode<Events>("""{"created_at": "pas une date"}""")
            fail("Une date illisible doit lever une exception")
        } catch (expected: SerializationException) {
            // ok
        }
    }

    @Test
    fun event_dateRoundTripKeepsFormat() {
        val encoded = ApiJson.encode(ApiJson.decode<Events>("""{"created_at": "2026-03-05T10:30:00.000+0000"}"""))
        ApiJson.assertContainsAll("""{"created_at": "2026-03-05T10:30:00.000+0000"}""", encoded)
    }

    // ----------------------------------------------------------------- Group

    @Test
    fun group_wrapperParsesNestedGroup() {
        val g = ApiJson.decode<GroupWrapper>(ApiJson.fixture("group.json")).group
        assertEquals(12, g.id)
        assertEquals("Paris Centre", g.name)
        assertEquals("Bienvenue !", g.welcomeMessage)
        assertEquals(listOf("sport", "cuisine"), g.interests)
        assertEquals(42, g.members_count)
        assertTrue(g.member)
        assertFalse(g.national)
        assertEquals(5, g.radius)
        assertEquals("neighborhood", g.groupType)
        assertEquals("2026-01-02T03:04:05.000Z", g.createdAt) // String : jamais converti en Date
        assertEquals(3, g.unreadPostsCount)
        assertEquals(48.86, g.address!!.latitude, 0.0)
        assertEquals("Admin", g.admin!!.displayName)
        assertEquals(1, g.members!!.size)
        assertEquals(13, g.children!![0].id)
        assertEquals("Sous-groupe", g.children!![0].name)
    }

    @Test
    fun group_roundTrip_keepsEveryFixtureField() {
        val raw = ApiJson.fixture("group.json")
        val wrapper = ApiJson.decode<GroupWrapper>(raw)
        ApiJson.assertContainsAll(raw, ApiJson.encode(wrapper))
    }

    @Test
    fun group_listWrapperParsesArray() {
        val raw = """{"neighborhoods": [{"id": 1, "name": "A"}, {"id": 2, "name": "B"}]}"""
        val list = ApiJson.decode<social.entourage.android.api.request.GroupsListWrapper>(raw).allGroups
        assertEquals(listOf(1, 2), list.map { it.id })
    }

    @Test
    fun group_localOnlyFieldIsSerializedUnderItsKotlinName() {
        // `isSelected` n'a pas de @SerializedName : Gson l'écrit sous le nom du champ.
        assertTrue(ApiJson.encode(Group(id = 1)).contains("\"isSelected\":false"))
    }

    // --------------------------------------------------------------- Summary

    @Test
    fun summary_parsesCountersAndRecommendations() {
        val s = ApiJson.decode<Summary>(ApiJson.fixture("summary.json"))
        assertEquals(9, s.id)
        assertEquals(120, s.chatMessagesCount)
        assertEquals(true, s.signablePermission)
        assertEquals(false, s.association)
        assertEquals("contribution", s.preference)
        assertEquals(listOf("evt-1", "evt-2"), s.events)
        assertEquals("Modo", s.moderator!!.displayName)
        assertEquals(55, s.unclosedAction!!.id)
        assertEquals(emptyList<Any>(), s.congratulations)
        val reco = s.recommendations!!.single()
        assertEquals(HomeType.NEIGHBORHOOD, reco.homeType)
        assertEquals("show", reco.action!!.name.lowercase())
        assertEquals(99, reco.params!!.postId)
        assertEquals("n_xyz", reco.params!!.uuid)
    }

    @Test
    fun summary_roundTrip_keepsEveryFixtureField() {
        val raw = ApiJson.fixture("summary.json")
        ApiJson.assertContainsAll(raw, ApiJson.encode(ApiJson.decode<Summary>(raw)))
    }

    @Test
    fun summary_unknownHomeTypeBecomesNull() {
        val s = ApiJson.decode<Summary>("""{"recommandations": [{"name": "x", "type": "nouveau_type"}]}""")
        assertNull(s.recommendations!!.single().homeType)
    }

    // ----------------------------------------------------------- Conversation

    @Test
    fun conversation_parsesListItem() {
        val c = ApiJson.decode<Conversation>(ApiJson.fixture("conversation.json"))
        assertEquals(301, c.id)
        assertEquals("Jean M.", c.title)
        assertEquals("private", c.type)
        assertTrue(c.isOneToOne())
        assertEquals(2, c.numberUnreadMessages)
        assertEquals(2, c.memberCount)
        assertEquals(true, c.isCreator)
        assertEquals(arrayListOf("user-5"), c.blockers)
        assertEquals("À demain !", c.lastMessage!!.text)
        assertEquals(millis("2026-05-01T09:15:00.000+0000"), c.lastMessage!!.date!!.time)
        assertEquals("https://cdn.example.org/m.jpg", c.lastMessage!!.imageUrl) // clé JSON = "imageUrl"
        assertEquals("Bénévole", c.user!!.partnerRoleTitle)
        assertEquals(arrayListOf("Animateur Entourage"), c.user!!.roles)
        assertEquals("Jean M.", c.author!!.username)
        assertEquals(1, c.members!!.size)
    }

    @Test
    fun conversation_roundTrip_keepsEveryFixtureField() {
        val raw = ApiJson.fixture("conversation.json")
        ApiJson.assertContainsAll(raw, ApiJson.encode(ApiJson.decode<Conversation>(raw)))
    }

    @Test
    fun conversation_missingCountersUseDefaults() {
        val c = ApiJson.decode<Conversation>("""{"id": 1}""")
        assertEquals(0, c.numberUnreadMessages)
        assertEquals(0, c.memberCount)
        assertFalse(c.hasUnread())
    }

    // ------------------------------------------------------------ ChatMessage

    @Test
    fun chatMessage_decodesServerFields() {
        val raw = """{"content": "Salut", "id": 77, "created_at": "2026-05-01T09:15:00.000+0000",
            "message_type": "text", "user": {"id": 3, "display_name": "Jean M."}}"""
        val m = ApiJson.decode<ChatMessage>(raw)
        assertEquals("Salut", m.content)
        assertEquals(77L, m.chatId)
        assertEquals(millis("2026-05-01T09:15:00.000+0000"), m.creationDate.time)
        assertEquals("text", m.messageType)
        assertEquals("Jean M.", m.userName)
        assertEquals(3, m.userId)
        // Plus d'asymétrie @Expose(serialize = false) : ChatMessage n'est de toute façon jamais envoyé
        // en @Body (les envois passent par des corps dédiés, cf. WriteRequestFixturesTest).
    }

    // ------------------------------------------------------------------- Post

    @Test
    fun post_parsesAllFieldKinds() {
        val p = ApiJson.decode<Post>(ApiJson.fixture("post.json"))
        assertEquals(8001, p.id)
        assertEquals("Bonjour à tous !", p.content)
        assertEquals(millis("2026-02-10T18:00:00.000+0000"), p.createdTime!!.time)
        assertEquals(true, p.hasComments)
        assertEquals(4, p.commentsCount)
        assertEquals(2, p.reactionId)
        assertEquals("Admin", p.user!!.displayName)
        assertEquals(3, p.reactions!!.single().reactionsCount)
        assertEquals(listOf("Oui", "Non"), p.survey!!.choices)
        assertEquals(listOf(5, 1), p.survey!!.summary)
        assertEquals(listOf(true, false), p.surveyResponse)
        assertEquals("Neighborhood", p.autoPostFrom!!.instanceType)
        assertEquals(12, p.autoPostFrom!!.instanceId)
    }

    @Test
    fun post_roundTrip_keepsEveryFixtureField() {
        val raw = ApiJson.fixture("post.json")
        ApiJson.assertContainsAll(raw, ApiJson.encode(ApiJson.decode<Post>(raw)))
    }

    @Test
    fun post_reactionIdFalseIsTolerated() {
        // Régression connue : le back renvoie parfois `reaction_id: false` à la place de null.
        assertNull(ApiJson.decode<Post>("""{"id": 1, "reaction_id": false}""").reactionId)
    }

    @Test
    fun post_reactionIdNullOrAbsentIsNull() {
        assertNull(ApiJson.decode<Post>("""{"id": 1, "reaction_id": null}""").reactionId)
        assertNull(ApiJson.decode<Post>("""{"id": 1}""").reactionId)
    }

    @Test
    fun post_absentReactionsKeepsEmptyListDefault() {
        assertEquals(emptyList<Any>(), ApiJson.decode<Post>("""{"id": 1}""").reactions)
    }

    // ------------------------------------------------------ EntourageUser (@Expose)

    @Test
    fun entourageUser_emailAndStatusAreRead() {
        val raw = """{"id": 5, "display_name": "Admin", "email": "admin@example.org", "status": "accepted"}"""
        val u = ApiJson.decode<EntourageUser>(raw)
        assertEquals(5, u.userId)
        assertEquals("accepted", u.status)
    }

    // ----------------------------------------------------------------- Action

    @Test
    fun action_parsesAndRoundTrips() {
        val raw = ApiJson.fixture("action.json")
        val a = ApiJson.decode<Action>(raw)
        assertEquals(612, a.id)
        assertEquals("Don de vêtements chauds", a.title)
        assertEquals("clothes", a.sectionName)
        assertEquals("contribution", a.actionType)
        assertFalse(a.isDemand())
        assertFalse(a.isCancel())
        assertEquals(0.8, a.distance!!, 0.0)
        assertEquals(true, a.autoPostAtCreate)
        assertEquals(millis("2026-04-02T12:00:00.000+0000"), a.updatedAt!!.time)
        assertEquals("Lyon", a.location!!.displayAddress)
        assertEquals("Warm clothes", a.titleTranslations!!.translation)
        assertNotNull(a.createdAt)
        ApiJson.assertContainsAll(raw, ApiJson.encode(a))
    }

    @Test
    fun action_privateStatusIsSerialized() {
        val a = ApiJson.decode<Action>("""{"status": "closed"}""")
        assertTrue(a.isCancel())
        ApiJson.assertContainsAll("""{"status": "closed"}""", ApiJson.encode(a))
    }

    // --------------------------------------------- MetaData (capture réelle preprod)

    @Test
    fun metadata_realResponse_parsesTagsAndReactions() {
        // metadata.json = vraie réponse de GET home/metadata (référentiels publics, sans donnée perso).
        val m = ApiJson.decode<MetaDataResponse>(ApiJson.fixture("metadata.json"))
        assertEquals(
            listOf("social", "services", "clothes", "equipment", "hygiene"),
            m.tags.sections!!.map { it.id }
        )
        assertEquals("Temps de partage", m.tags.sections!![0].name)
        assertEquals("café, activité...", m.tags.sections!![0].subname)
        assertEquals("activites", m.tags.interests!![0].id)
        assertEquals(5, m.reactions.size)
        assertEquals(2, m.reactions[0].id)
        assertEquals("thumbup", m.reactions[0].key)
        assertTrue(m.reactions[0].imageUrl!!.startsWith("https://"))
    }

    @Test
    fun metadata_realResponse_topLevelListsAbsentFromServerAreEmpty() {
        // Le serveur n'envoie PAS interests/involvements/concerns/orientations à la racine (ils sont dans
        // `tags`), alors que MetaDataResponse les déclare. Avant la migration Gson les laissait à null
        // (type non nul violé, d'où l'ancien log "wtf orientations null") ; ils valent maintenant une liste
        // vide, et une réponse sans ces clés ne fait plus perdre `tags` ni `reactions`.
        val m = ApiJson.decode<MetaDataResponse>(ApiJson.fixture("metadata.json"))
        assertEquals(emptyList<Any>(), m.interests)
        assertEquals(emptyList<Any>(), m.orientations)
        assertEquals(emptyList<Any>(), m.concerns)
        assertEquals(emptyList<Any>(), m.involvements)
    }
}
