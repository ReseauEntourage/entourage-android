package social.entourage.android.api

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import social.entourage.android.api.model.User
import social.entourage.android.api.request.LoginResponse
import social.entourage.android.authentication.ComplexPreferences
import social.entourage.android.authentication.UserPreferences

/**
 * Format stocké sur disque par [ComplexPreferences] (utilisateur connecté, préférences par
 * utilisateur). Il utilise un `Gson()` NU : contrairement à Retrofit, @Expose n'y est pas pris en
 * compte, donc `token`, `phone` et `smsCode` SONT persistés. Un utilisateur déjà connecté relit ce
 * JSON après mise à jour de l'app : la migration doit rester capable de le lire (sinon déconnexion).
 */
class LocalPersistenceCharacterizationTest {

    /** SharedPreferences en mémoire, suffisant pour ComplexPreferences. */
    private class MemoryPrefs {
        val store = mutableMapOf<String, String?>()
        val prefs: SharedPreferences = mock()
        val context: Context = mock()

        init {
            val editor: SharedPreferences.Editor = mock()
            whenever(editor.putString(any(), anyOrNull())).doAnswer {
                store[it.getArgument(0)] = it.getArgument(1)
                editor
            }
            whenever(prefs.edit()).thenReturn(editor)
            whenever(prefs.getString(any(), anyOrNull())).doAnswer {
                if (store.containsKey(it.getArgument<String>(0))) store[it.getArgument(0)] else it.getArgument(1)
            }
            whenever(context.getSharedPreferences(any(), eq(Context.MODE_PRIVATE))).thenReturn(prefs)
        }

        fun complex() = ComplexPreferences(context, "test_prefs", Context.MODE_PRIVATE)
    }

    private fun loggedUser(): User {
        val u = ApiJson.decode<LoginResponse>(ApiJson.fixture("login.json")).user
        u.phone = "0600000000"
        u.smsCode = "654321"
        return u
    }

    @Test
    fun user_roundTripThroughPreferences_keepsEverythingNeededToStayLoggedIn() {
        val mem = MemoryPrefs()
        val cp = mem.complex()
        cp.putObject("user", loggedUser())
        cp.commit()

        val restored = mem.complex().getObject("user", User::class.java)!!
        assertEquals(4079, restored.id)
        assertEquals("fake-token-0000", restored.token)
        assertEquals("0600000000", restored.phone)
        assertEquals("654321", restored.smsCode)
        assertEquals("Test", restored.firstName)
        assertEquals(listOf("09:00-12:00", "14:00-18:00"), restored.availability["1"])
        assertEquals("Lyon", restored.addressSecondary!!.displayAddress)
        assertEquals(5, restored.stats!!.outingsCount)
        assertEquals(1705311000000L, restored.createdAt!!.time)
    }

    @Test
    fun user_storedJson_containsSensitiveFieldsDespiteExposeAnnotations() {
        val mem = MemoryPrefs()
        mem.complex().putObject("user", loggedUser())
        val json = ApiJson.tree(mem.store["user"]!!).toString()
        assertTrue(json.contains("\"token\":\"fake-token-0000\""))
        assertTrue(json.contains("\"phone\":\"0600000000\""))
        assertTrue(json.contains("\"smsCode\":\"654321\""))
        // Les autres champs sont stockés sous leurs clés serveur.
        assertTrue(json.contains("\"first_name\":\"Test\""))
        assertTrue(json.contains("\"address_2\""))
    }

    /** JSON tel qu'écrit par une version déjà installée (format de référence à relire après migration). */
    private val legacyStoredUser = """{"id":4079,"email":"test@example.org","first_name":"Test","last_name":"Utilisateur",
        "birthdate":"1990-05-17","gender":"female","interests":["sport"],"involvements":[],"orientations":[],"concerns":[],
        "display_name":"Test U.","avatar_url":"https://cdn.example.org/a.png","availability":{"1":["09:00-12:00"]},
        "about":"Bonjour","address":{"latitude":48.8,"longitude":2.3,"display_address":"Paris"},
        "engaged":true,"is_birthday":false,"phone":"0600000000","smsCode":"654321","token":"fake-token-0000",
        "created_at":"2024-01-15T09:30:00.000+0000"}"""

    @Test
    fun user_legacyStoredJson_canStillBeRead() {
        val mem = MemoryPrefs()
        mem.store["user"] = legacyStoredUser
        val u = mem.complex().getObject("user", User::class.java)!!
        assertEquals(4079, u.id)
        assertEquals("fake-token-0000", u.token)
        assertEquals("654321", u.smsCode)
        assertEquals("Bonjour", u.about)
        assertEquals(listOf("09:00-12:00"), u.availability["1"])
        assertEquals("Paris", u.address!!.displayAddress)
        assertTrue(u.isEngaged)
    }

    @Test
    fun missingKey_returnsNull() {
        assertNull(MemoryPrefs().complex().getObject("user", User::class.java))
    }

    @Test
    fun nullObjectIsStoredAsEmptyString_andReadsBackAsNull() {
        val mem = MemoryPrefs()
        mem.complex().putObject("user", null)
        assertEquals("", mem.store["user"])
        assertNull(mem.complex().getObject("user", User::class.java))
    }

    @Test
    fun corruptedJson_isSwallowedAndReturnsNull() {
        val mem = MemoryPrefs()
        mem.store["user"] = "{pas du json"
        assertNull(mem.complex().getObject("user", User::class.java))
    }

    @Test
    fun userPreferencesMap_isStoredWithStringKeysAndReadBackWithIntKeys() {
        val mem = MemoryPrefs()
        val prefs = UserPreferences().apply { isShowInfoPOIsPopup = false }
        mem.complex().putObject("prefs", mapOf(4079 to prefs))
        assertTrue(mem.store["prefs"]!!.startsWith("{\"4079\":"))

        val type = object : TypeToken<Map<Int?, UserPreferences?>?>() {}.type
        val back = mem.complex().getObjectFromType<Map<Int, UserPreferences?>>("prefs", type)!!
        assertEquals(setOf(4079), back.keys)
        assertFalse(back[4079]!!.isShowInfoPOIsPopup)
    }

    @Test
    fun userPreferencesMap_legacyJsonIsReadable() {
        val mem = MemoryPrefs()
        mem.store["prefs"] = """{"4079":{"isShowInfoPOIsPopup":true,"isShowNoEntouragesPopup":false},"12":{"isShowInfoPOIsPopup":false}}"""
        val type = object : TypeToken<Map<Int?, UserPreferences?>?>() {}.type
        val back = mem.complex().getObjectFromType<Map<Int, UserPreferences?>>("prefs", type)!!
        assertEquals(setOf(4079, 12), back.keys)
        assertNotNull(back[12])
        assertFalse(back[12]!!.isShowInfoPOIsPopup)
        // Gson nu ignore @Expose : un champ marqué serialize=false est lu et écrit quand même.
        assertFalse(back[4079]!!.isShowNoEntouragesPopup)
    }
}
