package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by mihaiionescu on 13/10/16.
 */
class LastMessageAuthor : Serializable {
    @SerializedName("first_name")
    var firstName: String? = null

    @SerializedName("last_name")
    var lastName: String? = null

    companion object {
        private const val serialVersionUID = 8632217120061284659L
    }
}