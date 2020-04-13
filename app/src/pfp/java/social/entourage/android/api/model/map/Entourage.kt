package social.entourage.android.api.model.map

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import social.entourage.android.R
import java.io.Serializable

/**
 * PFP Entourage
 * Created by Mihai Ionescu on 05/06/2018.
 */
class Entourage : BaseEntourage, Serializable {
    // ----------------------------------
    // Constructors
    // ----------------------------------
    constructor(entourageType: String?, category: String?, title: String?, description: String?, location: TourPoint?) : super(entourageType, category, title, description, location) {}

    // ----------------------------------
    // FeedItem overrides
    // ----------------------------------
    override fun getIconDrawable(context: Context): Drawable {
        return if (TYPE_PRIVATE_CIRCLE.equals(groupType, ignoreCase = true)) {
            AppCompatResources.getDrawable(context, R.drawable.ic_heart)!!
        } else super.getIconDrawable(context)
    }

    override fun showHeatmapAsOverlay(): Boolean {
        return if (TYPE_PRIVATE_CIRCLE.equals(groupType, ignoreCase = true)) {
            false
        } else super.showHeatmapAsOverlay()
    }

    override fun getHeatmapResourceId(): Int {
        return if (TYPE_PRIVATE_CIRCLE.equals(groupType, ignoreCase = true)) {
            R.drawable.ic_heart_marker
        } else super.getHeatmapResourceId()
    }

    override fun canBeClosed(): Boolean {
        return if (TYPE_PRIVATE_CIRCLE.equals(groupType, ignoreCase = true)) {
            false
        } else super.canBeClosed()
    }

    override fun showAuthor(): Boolean {
        return if (TYPE_PRIVATE_CIRCLE.equals(groupType, ignoreCase = true)) {
            false
        } else super.showAuthor()
    }

    override fun getJoinRequestButton(): Int {
        return if (TYPE_OUTING.equals(groupType, ignoreCase = true)) R.string.tour_info_request_join_button_outing else super.getJoinRequestButton()
    }

    override fun getFeedTypeLong(context: Context): String {
        return if (TYPE_PRIVATE_CIRCLE.equals(groupType, ignoreCase = true)) {
            context.getString(R.string.entourage_type_private_circle)
        } else super.getFeedTypeLong(context)
    }

    override fun getFeedTypeColor(): Int {
        return if (TYPE_PRIVATE_CIRCLE.equals(groupType, ignoreCase = true)) {
            R.color.action_type_private_circle
        } else super.getFeedTypeColor()
    }

    override fun showInviteViewAfterCreation(): Boolean {
        return false
    }

    override fun showEditEntourageView(): Boolean {
        return if (TYPE_PRIVATE_CIRCLE.equals(groupType, ignoreCase = true) || TYPE_NEIGHBORHOOD.equals(groupType, ignoreCase = true)) {
            false
        } else super.showEditEntourageView()
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        private const val serialVersionUID = -7858700650513499498L
    }
}