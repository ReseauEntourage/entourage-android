package social.entourage.android.new_v8.models

import com.google.gson.annotations.SerializedName

data class GroupMember(
    @SerializedName("id")
    var id: Int? = null,
    @SerializedName("display_name")
    var displayName: String? = null,
    @SerializedName("avatar_url")
    var avatarUrl: String? = null,
) {
    override fun toString(): String {
        return "GroupMember(id=$id, displayName=$displayName, avatarUrl=$avatarUrl)"
    }
}