package social.entourage.android.entourage.information.discussion;

import android.view.View;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.BaseCardViewHolder;

/**
 * Created by mihaiionescu on 02/03/16.
 */
public class SeparatorCardViewHolder extends BaseCardViewHolder {

    public SeparatorCardViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {

    }

    @Override
    public void populate(final TimestampedObject data) {

    }

    public static int getLayoutResource() {
        return R.layout.tour_information_separator_card;
    }
}
