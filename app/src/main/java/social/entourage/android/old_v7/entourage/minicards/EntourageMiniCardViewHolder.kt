package social.entourage.android.old_v7.entourage.minicards

import android.view.View
import social.entourage.android.old_v7.entourage.EntourageViewHolder

/**
 * View Holder for Entourage mini cards that are shown on heatzone tap<br></br>
 * Created by Mihai Ionescu on 05/10/2017.
 * @see EntourageViewHolder
 *
 * @see social.entourage.android.newsfeed.FeedItemViewHolder
 */
class EntourageMiniCardViewHolder(itemView: View) : EntourageViewHolder(itemView) {
    override fun showCategoryIcon(): Boolean {
        return true
    }
}