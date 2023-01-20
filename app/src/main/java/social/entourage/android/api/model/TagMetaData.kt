package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class TagMetaData : Serializable {
    @SerializedName("id")
    var id: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("subname")
    var subname: String? = null

    override fun toString(): String {
        return "Interests(id=$id, name=$name, subname=$subname)"
    }
}