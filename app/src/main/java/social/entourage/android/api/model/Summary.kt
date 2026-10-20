package social.entourage.android.api.model

import java.io.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
class SummaryAction{
    @SerialName("id")
    var id: Int? = null
    @SerialName("action_type")
    var actionType: String? = null
    @SerialName("title")
    var title: String? = null

}

@KSerializable
class Summary : Serializable {
    @SerialName("id")
    var id: Int? = null

    @SerialName("display_name")
    var displayName: String? = null

    @SerialName("avatar_url")
    var avatarURL: String? = null

    @SerialName("meetings_count")
    var meetingsCount: Int? = null

    @SerialName("chat_messages_count")
    var chatMessagesCount: Int? = null

    @SerialName("outing_participations_count")
    var outingParticipationsCount: Int? = null

    @SerialName("past_outing_participations_count")
    var pastOutingParticipationsCount: Int? = null

    @SerialName("action_creations_count")
    var actionCreationsCount: Int? = null

    @SerialName("neighborhood_messages_count")
    var neighborhoodMessagesCount: Int? = null

    @SerialName("conversation_members_count")
    var conversationMembersCount: Int? = null

    @SerialName("signable_permission")
    var signablePermission : Boolean? = null

    @SerialName("association")
    var association : Boolean? = null

    @SerialName("neighborhood_participations_count")
    var neighborhoodParticipationsCount: Int? = null

    @SerialName("recommandations")
    var recommendations: MutableList<HomeAction>? = null

    @SerialName("moderator")
    var moderator: HomeModerator? = null

    @SerialName("referent_benevole")
    var referentBenevole: HomeModerator? = null

    @SerialName("congratulations")
    var congratulations: MutableList<HomeAction>? = null

    @SerialName("unclosed_action")
    var unclosedAction: SummaryAction? = null


    @SerialName("preference")
    var preference: String? = null

    @SerialName("events")
    var events: MutableList<String>? = null


}

@KSerializable
class HomeAction : Serializable {
    @SerialName("name")
    var name: String? = null

    @SerialName("type")
    var homeType: HomeType? = null

    @SerialName("action")
    var action: ActionSummary? = null

    @SerialName("image_url")
    var imageURL: String? = null

    @SerialName("params")
    var params: HomeActionParams? = null
}

@KSerializable
class HomeActionParams : Serializable {
    @SerialName("id")
    var id: Int? = null

    @SerialName("uuid")
    var uuid: String? = null

    @SerialName("url")
    var url: String? = null

    @SerialName("post_id")
    var postId: Int? = null

    @SerialName("chat_message_id")
    var chatMessageId: Int? = null

}

@KSerializable
class HomeModerator : Serializable {
    @SerialName("display_name")
    var displayName: String? = null

    @SerialName("id")
    val id: Int? = null

    @SerialName("avatar_url")
    val imageURL: String? = null

}

@KSerializable
enum class HomeType {
    @SerialName("conversation")
    CONVERSATION,

    @SerialName("neighborhood")
    NEIGHBORHOOD,

    @SerialName("neighborhood_post")
    NEIGHBORHOOD_POST,

    @SerialName("profile")
    PROFILE,

    @SerialName("poi")
    POI,

    @SerialName("user")
    USER,

    @SerialName("outing_post")
    OUTING_POST,

    @SerialName("outing")
    OUTING,

    @SerialName("webview")
    WEBVIEW,

    @SerialName("contribution")
    CONTRIBUTION,

    @SerialName("solicitation")
    SOLICITATION,

    @SerialName("resource")
    RESOURCE;
}

@KSerializable
enum class ActionSummary {
    @SerialName("show")
    SHOW,

    @SerialName("index")
    INDEX,

    @SerialName("create")
    CREATE;
}