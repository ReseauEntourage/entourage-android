package social.entourage.android.newsfeed;

import android.view.View;

import social.entourage.android.R;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BottomViewHolder;
import social.entourage.android.tools.BusProvider;

/**
 * Created by mihaiionescu on 10/05/2017.
 */

public class NewsfeedBottomViewHolder extends BottomViewHolder {

    public NewsfeedBottomViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {
        content = itemView.findViewById(R.id.newsfeed_load_more);
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                BusProvider.getInstance().post(new Events.OnNewsfeedLoadMoreEvent());
            }
        });
    }

    public static int getLayoutResource() {
        return R.layout.layout_newsfeed_bottom_card;
    }
}
