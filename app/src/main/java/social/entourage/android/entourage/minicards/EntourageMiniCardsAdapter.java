package social.entourage.android.entourage.minicards;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.base.EntourageBaseAdapter;
import social.entourage.android.base.ViewHolderFactory;

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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {

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

    public void addItems(List<TimestampedObject> addItems) {
        items.clear();
        items.addAll(addItems);
        notifyDataSetChanged();
    }

}