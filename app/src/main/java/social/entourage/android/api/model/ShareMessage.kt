package social.entourage.android.api.model

import java.io.Serializable

abstract class ShareMessage(uuid: String, val content: String) : Serializable {
    val message_type = "share" //Used for POST
    protected val metadata: Metadata = Metadata()

    init {
        metadata.uuid = uuid
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