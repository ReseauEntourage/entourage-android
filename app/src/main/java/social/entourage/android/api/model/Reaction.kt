package social.entourage.android.api.model

import java.io.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
class ReactionType(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("key")
    val key: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null
) : Serializable

@KSerializable
class Reaction: Serializable {
    @SerialName("reaction_id")
    var reactionId:Int = 0
    @SerialName("chat_message_id")
    val chatMessageId:Int = 0
    @SerialName("reactions_count")
    var reactionsCount:Int = 0
}

@KSerializable
class ReactionWrapper: Serializable {
    @SerialName("reaction_id")
    var reactionId:Int? = null
}

@KSerializable
data class CompleteReactionsResponse(
    val user_reactions: List<UserReaction> = emptyList()
)

@KSerializable
data class UserReaction(
    val reaction_id: Int = 0,
    val user: EntourageUser
)

/*data class EntourageUser(
    val id: Int,
    val lang: String,
    val display_name: String,
    val avatar_url: String,
    val community_roles: List<String>
)*/
