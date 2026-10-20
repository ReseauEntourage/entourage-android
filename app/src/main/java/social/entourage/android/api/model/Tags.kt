package social.entourage.android.api.model

import java.io.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
class Tags : Serializable {
    @SerialName("interests")
    var interests: ArrayList<TagMetaData>? = null

    @SerialName("signals")
    var signals: ArrayList<TagMetaData>? = null


    @SerialName("sections")
    var sections: ArrayList<TagMetaData>? = null
    override fun toString(): String {
        return "Tags(interests=$interests, signals=$signals, sections=$sections)"
    }
}