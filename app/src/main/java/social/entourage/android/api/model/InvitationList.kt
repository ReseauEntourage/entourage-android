package social.entourage.android.api.model

import java.util.*

/**
 * It holds an array of Invitations that is used in recycler views
 * Created by Mihai Ionescu on 19/10/2017.
 */
class InvitationList : TimestampedObject() {
    val invitationList: MutableList<Invitation> = ArrayList()

    override fun getType(): Int {
        return INVITATION_LIST
    }

    override fun getTimestamp(): Date {
        return Date()
    }

    override fun getId(): Long {
        return 0
    }

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