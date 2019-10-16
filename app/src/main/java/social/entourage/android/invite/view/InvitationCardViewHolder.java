package social.entourage.android.invite.view;

import android.view.View;
import android.widget.TextView;

import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.tools.BusProvider;

/**
 * Created by mihaiionescu on 08/08/16.
 */
public class InvitationCardViewHolder extends BaseCardViewHolder {

    private TextView mInviterNameView;

    private long invitationId = 0;
    private String entourageUUID = "";

    public InvitationCardViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {

        mInviterNameView = itemView.findViewById(R.id.invitation_card_inviter);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (entourageUUID == null || entourageUUID.length() == 0 || invitationId == 0) return;
                EntourageEvents.logEvent(EntourageEvents.EVENT_MYENTOURAGES_BANNER_CLICK);
                BusProvider.getInstance().post(new Events.OnFeedItemInfoViewRequestedEvent(FeedItem.ENTOURAGE_CARD, entourageUUID, invitationId));
            }
        });

    }

    @Override
    public void populate(final TimestampedObject data) {
        populate((Invitation) data);
    }

    private void populate(Invitation invitation) {
        invitationId = invitation.getId();
        entourageUUID = invitation.getEntourageUUID();

        mInviterNameView.setText(itemView.getResources().getString(R.string.invitation_card_inviter, invitation.getInviterName()));
    }

    public static int getLayoutResource() {
        return R.layout.layout_invitation_card;
    }
}
