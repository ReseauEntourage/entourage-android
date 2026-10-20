package social.entourage.android.api.model

import java.io.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
data class GroupMember(
    @SerialName("id")
    var id: Int? = null,
    @SerialName("display_name")
    var displayName: String? = null,
    @SerialName("avatar_url")
    var avatarUrl: String? = null,
    @SerialName("is_birthday")
    var isBirthday: Boolean = false) : Serializable {
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
