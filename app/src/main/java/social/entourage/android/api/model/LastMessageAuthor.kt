package social.entourage.android.api.model

import java.io.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

/**
 * Created by mihaiionescu on 13/10/16.
 */
@KSerializable
class LastMessageAuthor : Serializable {
    @SerialName("first_name")
    var firstName: String? = null

    @SerialName("last_name")
    var lastName: String? = null

    @SerialName("display_name")
    var displayName: String? = null

    @SerialName("id")
    var authorId: Int = 0

    companion object {
        private const val serialVersionUID = 8632217120061284659L
    }
}