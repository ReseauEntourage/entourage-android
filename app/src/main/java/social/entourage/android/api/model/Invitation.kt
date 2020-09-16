package social.entourage.android.api.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by mihaiionescu on 12/07/16.
 */
class Invitation (
        @field:SerializedName("invitation_mode") var invitationMode: String,
        @field:SerializedName("phone_number") var phoneNumber: String
) : TimestampedObject() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @SerializedName("id")
    @Expose(serialize = false, deserialize = true)
    private var invitationId: Long = 0

    @SerializedName("inviter")
    @Expose(serialize = false, deserialize = true)
    var inviter: User? = null

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------
    @SerializedName("entourage_id")
    @Expose(serialize = false, deserialize = true)
    var entourageId = 0

    var entourage: BaseEntourage? = null

    @SerializedName("status")
    @Expose(serialize = false, deserialize = true)
    var status: String? = null

    //TODO Retrieve it from the server
    val entourageUUID: String
        get() = entourageId.toString()

    override val id: Long
        get() = invitationId

    /*fun setId(invitationId: Long) {
        this.invitationId = invitationId
    }*/

    val inviterName: String
        get() = inviter?.displayName ?: ""

    override val timestamp: Date
        get()  = Date()

    override fun hashString(): String {
        return HASH_STRING_HEAD + invitationId
    }

    override val type: Int
        get() = INVITATION_CARD

    override fun equals(other: Any?): Boolean {
        return !(other == null || other.javaClass != this.javaClass) && invitationId == (other as Invitation).invitationId
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val INVITE_BY_SMS = "SMS"
        private const val HASH_STRING_HEAD = "Invitation-"
        const val STATUS_ACCEPTED = "accepted"
        const val STATUS_PENDING = "pending"
        const val STATUS_REJECTED = "rejected"
    }

}