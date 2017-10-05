package social.entourage.android.map.entourage.minicards;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.base.EntourageBaseAdapter;
import social.entourage.android.map.entourage.EntourageViewHolder;
import social.entourage.android.map.tour.information.discussion.ViewHolderFactory;

/**
 * Adapter to be used with Entourage Mini Cards RecyclerView<br/>
 * It handles only Entourage Mini Cards
 * Created by Mihai Ionescu on 13/09/2017.
 */

public class EntourageMiniCardsAdapter extends EntourageBaseAdapter {

    private static final float MINICARD_WIDTH_PERCENTAGE = 0.85f;

    public EntourageMiniCardsAdapter() {

        viewHolderFactory.registerViewHolder(
                TimestampedObject.ENTOURAGE_CARD,
                new ViewHolderFactory.ViewHolderType(EntourageMiniCardViewHolder.class, R.layout.layout_entourage_mini_card)
        );

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {

        BaseCardViewHolder cardViewHolder = viewHolderFactory.getViewHolder(parent, viewType);
        if (cardViewHolder != null) {
            // Make the width of the mini card to be a percentage of the parent
            ViewGroup.LayoutParams params = cardViewHolder.itemView.getLayoutParams();
            params.width = (int) (parent.getWidth() * MINICARD_WIDTH_PERCENTAGE);
            cardViewHolder.itemView.setLayoutParams(params);

            cardViewHolder.setViewHolderListener(viewHolderListener);
        }

        return cardViewHolder;
    }

}
