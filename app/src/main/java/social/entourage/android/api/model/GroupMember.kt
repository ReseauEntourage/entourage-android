package social.entourage.android.api.model

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

fun GroupMember.toUser(): User {
    return User.fromGroupMember(this)
}

fun List<GroupMember>.toUsers(): List<User> {
    return map { it.toUser() }
}
