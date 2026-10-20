package social.entourage.android.api.model

import java.io.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
data class Address(
    @SerialName("latitude")
    var latitude: Double = 0.0,
    @SerialName("longitude")
    var longitude: Double = 0.0,
    @SerialName("display_address")
    var displayAddress: String = ""
) : Serializable {
    override fun toString(): String {
        return "Address(latitude=$latitude, longitude=$longitude, displayAddress='$displayAddress')"
    }
}
