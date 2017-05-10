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

public class BottomViewHolder extends BaseCardViewHolder {

    protected View content;

    public BottomViewHolder(final View view) {
        super(view);
    }

    protected void bindFields() {
        content = (ProgressBar)itemView.findViewById(R.id.layout_loader_card_progressBar);
    }

    @Override
    public void populate(final TimestampedObject data) {
        // Does nothing
    }

    public void populate(boolean showContent) {
        if (content != null) {
            content.setVisibility(showContent ? View.VISIBLE : View.GONE);
        }
    }

    public static int getLayoutResource() {
        return R.layout.layout_loader_card;
    }

}
