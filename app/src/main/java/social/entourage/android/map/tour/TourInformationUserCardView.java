package social.entourage.android.map.tour;

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
import social.entourage.android.api.model.map.Tour;

/**
 * User Card View in tour information screen
 */
public class TourInformationUserCardView extends LinearLayout {

    private TextView mUsernameView;
    private TextView mJoinStatusView;

    public TourInformationUserCardView(Context context) {
        super(context, null, R.attr.TourInformationUserCardViewStyle);
        init(null, 0);
    }

    public TourInformationUserCardView(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.TourInformationUserCardViewStyle);
        init(attrs, 0);
    }

    public TourInformationUserCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        inflate(getContext(), R.layout.tour_information_user_card_view, this);

        mUsernameView = (TextView) findViewById(R.id.tic_username);
        mJoinStatusView = (TextView) findViewById(R.id.tic_join_status);
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
