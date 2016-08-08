package social.entourage.android.invite.view;

import android.view.View;
import android.widget.TextView;

import social.entourage.android.R;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.BaseCardViewHolder;

/**
 * Created by mihaiionescu on 08/08/16.
 */
public class InvitationCardViewHolder extends BaseCardViewHolder {

    private TextView mInviterNameView;

    private long invitationId;

    public InvitationCardViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {

        mInviterNameView = (TextView) itemView.findViewById(R.id.invitation_card_inviter);

    }

    @Override
    public void populate(final TimestampedObject data) {
        populate((Invitation) data);
    }

    private void populate(Invitation invitation) {
        invitationId = invitation.getId();
        mInviterNameView.setText(itemView.getResources().getString(R.string.invitation_card_inviter, invitation.getInviterName()));
    }

    public static int getLayoutResource() {
        return R.layout.layout_invitation_card;
    }
}
