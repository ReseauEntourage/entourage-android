package social.entourage.android.api.model

import android.content.Context
import android.graphics.drawable.Drawable
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class EntourageConversation : BaseEntourage, Serializable {
    @SerializedName("display_report_prompt")
    var isDisplay_report_prompt:Boolean = false

    companion object {
        const val serialVersionUID = -967689727L
    }

    //needed for deserialize
    constructor() : super(GROUPTYPE_CONVERSATION, GROUPTYPE_CONVERSATION)

    constructor(category: String?,
                title: String, description: String, location: LocationPoint) : super(GROUPTYPE_CONVERSATION, GROUPTYPE_CONVERSATION, category, title, description, location)

    override fun getIconDrawable(context: Context): Drawable? {
        return null
    }

    override fun getIconURL(): String? {
        return author?.avatarURLAsString ?: super.getIconURL()
    }

}