package social.entourage.android.new_v8.models

import com.google.gson.annotations.SerializedName
import social.entourage.android.R
import social.entourage.android.api.model.feed.FeedItemAuthor
import social.entourage.android.tools.Utils
import java.io.Serializable
import java.util.*

data class Action(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("uuid")
    val uuid: String? = null,

    @field:SerializedName("status")
    private var status: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("image_url")
    val imageUrl: String? = null,

    @field:SerializedName("author")
    val author: FeedItemAuthor? = null,


    @field:SerializedName("section")
    val sectionName: String? = null,

    @field:SerializedName("location")
    val location: Address? = null,

    @field:SerializedName("metadata")
    val metadata: MetadataAction? = null,

    @field:SerializedName("action_type")
    val actionType: String? = null,

    @field:SerializedName("created_at")
    val createdAt: Date? = null,
    @field:SerializedName("status_changed_at")
    val statusChangedAt: Date? = null,
    @field:SerializedName("updated_at")
    val updatedAt: Date? = null

) : Serializable {
    fun isCancel() : Boolean {
        return status == "closed"
    }

    fun setCancel() {
        status = "closed"
    }

    fun isContrib() : Boolean {
        return actionType == "contribution"
    }

    fun dateFormattedString(context: android.content.Context) : String {
        createdAt?.let {
            return Utils.dateAsStringLitteralFromNow(it,context, R.string.action_date_list)
        } ?: return  "-"
    }
}