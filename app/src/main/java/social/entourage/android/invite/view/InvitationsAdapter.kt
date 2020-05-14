package social.entourage.android.invite.view

import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.EntourageBaseAdapter
import social.entourage.android.base.ViewHolderFactory.ViewHolderType
import social.entourage.android.invite.view.InvitationCardViewHolder.Companion.layoutResource

/**
 * Created by mihaiionescu on 08/08/16.
 */
class InvitationsAdapter : EntourageBaseAdapter() {
    init {
        viewHolderFactory.registerViewHolder(
                TimestampedObject.INVITATION_CARD,
                ViewHolderType(InvitationCardViewHolder::class.java, layoutResource)
        )
    }
}