package social.entourage.android.map.entourage.minicards;

import android.view.View;

import social.entourage.android.map.entourage.EntourageViewHolder;

/**
 * View Holder for Entourage mini cards that are shown on heatzone tap<br/>
 * Created by Mihai Ionescu on 05/10/2017.
 * @see EntourageViewHolder
 * @see social.entourage.android.newsfeed.FeedItemViewHolder
 */

public class EntourageMiniCardViewHolder extends EntourageViewHolder {

    public EntourageMiniCardViewHolder(final View itemView) {
        super(itemView);
    }

    @Override
    protected boolean showCategoryIcon() {
        return false;
    }
}
