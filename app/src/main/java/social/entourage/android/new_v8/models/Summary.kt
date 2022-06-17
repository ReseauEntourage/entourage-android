package social.entourage.android.new_v8.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

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

    @SerializedName("neighborhood_participations_count")
    var neighborhoodParticipationsCount: Int? = null

    @SerializedName("recommandations")
    var recommandations: List<Recommandation>? = null
}

class Recommandation : Serializable {
    @SerializedName("name")
    var name: String? = null

    @SerializedName("type")
    var type: String? = null

    @SerializedName("action")
    var action: String? = null

    @SerializedName("image_url")
    var imageURL: String? = null

    @SerializedName("params")
    var params: Params? = null
}

class Params : Serializable {
    @SerializedName("id")
    val id: String? = null

    @SerializedName("uuid")
    val uuid: String? = null

    @SerializedName("url")
    val url: String? = null
}