package social.entourage.android.api.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import social.entourage.android.api.model.TimestampedObject;

/**
 * It holds an array of Invitations that is used in recycler views
 * Created by Mihai Ionescu on 19/10/2017.
 */

public class InvitationList extends TimestampedObject {

    private List<Invitation> invitationList;

    @Override
    public int getType() {
        return INVITATION_LIST;
    }

    @Override
    public Date getTimestamp() {
        return null;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public String hashString() {
        return "InvitationList";
    }

    public List<Invitation> getInvitationList() {
        if (invitationList == null) return new ArrayList<>();
        return invitationList;
    }

    public void setInvitationList(final List<Invitation> invitationList) {
        this.invitationList = invitationList;
    }
}
