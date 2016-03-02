package social.entourage.android.map.tour.TourInformation.discussion;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import social.entourage.android.api.model.TimestampedObject;

/**
 * Created by mihaiionescu on 02/03/16.
 */
abstract public class BaseCardViewHolder extends RecyclerView.ViewHolder {

    public BaseCardViewHolder(View view) {
        super(view);
        bindFields();
    }

    abstract protected void bindFields();

    abstract public void populate(TimestampedObject data);

}
