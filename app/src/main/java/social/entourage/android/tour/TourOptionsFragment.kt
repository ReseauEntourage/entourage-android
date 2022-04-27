package social.entourage.android.tour

import kotlinx.android.synthetic.main.layout_entourage_options.*
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.tape.Events.OnFeedItemCloseRequestEvent
import social.entourage.android.entourage.FeedItemOptionsFragment
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.Utils.getDateStringFromSeconds
import java.util.*

class TourOptionsFragment : FeedItemOptionsFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    override fun initializeView() {
        entourage_option_stop?.setText(if (feedItem.isClosed()) R.string.tour_info_options_freeze_tour else R.string.tour_info_options_stop_tour)
    }

    override val slideStyle: Int
        get() = R.style.CustomDialogFragmentSlide

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    override fun onStopClicked() {
        val tour = feedItem as? Tour ?: return
        if (tour.isOpen()) {
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
            tour.duration = getDateStringFromSeconds(now.time - tour.getStartTime().time)

            //show stop tour activity
            (activity as? MainActivity)?.showStopTourFragment(tour)

            //hide the options
            dismiss()
        } else if (tour.isClosed()) {
            EntBus.post(OnFeedItemCloseRequestEvent(tour, showUI = false, success = true,""))
            dismiss()
        }
    }

    override fun onEditClicked() {}
}