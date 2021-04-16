package social.entourage.android.entourage.my.invitations

import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.BaseAdapter
import social.entourage.android.base.ViewHolderFactory.ViewHolderType
import social.entourage.android.entourage.my.invitations.InvitationCardViewHolder.Companion.layoutResource

/**
 * Created by mihaiionescu on 08/08/16.
 */
class InvitationsAdapter : BaseAdapter() {
    init {
        viewHolderFactory.registerViewHolder(
                TimestampedObject.INVITATION_CARD,
                ViewHolderType(InvitationCardViewHolder::class.java, layoutResource)
        )
    }
}