package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Tags : Serializable {
    @SerializedName("interests")
    var interests: ArrayList<MetaData>? = null

    @SerializedName("signals")
    var signals: ArrayList<MetaData>? = null

    @SerializedName("sections")
    var sections: ArrayList<MetaData>? = null
    override fun toString(): String {
        return "Tags(interests=$interests, signals=$signals, sections=$sections)"
    }
}