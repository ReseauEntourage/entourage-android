package social.entourage.android.map.tour.information.members;

import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.EntourageBaseAdapter;
import social.entourage.android.map.tour.information.discussion.ViewHolderFactory;

/**
 * Created by mihaiionescu on 23/05/16.
 */
public class MembersAdapter extends EntourageBaseAdapter {

    public MembersAdapter() {
        ViewHolderFactory.registerViewHolder(
                TimestampedObject.FEED_MEMBER_CARD,
                new ViewHolderFactory.ViewHolderType(MemberCardViewHolder.class, MemberCardViewHolder.getLayoutResource())
        );
    }
}
