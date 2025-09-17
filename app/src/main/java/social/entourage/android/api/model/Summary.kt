package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class SummaryAction{
    @SerializedName("id")
    var id: Int? = null
    @SerializedName("action_type")
    var actionType: String? = null
    @SerializedName("title")
    var title: String? = null

}

class Summary : Serializable {
    @SerializedName("id")
    var id: Int? = null

    @SerializedName("display_name")
    var displayName: String? = null

    @SerializedName("avatar_url")
    var avatarURL: String? = null

    @SerializedName("meetings_count")
    var meetingsCount: Int? = null

    @SerializedName("chat_messages_count")
    var chatMessagesCount: Int? = null

    @SerializedName("outing_participations_count")
    var outingParticipationsCount: Int? = null

    @SerializedName("signable_permission")
    var signablePermission : Boolean? = null

    @SerializedName("neighborhood_participations_count")
    var neighborhoodParticipationsCount: Int? = null

    @SerializedName("recommandations")
    var recommendations: MutableList<HomeAction>? = null

    @SerializedName("moderator")
    var moderator: HomeModerator? = null

    @SerializedName("congratulations")
    var congratulations: MutableList<HomeAction>? = null

    @SerializedName("unclosed_action")
    var unclosedAction: SummaryAction? = null

    @SerializedName("preference")
    var preference: String? = null

}

class HomeAction : Serializable {
    @SerializedName("name")
    var name: String? = null

    @SerializedName("type")
    var homeType: HomeType? = null

    @SerializedName("action")
    var action: ActionSummary? = null

    @SerializedName("image_url")
    var imageURL: String? = null

    @SerializedName("params")
    var params: HomeActionParams? = null
}

class HomeActionParams : Serializable {
    @SerializedName("id")
    var id: Int? = null

    @SerializedName("uuid")
    var uuid: String? = null

    @SerializedName("url")
    var url: String? = null

    @SerializedName("post_id")
    var postId: Int? = null

}

class HomeModerator : Serializable {
    @SerializedName("display_name")
    var displayName: String? = null

    @SerializedName("id")
    val id: Int? = null

    @SerializedName("avatar_url")
    val imageURL: String? = null

}

enum class HomeType {
    @SerializedName("conversation")
    CONVERSATION,

    @SerializedName("neighborhood")
    NEIGHBORHOOD,

    @SerializedName("neighborhood_post")
    NEIGHBORHOOD_POST,

    @SerializedName("profile")
    PROFILE,

    @SerializedName("poi")
    POI,

    @SerializedName("user")
    USER,

    @SerializedName("outing_post")
    OUTING_POST,

    @SerializedName("outing")
    OUTING,

    @SerializedName("webview")
    WEBVIEW,

    @SerializedName("contribution")
    CONTRIBUTION,

    @SerializedName("solicitation")
    SOLICITATION,

    @SerializedName("resource")
    RESOURCE;
}

enum class ActionSummary {
    @SerializedName("show")
    SHOW,

    @SerializedName("index")
    INDEX,

    @SerializedName("create")
    CREATE;
}