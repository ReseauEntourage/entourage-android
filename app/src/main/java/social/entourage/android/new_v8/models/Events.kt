package social.entourage.android.new_v8.models

import com.google.gson.annotations.SerializedName
import social.entourage.android.api.model.BaseEntourage.*
import social.entourage.android.api.model.feed.FeedItemAuthor

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
    val createdAt: String? = null,

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
    val updatedAt: String? = null,

    @field:SerializedName("share_url")
    val shareUrl: String? = null,

    @field:SerializedName("online")
    val online: Boolean? = null,

    @field:SerializedName("location")
    val location: Address? = null,

    @field:SerializedName("members_count")
    val id: Int? = null,

    @field:SerializedName("id")
    val membersCount: Int? = null,

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
    val status: String? = null
)


