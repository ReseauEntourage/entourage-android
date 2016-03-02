package social.entourage.android.map.tour.TourInformation.discussion;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import social.entourage.android.api.model.TimestampedObject;

/**
 * Created by mihaiionescu on 02/03/16.
 */
public class BaseCardViewHolder extends RecyclerView.ViewHolder {

    public BaseCardViewHolder(View view) {
        super(view);
    }

    public BaseCardViewHolder newViewHolder(final ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                getLayoutResource(),
                parent,
                false
        );
        BaseCardViewHolder binder = new BaseCardViewHolder(view);
        binder.bind(view);

        return binder;
    };

    protected int getLayoutResource() {
        return 0;
    };

    protected void bind(View view) {};

    public void onBindViewHolder(TimestampedObject data) {};

}
