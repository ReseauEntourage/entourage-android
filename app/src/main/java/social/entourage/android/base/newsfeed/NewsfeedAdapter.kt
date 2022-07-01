package social.entourage.android.base.newsfeed

import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.HeaderFooterBaseAdapter
import social.entourage.android.base.ViewHolderFactory.ViewHolderType
import social.entourage.android.base.map.MapViewHolder
import social.entourage.android.base.newsfeed.announcement.AnnouncementViewHolder

/**
 * Created by mihaiionescu on 05/05/16.
 */
class NewsfeedAdapter : HeaderFooterBaseAdapter() {
    private var selectedTab = NewsfeedTabItem.ALL_TAB
    fun showBottomView(showBottomView: Boolean, bottomViewContentType: Int, selectedTab: NewsfeedTabItem) {
        setSelectedTab(selectedTab)
        showBottomView(showBottomView, bottomViewContentType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == itemCount - 1) {
            // map the actual content type using the selected tab
            (holder as NewsfeedBottomViewHolder).populate(showBottomView, bottomViewContentType, selectedTab)
            return
        }
        super.onBindViewHolder(holder, position)
    }

    private fun setSelectedTab(selectedTab: NewsfeedTabItem) {
        this.selectedTab = selectedTab
    }

    init {
        viewHolderFactory.registerViewHolder(
                TimestampedObject.ENTOURAGE_CARD,
                ViewHolderType(FeedItemViewHolderRenew::class.java, FeedItemViewHolderRenew.layoutResource)
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.ANNOUNCEMENT_CARD,
                ViewHolderType(AnnouncementViewHolder::class.java, AnnouncementViewHolder.layoutResource)
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.BOTTOM_VIEW,
                ViewHolderType(
                    NewsfeedBottomViewHolder::class.java,
                    NewsfeedBottomViewHolder.layoutResource
                )
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOP_VIEW,
                ViewHolderType(MapViewHolder::class.java, MapViewHolder.layoutResource)
        )
        setHasStableIds(false)
    }
}