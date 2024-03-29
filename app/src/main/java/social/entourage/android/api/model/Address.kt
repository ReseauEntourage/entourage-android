package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Address(
    @SerializedName("latitude")
    var latitude: Double = 0.0,
    @SerializedName("longitude")
    var longitude: Double = 0.0,
    @SerializedName("display_address")
    var displayAddress: String = ""
) : Serializable {
    override fun toString(): String {
        return "Address(latitude=$latitude, longitude=$longitude, displayAddress='$displayAddress')"
    }
}
