package social.entourage.android.old_v7.entourage.information.members

import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.BaseAdapter
import social.entourage.android.base.ViewHolderFactory.ViewHolderType

/**
 * Created by mihaiionescu on 23/05/16.
 */
class MembersAdapter : BaseAdapter() {
    init {
        viewHolderFactory.registerViewHolder(
                TimestampedObject.FEED_MEMBER_CARD,
                ViewHolderType(
                    MemberCardViewHolder::class.java,
                    MemberCardViewHolder.layoutResource
                )
        )
    }
}