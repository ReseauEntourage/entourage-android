package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

object Tags : Serializable {
    @SerializedName("interests")
    var interests: Interests? = null
    override fun toString(): String {
        return "Tags(interests=$interests)"
    }
}