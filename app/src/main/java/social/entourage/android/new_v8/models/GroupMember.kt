package social.entourage.android.new_v8.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GroupMember(
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("display_name")
    var displayName: String? = null,
    @SerializedName("avatar_url")
    var avatarUrl: String? = null,
) : Serializable {
    override fun toString(): String {
        return "GroupMember(id=$id, displayName=$displayName, avatarUrl=$avatarUrl)"
    }
}