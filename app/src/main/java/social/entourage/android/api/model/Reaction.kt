package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ReactionType(
    @SerializedName("id")
    val id: Int,
    @SerializedName("key")
    val key: String?,
    @SerializedName("image_url")
    val imageUrl: String?
) : Serializable

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

data class CompleteReactionsResponse(
    val user_reactions: List<UserReaction>
)

data class UserReaction(
    val reaction_id: Int,
    val user: EntourageUser
)

/*data class EntourageUser(
    val id: Int,
    val lang: String,
    val display_name: String,
    val avatar_url: String,
    val community_roles: List<String>
)*/
