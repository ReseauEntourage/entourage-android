package social.entourage.android.base;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.map.MapTabItem;
import social.entourage.android.newsfeed.NewsfeedBottomViewHolder;

public class HeaderFooterBaseAdapter extends HeaderBaseAdapter {
    private boolean showBottomView = false;
    private int bottomViewContentType;
    private MapTabItem selectedTab = MapTabItem.ALL_TAB;

    public void setSelectedTab(MapTabItem selectedTab) {
        this.selectedTab = selectedTab;
        if (mapViewHolder != null) {
            mapViewHolder.setSelectedTab(selectedTab);
        }
    }

    public void showBottomView(final boolean showBottomView, int bottomViewContentType, MapTabItem selectedTab) {
        setSelectedTab(selectedTab);
        this.showBottomView = showBottomView;
        this.bottomViewContentType = bottomViewContentType;
        if (items != null) {
            notifyItemChanged(getBottomViewPosition());
        }
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

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size() + getPositionOffset() + 1; // +1 for the footer
    }

    @Override
    public int getItemViewType(final int position) {
        if (position == getItemCount() - 1) {
            return TimestampedObject.BOTTOM_VIEW;
        }
        return super.getItemViewType(position);
    }

    private int getBottomViewPosition() {
        if (items != null) {
            return items.size() + getPositionOffset();
        }
        return 0;
    }

}
