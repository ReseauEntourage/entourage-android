package social.entourage.android.api.model

import java.io.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
class TagMetaData : Serializable {
    @SerialName("id")
    var id: String? = null

    @SerialName("name")
    var name: String? = null

    @SerialName("subname")
    var subname: String? = null

    override fun toString(): String {
        return "Interests(id=$id, name=$name, subname=$subname)"
    }
}