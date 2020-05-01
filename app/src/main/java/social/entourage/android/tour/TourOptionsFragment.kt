package social.entourage.android.tour

import android.app.Activity
import kotlinx.android.synthetic.main.layout_entourage_options.*
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.tape.Events.OnFeedItemCloseRequestEvent
import social.entourage.android.entourage.FeedItemOptionsFragment
import social.entourage.android.tools.BusProvider.instance
import social.entourage.android.tools.Utils.getDateStringFromSeconds
import java.util.*

class TourOptionsFragment : FeedItemOptionsFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    override fun initializeView() {
        if (feedItem.isClosed) {
            entourage_option_stop?.setText(R.string.tour_info_options_freeze_tour)
        } else {
            entourage_option_stop?.setText(R.string.tour_info_options_stop_tour)
        }
    }

    override fun getSlideStyle(): Int {
        return R.style.CustomDialogFragmentSlide
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    override fun onStopClicked() {
        if (feedItem.status == FeedItem.STATUS_ON_GOING || feedItem.status == FeedItem.STATUS_OPEN) {
            val tour = feedItem as Tour
            //compute distance
            var distance = 0.0f
            val tourPointsList = tour.tourPoints
            if (tourPointsList.size > 0) {
                var startPoint = tourPointsList[0]
                for (i in 1 until tourPointsList.size) {
                    val p = tourPointsList[i]
                    distance += p.distanceTo(startPoint)
                    startPoint = p
                }
            }
            tour.distance = distance

            //duration
            val now = Date()
            tour.duration = getDateStringFromSeconds(now.time - tour.startTime.time)

            //show stop tour activity
            val activity: Activity? = activity
            if (activity is MainActivity) {
                activity.showStopTourActivity(tour)
            }

            //hide the options
            dismiss()
        } else if (feedItem.status == FeedItem.STATUS_CLOSED) {
            instance.post(OnFeedItemCloseRequestEvent(feedItem, false, true))
            dismiss()
        }
    }

    override fun onEditClicked() {}
}