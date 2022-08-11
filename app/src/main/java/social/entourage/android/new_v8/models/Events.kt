package social.entourage.android.new_v8.models

import android.content.Context
import android.location.Location
import com.google.gson.annotations.SerializedName
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.feed.FeedItemAuthor
import java.util.*

enum class Status {
    @SerializedName("open")
    OPEN,

    @SerializedName("closed")
    CLOSED,
}

data class GroupEvent(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("name")
    val name: String? = null,
)

data class Events(
    @field:SerializedName("metadata")
    val metadata: Metadata? = null,

    @field:SerializedName("entourage_type")
    val entourageType: String? = null,

    @field:SerializedName("image_url")
    val imageUrl: String? = null,

    @field:SerializedName("author")
    val author: FeedItemAuthor? = null,

    @field:SerializedName("created_at")
    val createdAt: Date? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("group_type")
    val groupType: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("uuid")
    val uuid: String? = null,

    @field:SerializedName("event_url")
    val eventUrl: String? = null,

    @field:SerializedName("number_of_people")
    val numberOfPeople: Int? = null,

    @field:SerializedName("public")
    val jsonMemberPublic: Boolean? = null,

    @field:SerializedName("updated_at")
    val updatedAt: Date? = null,

    @field:SerializedName("share_url")
    val shareUrl: String? = null,

    @field:SerializedName("online")
    val online: Boolean? = null,

    @field:SerializedName("location")
    val location: Address? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("members_count")
    val membersCount: Int? = null,

    @SerializedName("members")
    var members: MutableList<GroupMember>? = mutableListOf(),

    @SerializedName("member")
    var member: Boolean = false,

    @SerializedName("interests")
    var interests: MutableList<String> = mutableListOf(),

    @field:SerializedName("number_of_unread_messages")
    val numberOfUnreadMessages: Int? = null,

    @field:SerializedName("postal_code")
    val postalCode: String? = null,

    @field:SerializedName("join_status")
    val joinStatus: String? = null,

    @field:SerializedName("display_report_prompt")
    val displayReportPrompt: Boolean? = null,

    @field:SerializedName("display_category")
    val displayCategory: String? = null,

    @field:SerializedName("status")
    val status: Status? = null,

    @field:SerializedName("address")
    var displayAddress: String? = null,

    @SerializedName("recurrency")
    var recurrence: Int? = 0,

    @SerializedName("neighborhoods")
    var neighborhoods: MutableList<GroupEvent>? = mutableListOf(),
)

fun Events.toEventUi(context: Context): EventUiModel {
    return EventUiModel(
        this.id,
        this.title,
        this.membersCount,
        this.displayAddress,
        this.interests,
        this.description,
        this.members,
        this.member,
        EntourageApplication.me(context)?.id == this.author?.userID,
        this.online,
        this.metadata,
        this.eventUrl,
        this.createdAt,
        this.updatedAt,
        this.recurrence,
        this.neighborhoods,
        this.location
    )
}


