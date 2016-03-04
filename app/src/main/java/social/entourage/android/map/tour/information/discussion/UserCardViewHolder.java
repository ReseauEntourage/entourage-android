package social.entourage.android.map.tour.information.discussion;

import android.view.View;
import android.widget.TextView;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

/**
 * User Card View in tour information screen
 */
public class UserCardViewHolder extends BaseCardViewHolder {

    private TextView mUsernameView;
    private TextView mJoinStatusView;

    private int userId;

    public UserCardViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {
        mUsernameView = (TextView) itemView.findViewById(R.id.tic_username);
        mJoinStatusView = (TextView) itemView.findViewById(R.id.tic_join_status);

        mUsernameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (userId == 0) return;
                    BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(userId));
                }
            }
        );
    }

    @Override
    public void populate(final TimestampedObject data) {
        this.populate((TourUser)data);
    }

    public void populate(TourUser user) {
        setUsername(user.getFirstName());
        setJoinStatus(user.getStatus());

        userId = user.getUserId();
    }

    private void setUsername(String username) {
        mUsernameView.setText(username);
    }

    private void setJoinStatus(String joinStatus) {
        if (joinStatus.equals(Tour.JOIN_STATUS_ACCEPTED)) {
            mJoinStatusView.setText(R.string.tour_info_text_join_accepted);
        }
        else if (joinStatus.equals(Tour.JOIN_STATUS_REJECTED)) {
            mJoinStatusView.setText(R.string.tour_info_text_join_rejected);
        }
        else if (joinStatus.equals(Tour.JOIN_STATUS_PENDING)) {
            mJoinStatusView.setText(R.string.tour_info_text_join_pending);
        }
        else {
            mJoinStatusView.setText("");
        }
    }

    public static int getLayoutResource() {
        return R.layout.tour_information_user_card_view;
    }

}
