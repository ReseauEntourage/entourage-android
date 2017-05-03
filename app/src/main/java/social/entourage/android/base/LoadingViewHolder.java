package social.entourage.android.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;

/**
 * Loading View Holder
 * Created by mihaiionescu on 03/05/2017.
 */

public class LoadingViewHolder extends BaseCardViewHolder {

    ProgressBar progressBar;

    public LoadingViewHolder(final View view) {
        super(view);
    }

    protected void bindFields() {
        progressBar = (ProgressBar)itemView.findViewById(R.id.layout_loader_card_progressBar);
    }

    @Override
    public void populate(final TimestampedObject data) {
        // Does nothing
    }

    public void populate(boolean showLoader) {
        if (progressBar != null) {
            progressBar.setVisibility(showLoader ? View.VISIBLE : View.GONE);
        }
    }

    public static int getLayoutResource() {
        return R.layout.layout_loader_card;
    }

}
