package social.entourage.android.api.model.notification

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ReactionType: Serializable {
    @SerializedName("id")
    val id:Int = 0
    @SerializedName("key")
    val key:String? = null
    @SerializedName("image_url")
    val imageUrl:String? = null
}

class Reaction: Serializable {
    @SerializedName("reaction_id")
    var reactionId:Int = 0
    @SerializedName("chat_message_id")
    val chatMessageId:Int = 0
    @SerializedName("reactions_count")
    var reactionsCount:Int = 0
}

class ReactionWrapper: Serializable {
    @SerializedName("reaction_id")
    var reactionId:Int? = null
}