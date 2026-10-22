@file:UseSerializers(ApiDateSerializer::class)

package social.entourage.android.api.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import social.entourage.android.api.model.notification.Translation
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.UseSerializers
import social.entourage.android.api.json.ApiDateSerializer

@KSerializable
class Post(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("content")
    val content: String? = null,
    @SerialName("content_html")
    val contentHtml: String? = null,
    @SerialName("content_translations")
    val contentTranslations: Translation? = null,
    @SerialName("content_translations_html")
    val contentTranslationsHtml: Translation? = null,
    @SerialName("user")
    var user: EntourageUser? = null,
    @SerialName("created_at")
    var createdTime: Date? = null,
    @SerialName("message_type")
    val messageType: String? = null,
    @SerialName("post_id")
    val postId: Int? = null,
    @SerialName("has_comments")
    val hasComments: Boolean? = null,
    @SerialName("comments_count")
    val commentsCount: Int? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("reactions")
    var reactions: MutableList<Reaction>? = mutableListOf(),
    @SerialName("read")
    val read: Boolean? = null,
    @SerialName("reaction_id")
    @KSerializable(with = NullableBooleanTolerantIntSerializer::class)
    var reactionId: Int? = null,
    @Transient
    val idInternal: UUID? = null,
    @SerialName("survey")
    val survey: Survey? = null,
    @SerialName("survey_response")
    var surveyResponse: MutableList<Boolean>? = mutableListOf(),
    @SerialName("auto_post_from")
    val autoPostFrom: AutoPostFrom? = null) {
    var datePostText = ""
    var isDatePostOnly = false

    fun getFormatedStr() : String {
        val locale = Locale.getDefault()
        return createdTime?.let { SimpleDateFormat("EEEE dd MMMM yyyy",locale).format(it) } ?: ""
    }

    override fun toString(): String {
        return "Post(id=$id, content=$content, user=$user, createdTime=$createdTime, messageType=$messageType, postId=$postId, hasComments=$hasComments, commentsCount=$commentsCount, imageUrl=$imageUrl, read=$read)"
    }
}


@KSerializable
data class Survey(
    @SerialName("choices")
    var choices: List<String> = emptyList(),
    @SerialName("multiple")
    var multiple: Boolean = false,
    @SerialName("summary")
    var summary: MutableList<Int> = mutableListOf()
)

@KSerializable
data class AutoPostFrom(
    @SerialName("instance_type")
    val instanceType: String = "", // ou Entourage, selon ce que tu as besoin de représenter
    @SerialName("instance_id")
    val instanceId: Int = 0
)

/**
 * Le back renvoie parfois `reaction_id: false` (booléen JSON) au lieu de `null`/absent quand il
 * n'y a pas de réaction utilisateur — constaté sur la réponse de POST chat_messages
 * (JsonSyntaxException "Expected an int but was BOOLEAN ... $.chat_message.reaction_id"), qui
 * faisait échouer tout le parsing de Post et donc croire à un échec de publication alors que le
 * post était bien créé côté serveur.
 */
object NullableBooleanTolerantIntSerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NullableBooleanTolerantInt", PrimitiveKind.INT).nullable

    override fun serialize(encoder: Encoder, value: Int?) {
        if (value == null) encoder.encodeNull() else encoder.encodeInt(value)
    }

    override fun deserialize(decoder: Decoder): Int? {
        val element = (decoder as JsonDecoder).decodeJsonElement()
        return when {
            element is JsonNull -> null
            element is JsonPrimitive && element.booleanOrNull != null -> null
            element is JsonPrimitive -> element.intOrNull ?: element.content.toDoubleOrNull()?.toInt()
            else -> null
        }
    }
}
