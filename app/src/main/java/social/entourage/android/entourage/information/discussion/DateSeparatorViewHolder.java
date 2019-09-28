package social.entourage.android.entourage.information.discussion;

import android.view.View;
import android.widget.TextView;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.tools.Utils;

/**
 * Created by mihaiionescu on 15/03/2017.
 */

public class DateSeparatorViewHolder extends BaseCardViewHolder {

    private TextView mTimestampView;

    public DateSeparatorViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {
        mTimestampView = itemView.findViewById(R.id.tic_date_separator_timestamp);
    }

    @Override
    public void populate(final TimestampedObject data) {
        if (data == null || mTimestampView == null) return;
        mTimestampView.setText(Utils.dateAsStringFromNow(data.getTimestamp(), itemView.getContext()));
    }

    public static int getLayoutResource() {
        return R.layout.tour_information_date_separator_card;
    }

}
