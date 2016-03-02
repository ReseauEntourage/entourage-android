package social.entourage.android.map.tour.TourInformation.discussion;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourUser;

/**
 * User Card View in tour information screen
 */
public class UserCardViewHolder extends LinearLayout {

    private TextView mUsernameView;
    private TextView mJoinStatusView;

    public UserCardViewHolder(Context context) {
        super(context, null, R.attr.TourInformationCardViewStyle);
        init(null, 0);
    }

    public UserCardViewHolder(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.TourInformationCardViewStyle);
        init(attrs, 0);
    }

    public UserCardViewHolder(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        inflate(getContext(), R.layout.tour_information_user_card_view, this);

        mUsernameView = (TextView) findViewById(R.id.tic_username);
        mJoinStatusView = (TextView) findViewById(R.id.tic_join_status);
    }

    public void populate(TourUser user) {
        setUsername(user.getFirstName());
        setJoinStatus(user.getStatus());
    }

    public void setUsername(String username) {
        mUsernameView.setText(username);
    }

    public void setJoinStatus(String joinStatus) {
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

}
