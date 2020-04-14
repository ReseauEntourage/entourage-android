package social.entourage.android.newsfeed;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import social.entourage.android.announcement.AnnouncementViewHolder;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.BottomViewHolder;
import social.entourage.android.base.HeaderFooterBaseAdapter;
import social.entourage.android.map.MapViewHolder;
import social.entourage.android.entourage.EntourageViewHolder;
import social.entourage.android.tour.TourViewHolder;
import social.entourage.android.base.ViewHolderFactory;

/**
 * Created by mihaiionescu on 05/05/16.
 */
public class NewsfeedAdapter extends HeaderFooterBaseAdapter {

    private NewsfeedTabItem selectedTab = NewsfeedTabItem.ALL_TAB;

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

    private int getContentType(NewsfeedTabItem selectedTab) {
        return bottomViewContentType + NewsfeedBottomViewHolder.CONTENT_TYPES * selectedTab.getId();
    }

    public void showBottomView(final boolean showBottomView, int bottomViewContentType, NewsfeedTabItem selectedTab) {
        setSelectedTab(selectedTab);
        showBottomView(showBottomView, bottomViewContentType);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (position == getItemCount() - 1) {
            // map the actual content type using the selected tab
            ((BottomViewHolder)holder).populate(showBottomView, getContentType(selectedTab));
            return;
        }
        super.onBindViewHolder(holder, position);
    }

    private void setSelectedTab(NewsfeedTabItem selectedTab) {
        this.selectedTab = selectedTab;
    }
}
