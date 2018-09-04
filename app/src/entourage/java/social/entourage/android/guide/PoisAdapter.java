package social.entourage.android.guide;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.EntourageBaseAdapter;
import social.entourage.android.guide.poi.PoiViewHolder;
import social.entourage.android.map.MapViewHolder;
import social.entourage.android.map.tour.information.discussion.ViewHolderFactory;

/**
 * Point of interest adapter
 *
 * Created by mihaiionescu on 26/04/2017.
 */

public class PoisAdapter extends EntourageBaseAdapter {

    public PoisAdapter() {

        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOP_VIEW,
                new ViewHolderFactory.ViewHolderType(MapViewHolder.class, R.layout.layout_feed_map_full_card)
        );

        viewHolderFactory.registerViewHolder(
                TimestampedObject.GUIDE_POI,
                new ViewHolderFactory.ViewHolderType(PoiViewHolder.class, PoiViewHolder.getLayoutResource())
        );

        setHasStableIds(false);
        needsTopView = true;
    }
}
