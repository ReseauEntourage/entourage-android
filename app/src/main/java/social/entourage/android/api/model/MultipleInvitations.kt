package social.entourage.android.api.model

import java.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

/**
 * Created by mihaiionescu on 08/08/16.
 */
@KSerializable
class MultipleInvitations(@SerialName("mode") private val invitationMode: String = "") {

    @SerialName("phone_numbers")
    private val phoneNumbers = ArrayList<String>()

    fun addPhoneNumber(phoneNumber: String) {
        phoneNumbers.add(phoneNumber)
    }

}