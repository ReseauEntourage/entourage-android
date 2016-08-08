package social.entourage.android.invite.view;

import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.EntourageBaseAdapter;
import social.entourage.android.map.tour.information.discussion.ViewHolderFactory;

/**
 * Created by mihaiionescu on 08/08/16.
 */
public class InvitationsAdapter extends EntourageBaseAdapter {

    public InvitationsAdapter() {

        ViewHolderFactory.registerViewHolder(
                TimestampedObject.INVITATION_CARD,
                new ViewHolderFactory.ViewHolderType(InvitationCardViewHolder.class, InvitationCardViewHolder.getLayoutResource())
        );

    }

}
