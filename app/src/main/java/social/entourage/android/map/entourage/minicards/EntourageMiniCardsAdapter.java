package social.entourage.android.map.entourage.minicards;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.EntourageBaseAdapter;
import social.entourage.android.map.entourage.EntourageViewHolder;
import social.entourage.android.map.tour.information.discussion.ViewHolderFactory;

/**
 * Adapter to be used with Entourage Mini Cards RecyclerView
 * It handles only Entourage Mini Cards
 * Created by Mihai Ionescu on 13/09/2017.
 */

public class EntourageMiniCardsAdapter extends EntourageBaseAdapter {

    public EntourageMiniCardsAdapter() {

        viewHolderFactory.registerViewHolder(
                TimestampedObject.ENTOURAGE_CARD,
                new ViewHolderFactory.ViewHolderType(EntourageViewHolder.class, R.layout.layout_entourage_mini_card)
        );

    }

}
