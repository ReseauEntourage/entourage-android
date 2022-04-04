package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Interests : Serializable {
    @SerializedName("id")
    var id: String? = null

    @SerializedName("name")
    var name: String? = null

    override fun toString(): String {
        return "Interests(id=$id, name=$name)"
    }
}