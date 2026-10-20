package social.entourage.android.api

import androidx.collection.ArrayMap
import org.junit.Assert.assertEquals
import org.junit.Test
import social.entourage.android.api.model.Address

/**
 * Corps de requêtes envoyés via `@Body ArrayMap<String, Any>` (~99 usages). Chaque test reproduit
 * la construction réelle d'un payload (presenters, OnboardingAPI) et fige le JSON exact envoyé.
 * kotlinx.serialization ne sait pas sérialiser `Any` : à la migration, ces payloads devront devenir
 * des DTO @Serializable ou des JsonObject produisant exactement les mêmes JSON.
 */
class RequestBodyCharacterizationTest {

    private fun map() = ArrayMap<String, Any>()

    /** Compare deux JSON indépendamment de l'ordre des clés. */
    private fun assertJson(expected: String, actual: String) =
        assertEquals(ApiJson.tree(expected), ApiJson.tree(actual))

    @Test
    fun textPost_nestedChatMessage() {
        // GroupPresenter / EventsPresenter / DiscussionsPresenter.addPost
        val chatMessage = map().apply { put("chat_message", map().apply { put("content", "Bonjour à tous 👋") }) }
        assertJson("""{"chat_message":{"content":"Bonjour à tous 👋"}}""", ApiJson.encode(chatMessage))
    }

    @Test
    fun comment_parentIdIsSentAsString() {
        val messageChat = map()
        messageChat["content"] = "Merci !"
        messageChat["parent_id"] = 7000.toString()
        val body = map().apply { put("chat_message", messageChat) }
        assertJson("""{"chat_message":{"content":"Merci !","parent_id":"7000"}}""", ApiJson.encode(body))
    }

    @Test
    fun comment_nullPostIdIsSentAsTheStringNull() {
        // `comment?.postId.toString()` sur un Int? null produit la chaîne "null" (bug latent à figer).
        val postId: Int? = null
        val messageChat = map()
        messageChat["content"] = "Merci !"
        messageChat["parent_id"] = postId.toString()
        assertJson("""{"content":"Merci !","parent_id":"null"}""", ApiJson.encode(messageChat))
    }

    @Test
    fun nullValuesAreDroppedFromTheBody() {
        // Gson n'écrit pas les valeurs null d'une Map (serializeNulls désactivé).
        val content: String? = null
        val messageChat = map()
        messageChat["content"] = content
        messageChat["image_url"] = "uploads/a.jpg"
        assertJson("""{"image_url":"uploads/a.jpg"}""", ApiJson.encode(messageChat))
    }

    @Test
    fun imagePost_withEmojiFallbackContent() {
        val chatMessageMap = map().apply {
            put("image_url", "uploads/a.jpg")
            put("message_type", "image")
            put("content", "📷")
        }
        val root = map().apply { put("chat_message", chatMessageMap) }
        assertJson(
            """{"chat_message":{"image_url":"uploads/a.jpg","message_type":"image","content":"📷"}}""",
            ApiJson.encode(root)
        )
    }

    @Test
    fun updatePost_flatContent() {
        assertJson("""{"content":"Texte corrigé"}""", ApiJson.encode(map().apply { put("content", "Texte corrigé") }))
    }

    @Test
    fun createConversation_userIdKeepsItsType() {
        // DiscussionsPresenter.createOrGetConversation(userId: String)
        val params = map().apply { put("conversation", map().apply { put("user_id", "42") }) }
        assertJson("""{"conversation":{"user_id":"42"}}""", ApiJson.encode(params))

        val numeric = map().apply { put("conversation", map().apply { put("user_id", 42) }) }
        assertJson("""{"conversation":{"user_id":42}}""", ApiJson.encode(numeric))
    }

    @Test
    fun unblockUsers_sendsListOfIds() {
        val param = map().apply { put("blocked_user_ids", listOf(3, 5, 8)) }
        assertJson("""{"blocked_user_ids":[3,5,8]}""", ApiJson.encode(param))
    }

    @Test
    fun onboardingProfile_booleansStringsAndOptionalKeys() {
        // OnboardingAPI : seules les clés renseignées sont envoyées.
        val user = map()
        user["goal"] = "offer_help"
        user["newsletter_subscription"] = true
        user["email"] = "test@example.org"
        user["birthdate"] = "1990-05-17"
        val request = map().apply { put("user", user) }
        assertJson(
            """{"user":{"goal":"offer_help","newsletter_subscription":true,"email":"test@example.org","birthdate":"1990-05-17"}}""",
            ApiJson.encode(request)
        )
    }

    @Test
    fun enhancedOnboarding_listsOfStrings() {
        val userMap = map().apply {
            put("interests", arrayListOf("sport", "cuisine"))
            put("concerns", arrayListOf<String>())
            put("orientations", arrayListOf("share"))
        }
        val request = map().apply { put("user", userMap) }
        assertJson(
            """{"user":{"interests":["sport","cuisine"],"concerns":[],"orientations":["share"]}}""",
            ApiJson.encode(request)
        )
    }

    @Test
    fun registerUser_withCodeAndSmsCode() {
        val user = map().apply { put("phone", "+33600000000") }
        val request = map().apply {
            put("user", user)
            put("code", "123456")
        }
        assertJson("""{"user":{"phone":"+33600000000"},"code":"123456"}""", ApiJson.encode(request))
    }

    @Test
    fun updateAddress_modelObjectIsSerializedWithServerKeys() {
        // OnboardingAPI : request["address"] = address (objet modèle dans une Map<String, Any>).
        val request = map().apply { put("address", Address(48.8675, 2.3636, "Paris 3e")) }
        assertJson(
            """{"address":{"latitude":48.8675,"longitude":2.3636,"display_address":"Paris 3e"}}""",
            ApiJson.encode(request)
        )
    }

    @Test
    fun numbers_keepIntegerVsDecimalForm() {
        val body = map().apply {
            put("count", 3)
            put("ratio", 2.5)
            put("whole_double", 2.0)
        }
        assertJson("""{"count":3,"ratio":2.5,"whole_double":2.0}""", ApiJson.encode(body))
    }

    @Test
    fun emptyBodyIsAnEmptyObject() {
        assertEquals("{}", ApiJson.encode(map()))
    }
}
