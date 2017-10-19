package social.entourage.android.map.entourage.my;

import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.InvitationList;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.EntourageBaseAdapter;
import social.entourage.android.invite.view.InvitationListViewHolder;
import social.entourage.android.map.entourage.EntourageViewHolder;
import social.entourage.android.map.tour.TourViewHolder;
import social.entourage.android.map.tour.information.discussion.ViewHolderFactory;

/**
 * Created by mihaiionescu on 09/08/16.
 */
public class MyEntouragesAdapter extends EntourageBaseAdapter {

    private InvitationList invitationList;

    public MyEntouragesAdapter() {

        viewHolderFactory.registerViewHolder(
                TimestampedObject.INVITATION_LIST,
                new ViewHolderFactory.ViewHolderType(InvitationListViewHolder.class, InvitationListViewHolder.getLayoutResource())
        );

        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOUR_CARD,
                new ViewHolderFactory.ViewHolderType(TourViewHolder.class, R.layout.layout_myentourages_card)
        );

        viewHolderFactory.registerViewHolder(
                TimestampedObject.ENTOURAGE_CARD,
                new ViewHolderFactory.ViewHolderType(EntourageViewHolder.class, R.layout.layout_myentourages_card)
        );

        setHasStableIds(false);

        invitationList = new InvitationList();
        items.add(invitationList);
    }

    public void setInvitations(List<Invitation> invitations) {
        invitationList.setInvitationList(invitations);
        notifyItemChanged(0);
    }

}
