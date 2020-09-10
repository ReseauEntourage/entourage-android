package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Jr (MJ-DEVS) on 09/09/2020.
 */
class SharingEntourage: Serializable {

    var id = 0
    var uuid = ""
    var title = ""
    var group_type = ""
    var entourage_type = ""
    @SerializedName("display_category")
    var category:String? = ""

    @SerializedName("author")
    var author: SharingAuthor? = null

    var isSelected = false
}

class SharingAuthor: Serializable {
    @SerializedName("avatar_url")
    var avatarUrl:String? = null
}