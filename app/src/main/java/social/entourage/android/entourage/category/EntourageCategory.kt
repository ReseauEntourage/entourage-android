package social.entourage.android.entourage.category

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.google.gson.annotations.SerializedName
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import java.io.Serializable

/**
 * Created by Mihai Ionescu on 20/09/2017.
 */
class EntourageCategory : Serializable {
    // ----------------------------------
    // Attributes from JSON
    // ----------------------------------
    @SerializedName("entourage_type")
    var groupType: String? = null

    @SerializedName("display_category")
    var category: String? = null
    var key: String? = null

    @SerializedName("display_category_title")
    var title: String? = null

    @SerializedName("title_example")
    var titleExample: String? = null

    @SerializedName("description_example")
    var descriptionExample: String? = null

    @SerializedName("hidden")
    val isHidden = false

    @SerializedName("default")
    var isDefault = false

    @SerializedName("display_category_title_list")
    var title_list: String? = null

    // ----------------------------------
    // Attributes not in JSON
    // ----------------------------------
    var isSelected = false
    var isNewlyCreated = false

    // ----------------------------------
    // Helper methods
    // ----------------------------------
    @get:DrawableRes
    val iconRes: Int
        get() {
            when {
                CATEGORY_SOCIAL.equals(category, ignoreCase = true) -> {
                    return R.drawable.ic_entourage_category_friendly_time
                }
                CATEGORY_EVENT.equals(category, ignoreCase = true) -> {
                    return R.drawable.ic_event_accent_24dp
                }
                CATEGORY_MATHELP.equals(category, ignoreCase = true) -> {
                    return R.drawable.ic_entourage_category_sweater
                }
                CATEGORY_RESOURCE.equals(category, ignoreCase = true) -> {
                    return R.drawable.ic_entourage_category_washing_machine
                }
                CATEGORY_INFO.equals(category, ignoreCase = true) -> {
                    return if (BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION.equals(groupType, ignoreCase = true)) {
                        R.drawable.ic_entourage_category_info_chat
                    } else R.drawable.ic_entourage_category_question_chat
                }
                CATEGORY_SKILL.equals(category, ignoreCase = true) -> {
                    return R.drawable.ic_entourage_category_skill
                }
                CATEGORY_UNKNOWN.equals(category, ignoreCase = true) -> {
                    return R.drawable.ic_entourage_category_more
                }
                else -> {
                    return R.drawable.ic_entourage_category_more
                }
            }
        }

    @get:ColorRes
    val typeColorRes: Int
        get() {
            if (BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION.equals(groupType, ignoreCase = true)) {
                return R.color.bright_blue
            }
            return if (BaseEntourage.GROUPTYPE_ACTION_DEMAND.equals(groupType, ignoreCase = true)) {
                R.color.accent
            } else R.color.accent
        }

    companion object {
        const val CATEGORY_UNKNOWN = "other"
        const val CATEGORY_SKILL = "skill"
        const val CATEGORY_INFO = "info"
        const val CATEGORY_RESOURCE = "resource"
        const val CATEGORY_MATHELP = "mat_help"
        const val CATEGORY_EVENT = "event"
        const val CATEGORY_SOCIAL = "social"
    }
}