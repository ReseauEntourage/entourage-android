package social.entourage.android.api.model

import java.util.*

/**
 * It holds an array of Invitations that is used in recycler views
 * Created by Mihai Ionescu on 19/10/2017.
 */
class InvitationList : TimestampedObject() {
    val invitationList: MutableList<Invitation> = ArrayList()

    override val type: Int
        get() = INVITATION_LIST

    override val timestamp: Date
        get() = Date()

    override val id: Long
        get() = 0

    override fun hashString(): String {
        return "InvitationList"
    }

    fun addToInvitationList(invitation: Invitation) {
        invitationList.add(invitation)
    }

    fun removeFromInvitationList(invitation: Invitation) {
        invitationList.remove(invitation)
    }
}