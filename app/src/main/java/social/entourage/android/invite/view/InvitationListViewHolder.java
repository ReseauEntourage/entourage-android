package social.entourage.android.invite.view;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.InvitationList;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.BaseCardViewHolder;

/**
 * Created by Mihai Ionescu on 19/10/2017.
 */

public class InvitationListViewHolder extends BaseCardViewHolder {

    private RecyclerView invitationsView;
    private InvitationsAdapter invitationsAdapter;

    public InvitationListViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {
        invitationsView = itemView.findViewById(R.id.invitation_list_recycler_view);

        invitationsView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        invitationsAdapter = new InvitationsAdapter();
        invitationsView.setAdapter(invitationsAdapter);
    }

    @Override
    public void populate(final TimestampedObject data) {
        populate((InvitationList)data);
    }

    private void populate(InvitationList invitationList) {
        if (invitationsAdapter != null) {
            List<TimestampedObject> list = new ArrayList<>(invitationList.getInvitationList());
            invitationsAdapter.removeAll();
            invitationsAdapter.addItems(list);
        }
    }

    public static int getLayoutResource() {
        return R.layout.layout_invitation_list_card;
    }
}
