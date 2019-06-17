package social.entourage.android.newsfeed;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import social.entourage.android.announcement.AnnouncementViewHolder;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.BottomViewHolder;
import social.entourage.android.base.HeaderFooterBaseAdapter;
import social.entourage.android.map.MapViewHolder;
import social.entourage.android.map.entourage.EntourageViewHolder;
import social.entourage.android.map.tour.TourViewHolder;
import social.entourage.android.base.ViewHolderFactory;
import social.entourage.android.map.MapTabItem;

/**
 * Created by mihaiionescu on 05/05/16.
 */
public class NewsfeedAdapter extends HeaderFooterBaseAdapter {

    private MapTabItem selectedTab = MapTabItem.ALL_TAB;

    public NewsfeedAdapter() {
        super();

        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOUR_CARD,
                new ViewHolderFactory.ViewHolderType(TourViewHolder.class, TourViewHolder.getLayoutResource())
        );

        viewHolderFactory.registerViewHolder(
                TimestampedObject.ENTOURAGE_CARD,
                new ViewHolderFactory.ViewHolderType(EntourageViewHolder.class, EntourageViewHolder.getLayoutResource())
        );

        viewHolderFactory.registerViewHolder(
                TimestampedObject.ANNOUNCEMENT_CARD,
                new ViewHolderFactory.ViewHolderType(AnnouncementViewHolder.class, AnnouncementViewHolder.getLayoutResource())
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
    }

    public void showBottomView(final boolean showBottomView, int bottomViewContentType, MapTabItem selectedTab) {
        setSelectedTab(selectedTab);
        showBottomView(showBottomView, bottomViewContentType);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (position == getItemCount() - 1) {
            // map the actual content type using the selected tab
            int contentType = bottomViewContentType + NewsfeedBottomViewHolder.CONTENT_TYPES * selectedTab.getId();
            ((BottomViewHolder)holder).populate(showBottomView, contentType);
            return;
        }
        super.onBindViewHolder(holder, position);
    }

    public void setSelectedTab(MapTabItem selectedTab) {
        this.selectedTab = selectedTab;
        if (mapViewHolder != null) {
            mapViewHolder.setSelectedTab(selectedTab);
        }
    }
}
