package social.entourage.android.api.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import social.entourage.android.api.model.map.BaseEntourage
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

    override fun getId(): Long {
        return invitationId
    }

    /*fun setId(invitationId: Long) {
        this.invitationId = invitationId
    }*/

    val inviterName: String
        get() = if (inviter == null) "" else inviter!!.displayName

    override fun getTimestamp(): Date {
        return Date()
    }

    override fun hashString(): String {
        return HASH_STRING_HEAD + invitationId
    }

    override fun getType(): Int {
        return INVITATION_CARD
    }

    override fun equals(other: Any?): Boolean {
        return !(other == null || other.javaClass != this.javaClass) && invitationId == (other as Invitation).invitationId
    }

    // ----------------------------------
    // WRAPPERS
    // ----------------------------------
    class InvitationWrapper(@field:SerializedName("invite") var invitation: Invitation)

    class InvitationsWrapper {
        @SerializedName("invitations")
        var invitations: List<Invitation>? = null

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