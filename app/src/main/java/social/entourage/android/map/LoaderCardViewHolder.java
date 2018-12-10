package social.entourage.android.map;

import android.view.View;
import android.widget.ProgressBar;
import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.BaseCardViewHolder;

public class LoaderCardViewHolder extends BaseCardViewHolder {
    private  ProgressBar loader;

    public LoaderCardViewHolder(View view) {
        super(view);
    }

    @Override
    protected void bindFields() {
        loader = itemView.findViewById(R.id.layout_loader_card_progressBar);
    }

    @Override
    public void populate(TimestampedObject data) {
        loader.setVisibility(View.VISIBLE);
    }
}
