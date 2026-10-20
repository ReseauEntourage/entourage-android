package social.entourage.android.api

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import social.entourage.android.api.model.User
import social.entourage.android.api.request.LoginResponse
import social.entourage.android.api.request.UserResponse
import java.util.TimeZone

/** Caractérisation du parsing de [User] (réponse de login, users/me, users/{id}). */
class UserJsonCharacterizationTest {

    private lateinit var previousTimeZone: TimeZone

    @Before
    fun fixTimeZone() {
        previousTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun restoreTimeZone() = TimeZone.setDefault(previousTimeZone)

    private fun user(): User = ApiJson.decode<LoginResponse>(ApiJson.fixture("login.json")).user

    @Test
    fun user_parsesIdentityAndProfile() {
        val u = user()
        assertEquals(4079, u.id)
        assertEquals("test@example.org", u.email)
        assertEquals("Test", u.firstName)
        assertEquals("Utilisateur", u.lastName)
        assertEquals("Test U.", u.displayName)
        assertEquals("1990-05-17", u.birthday)
        assertEquals("female", u.gender)
        assertEquals("fake-token-0000", u.token) // clé JSON = nom du champ ("token")
        assertEquals("Bénévole depuis 2024", u.about)
        assertEquals("offer_help", u.goal)
        assertTrue(u.isEngaged)
        assertEquals(3, u.unreadCount)
        assertEquals(15, u.travelDistance)
        assertEquals(true, u.willingToEngageLocally)
        assertFalse(u.isBirthday)
        assertEquals(1705311000000L, u.createdAt!!.time)
    }

    @Test
    fun user_parsesListsMapsAndNestedObjects() {
        val u = user()
        assertEquals(arrayListOf("sport", "cuisine"), u.interests)
        assertEquals(arrayListOf("event"), u.involvements)
        assertEquals(arrayListOf("share"), u.orientations)
        assertEquals(arrayListOf("isolation"), u.concerns)
        assertEquals(arrayListOf("Animateur Entourage"), u.roles)

        assertEquals(listOf("09:00-12:00", "14:00-18:00"), u.availability["1"])
        assertEquals(listOf("10:00-13:00"), u.availability["6"])
        assertEquals("75003", u.firebaseProperties!!["ActionZoneCP"])
        assertTrue(u.isCreateEventActive()) // permissions.outing.creation

        assertEquals(48.8675, u.address!!.latitude, 0.0)
        assertEquals("Paris 3e", u.address!!.displayAddress)
        assertEquals("Lyon", u.addressSecondary!!.displayAddress)

        assertEquals(7, u.stats!!.askCreationCount)
        assertEquals(5, u.stats!!.outingsCount)
        assertTrue(u.stats!!.isGoodWavesValidated)
        assertEquals("Asso Test", u.organization!!.name)
        assertEquals("https://cdn.example.org/org.png", u.organization!!.largeLogoUrl)

        assertEquals(12L, u.partner!!.id)
        assertEquals("Référent", u.partner!!.userRoleTitle)
        assertTrue(u.partner!!.isFollowing)
        assertFalse(u.partner!!.isDefault)

        val badge = u.badges!!.single()
        assertEquals("first_event", badge.name)
        assertTrue(badge.active)
        assertEquals(5, badge.metadata!!.target)
    }

    @Test
    fun user_minimalPayloadKeepsKotlinDefaults() {
        val u = ApiJson.decode<UserResponse>("""{"user": {"id": 7}}""").user
        assertEquals(7, u.id)
        assertEquals("", u.about)
        assertEquals(emptyList<String>(), u.interests)
        assertTrue(u.availability.isEmpty())
        assertNull(u.stats)
        assertNull(u.token)
        assertFalse(u.isEngaged)
        assertFalse(u.isCreateEventActive())
    }

    @Test
    fun user_aboutNullIsExposedAsEmptyString() {
        val u = ApiJson.decode<UserResponse>("""{"user": {"id": 7, "about": null}}""").user
        assertEquals("", u.about)
    }

    @Test
    fun user_unknownKeysAreIgnored() {
        val u = ApiJson.decode<UserResponse>("""{"user": {"id": 7, "nouveau": {"x": 1}}, "meta": 1}""").user
        assertEquals(7, u.id)
    }

    @Test
    fun user_encodingKeepsPhoneSmsCodeAndTokenForLocalPersistence() {
        // Avant la migration, @Expose(serialize = false) les excluait de la config Retrofit mais PAS de la
        // persistance locale (Gson nu). Un `User` n'est jamais envoyé en @Body (les mises à jour passent par
        // `arrayMapForUpdate`) : ces champs sont donc toujours sérialisés, ce que la persistance exige
        // (le token sert à rester connecté).
        val u = user()
        u.phone = "0600000000"
        u.smsCode = "654321"
        val encoded = ApiJson.encode(u)
        assertTrue(encoded.contains("0600000000"))
        assertTrue(encoded.contains("654321"))
        assertTrue(encoded.contains("fake-token-0000"))
    }

    @Test
    fun user_apiEncodingKeepsProfileFieldsUnderServerKeys() {
        val encoded = ApiJson.encode(user())
        ApiJson.assertContainsAll(
            """{"id": 4079, "email": "test@example.org", "first_name": "Test", "last_name": "Utilisateur",
                "display_name": "Test U.", "birthdate": "1990-05-17", "gender": "female",
                "interests": ["sport", "cuisine"], "orientations": ["share"], "goal": "offer_help",
                "engaged": true, "travel_distance": 15, "is_birthday": false,
                "availability": {"1": ["09:00-12:00", "14:00-18:00"], "6": ["10:00-13:00"]},
                "address": {"latitude": 48.8675, "longitude": 2.3636, "display_address": "Paris 3e"},
                "address_2": {"latitude": 45.76, "longitude": 4.83, "display_address": "Lyon"},
                "stats": {"entourage_count": 1, "actions_count": 2, "outings_count": 5},
                "badges": [{"name": "first_event", "active": true, "awarded_at": "2025-02-01",
                            "metadata": {"target": 5, "current": 2}}]}""",
            encoded.let { ApiJson.tree(it).toString() }
        )
    }

    @Test
    fun user_arrayMapForUpdate_buildsPatchBody() {
        val u = user()
        u.smsCode = "654321"
        val body = ApiJson.encode(u.arrayMapForUpdate)
        ApiJson.assertContainsAll(
            """{"first_name": "Test", "last_name": "Utilisateur", "email": "test@example.org",
                "sms_code": "654321", "about": "Bénévole depuis 2024"}""",
            body
        )
    }

    @Test
    fun user_arrayMapForUpdate_omitsEmailAndSmsCodeWhenNull() {
        val u = ApiJson.decode<UserResponse>("""{"user": {"id": 7, "first_name": "A", "last_name": "B"}}""").user
        u.email = null
        val body = ApiJson.tree(ApiJson.encode(u.arrayMapForUpdate)).toString()
        assertFalse(body.contains("email"))
        assertFalse(body.contains("sms_code"))
        assertTrue(body.contains("\"about\":\"\""))
    }
}
