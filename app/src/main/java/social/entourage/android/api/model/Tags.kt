package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Tags : Serializable {
    @SerializedName("interests")
    var interests: ArrayList<TagMetaData>? = null

    @SerializedName("signals")
    var signals: ArrayList<TagMetaData>? = null


    @SerializedName("sections")
    var sections: ArrayList<TagMetaData>? = null
    override fun toString(): String {
        return "Tags(interests=$interests, signals=$signals, sections=$sections)"
    }
}