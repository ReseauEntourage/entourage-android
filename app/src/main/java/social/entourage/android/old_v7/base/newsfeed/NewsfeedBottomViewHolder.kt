package social.entourage.android.old_v7.base.newsfeed

import android.view.View
import androidx.fragment.app.findFragment
import kotlinx.android.synthetic.main.layout_newsfeed_bottom_card.view.*
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.old_v7.home.actions.NewsFeedActionsFragment

/**
 * Created by mihaiionescu on 10/05/2017.
 */
class NewsfeedBottomViewHolder(view: View) : BaseCardViewHolder(view) {
    override fun bindFields() {
        itemView.newsfeed_load_more?.setOnClickListener { onNewsfeedLoadMoreEvent(itemView) }
        itemView.newsfeed_tab_events_load_more?.setOnClickListener { onNewsfeedLoadMoreEvent(itemView) }
    }

    private fun onNewsfeedLoadMoreEvent(itemView: View) {
        (itemView.findFragment() as? NewsFeedActionsFragment)?.onNewsfeedLoadMoreRequested()
    }

    override fun populate(data: TimestampedObject) {
        // Does nothing
    }

    fun populate(showContent: Boolean, contentType: Int, selectedTab: NewsfeedTabItem) {
        itemView.newsfeed_bottom_content?.visibility = if (showContent) View.VISIBLE else View.GONE
        if (showContent) {
            // switch between content types
            when(selectedTab) {
                NewsfeedTabItem.ALL_TAB -> {
                    when (contentType) {
                        CONTENT_TYPE_LOAD_MORE -> {
                            itemView.newsfeed_load_more_layout?.visibility = View.VISIBLE
                            itemView.newsfeed_no_items?.visibility = View.GONE
                        }
                        CONTENT_TYPE_NO_ITEMS -> {
                            itemView.newsfeed_load_more_layout?.visibility = View.GONE
                            itemView.newsfeed_no_items?.visibility = View.VISIBLE
                            itemView.newsfeed_no_items?.setText(R.string.map_empty_newsfeed)
                        }
                        CONTENT_TYPE_NO_MORE_ITEMS -> {
                            itemView.newsfeed_load_more_layout?.visibility = View.GONE
                            itemView.newsfeed_no_items?.visibility = View.VISIBLE
                            itemView.newsfeed_no_items?.setText(R.string.newsfeed_no_more_items)
                        }
                        else -> {
                        }
                    }
                    itemView.newsfeed_tab_all_content?.visibility = View.VISIBLE
                    itemView.newsfeed_tab_events_content?.visibility = View.GONE
                }
                NewsfeedTabItem.EVENTS_TAB -> {
                    itemView.newsfeed_tab_all_content?.visibility = View.GONE
                    itemView.newsfeed_tab_events_content?.visibility = if(contentType!= CONTENT_TYPE_NO_MORE_ITEMS) View.VISIBLE else View.GONE
                }
                else -> {}
            }
        }
    }

    companion object {
        const val CONTENT_TYPE_LOAD_MORE = 0
        const val CONTENT_TYPE_NO_ITEMS = 1
        const val CONTENT_TYPE_NO_MORE_ITEMS = 2

        val layoutResource: Int
            get() = R.layout.layout_newsfeed_bottom_card
    }
}