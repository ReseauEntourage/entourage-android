package social.entourage.android.newsfeed;

import social.entourage.android.base.EntourageBaseAdapter;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.BottomViewHolder;
import social.entourage.android.map.MapViewHolder;
import social.entourage.android.map.entourage.EntourageViewHolder;
import social.entourage.android.map.tour.TourViewHolder;
import social.entourage.android.map.tour.information.discussion.ViewHolderFactory;

/**
 * Created by mihaiionescu on 05/05/16.
 */
public class NewsfeedAdapter extends EntourageBaseAdapter {

    public NewsfeedAdapter() {

        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOUR_CARD,
                new ViewHolderFactory.ViewHolderType(TourViewHolder.class, TourViewHolder.getLayoutResource())
        );

        viewHolderFactory.registerViewHolder(
                TimestampedObject.ENTOURAGE_CARD,
                new ViewHolderFactory.ViewHolderType(EntourageViewHolder.class, EntourageViewHolder.getLayoutResource())
        );

        viewHolderFactory.registerViewHolder(
                TimestampedObject.BOTTOM_VIEW,
                new ViewHolderFactory.ViewHolderType(NewsfeedBottomViewHolder.class, NewsfeedBottomViewHolder.getLayoutResource())
        );

        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOP_VIEW,
                new ViewHolderFactory.ViewHolderType(MapViewHolder.class, MapViewHolder.getLayoutResource())
        );

        setHasStableIds(false);
        needsTopView = true;
        needsBottomView = true;
    }
}
