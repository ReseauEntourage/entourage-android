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
    @JvmField
    var groupType: String? = null

    @SerializedName("display_category")
    @JvmField
    var category: String? = null
    var key: String? = null

    @SerializedName("display_category_title")
    @JvmField
    var title: String? = null

    @SerializedName("title_example")
    @JvmField
    var titleExample: String? = null

    @SerializedName("description_example")
    @JvmField
    var descriptionExample: String? = null
    @SerializedName("hidden")
    val isHidden = false

    @SerializedName("default")
    var isDefault = false
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
            if ("social".equals(category, ignoreCase = true)) {
                return R.drawable.ic_entourage_category_friendly_time
            }
            if ("event".equals(category, ignoreCase = true)) {
                return R.drawable.ic_event_accent_24dp
            }
            if ("mat_help".equals(category, ignoreCase = true)) {
                return R.drawable.ic_entourage_category_sweater
            }
            if ("resource".equals(category, ignoreCase = true)) {
                return R.drawable.ic_entourage_category_washing_machine
            }
            if ("info".equals(category, ignoreCase = true)) {
                return if (BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION.equals(groupType, ignoreCase = true)) {
                    R.drawable.ic_entourage_category_info_chat
                } else R.drawable.ic_entourage_category_question_chat
            }
            if ("skill".equals(category, ignoreCase = true)) {
                return R.drawable.ic_entourage_category_skill
            }
            return if ("other".equals(category, ignoreCase = true)) {
                R.drawable.ic_entourage_category_more
            } else R.drawable.ic_entourage_category_more
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
}