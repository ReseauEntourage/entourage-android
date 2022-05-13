package social.entourage.android.new_v8.models

import com.google.gson.annotations.SerializedName


data class Address(
    @SerializedName("latitude")
    var latitude: Double = 0.0,
    @SerializedName("longitude")
    var longitude: Double = 0.0,
    @SerializedName("display_address")
    var displayAddress: String = ""
) {
    override fun toString(): String {
        return "Address(latitude=$latitude, longitude=$longitude, displayAddress='$displayAddress')"
    }
}
