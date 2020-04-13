package social.entourage.android.entourage

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import social.entourage.android.R
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.api.tape.Events.OnFeedItemCloseRequestEvent
import social.entourage.android.tools.BusProvider

/**
 * Created by Mihai Ionescu on 03/08/2018.
 */
class EntourageCloseFragment  {
    // ----------------------------------
    // Lifecycle
    // ----------------------------------
   private var feedItem: FeedItem? = null
    fun show(fragmentManager: FragmentManager?, tag: String?, context: Context?) {
        val builder = AlertDialog.Builder(context!!)
        builder.setMessage(R.string.entourage_close_alert_description)
                .setPositiveButton(R.string.yes) { dialog, which -> BusProvider.instance.post(OnFeedItemCloseRequestEvent(feedItem, false, true)) }
                .setNegativeButton(R.string.no, null)
        builder.create().show()
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        val TAG = EntourageCloseFragment::class.java.simpleName

        fun newInstance(feedItem: FeedItem?): EntourageCloseFragment {
            val fragment = EntourageCloseFragment()
            fragment.feedItem = feedItem
            return fragment
        }
    }
}