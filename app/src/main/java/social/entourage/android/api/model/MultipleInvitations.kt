package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by mihaiionescu on 08/08/16.
 */
class MultipleInvitations(@field:SerializedName("mode") private val invitationMode: String) {

    @SerializedName("phone_numbers")
    private val phoneNumbers = ArrayList<String>()

    fun addPhoneNumber(phoneNumber: String) {
        phoneNumbers.add(phoneNumber)
    }

}