package social.entourage.android.entourage.information.members

import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.EntourageBaseAdapter
import social.entourage.android.base.ViewHolderFactory.ViewHolderType

/**
 * Created by mihaiionescu on 23/05/16.
 */
class MembersAdapter : EntourageBaseAdapter() {
    init {
        viewHolderFactory.registerViewHolder(
                TimestampedObject.FEED_MEMBER_CARD,
                ViewHolderType(MemberCardViewHolder::class.java, MemberCardViewHolder.layoutResource)
        )
    }
}