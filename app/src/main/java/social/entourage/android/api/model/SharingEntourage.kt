package social.entourage.android.api.model

import java.io.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

/**
 * Created by Jr (MJ-DEVS) on 09/09/2020.
 */
@KSerializable
class SharingEntourage: Serializable {

    var id = 0
    var uuid = ""
    var title = ""
    var group_type = ""
    var entourage_type = ""
    @SerialName("display_category")
    var category:String? = ""

    @SerialName("author")
    var author: SharingAuthor? = null

    var isSelected = false
}

@KSerializable
class SharingAuthor: Serializable {
    @SerialName("avatar_url")
    var avatarUrl:String? = null
}