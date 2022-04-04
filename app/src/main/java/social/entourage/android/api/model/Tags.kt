package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Tags : Serializable {
    @SerializedName("interests")
    var interests: ArrayList<Interests>? = null

    @SerializedName("signals")
    var signals: ArrayList<Interests>? = null
    override fun toString(): String {
        return "Tags(interests=$interests, signals=$signals)"
    }
}