package social.entourage.android.api.model

import java.io.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable as KSerializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@KSerializable(with = ShareMessageSerializer::class)
abstract class ShareMessage(uuid: String, val content: String) : Serializable {
    val message_type = "share" //Used for POST
    protected val metadata: Metadata = Metadata()

    init {
        metadata.uuid = uuid
    }

    /** Corps envoyé à l'API : `{ message_type, content, metadata: { uuid, type } }`. */
    internal fun toJson(): JsonObject = buildJsonObject {
        put("message_type", message_type)
        put("content", content)
        put("metadata", buildJsonObject {
            metadata.uuid?.let { put("uuid", it) }
            metadata.type?.let { put("type", it) }
        })
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    class Metadata : Serializable {
        var uuid: String? = null
        var type: String? = null
    }
}

class ShareEntourageMessage(uuid: String) : ShareMessage(uuid, CONTENT) {
    init {
        metadata.type = "entourage"
    }

    companion object {
        const val CONTENT = "Voici une action solidaire pour toi !"
    }
}

class SharePOIMessage(uuid: String) : ShareMessage(uuid, SHARE_CONTENT) {
    init {
        metadata.type = "poi"
    }

    companion object {
        const val SHARE_CONTENT = "Voici un lieu solidaire pour toi !"
    }
}
/** Écriture seule : un message de partage n'est jamais relu depuis l'API. */
object ShareMessageSerializer : KSerializer<ShareMessage> {
    override val descriptor: SerialDescriptor = JsonObject.serializer().descriptor

    override fun serialize(encoder: Encoder, value: ShareMessage) {
        (encoder as JsonEncoder).encodeJsonElement(value.toJson())
    }

    override fun deserialize(decoder: Decoder): ShareMessage =
        throw SerializationException("ShareMessage n'est pas désérialisable")
}
