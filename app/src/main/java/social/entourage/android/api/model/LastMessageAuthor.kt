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

    @SerializedName("display_name")
    var displayName: String? = null

    @SerializedName("id")
    var authorId: Int = 0

    companion object {
        private const val serialVersionUID = 8632217120061284659L
    }
}