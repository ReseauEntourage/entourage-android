package social.entourage.android.entourage.information.discussion

import android.view.View
import kotlinx.android.synthetic.main.layout_tour_information_location_card_view.view.*
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.model.tour.TourInformation
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.tools.Utils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Linear Layout that represents a location card in the tour info screen
 */
class LocationCardViewHolder(view: View) : BaseCardViewHolder(view) {

    override fun bindFields() {}

    override fun populate(data: TimestampedObject) {
        populate(data as TourInformation)
    }

    private fun populate(tour: Tour, isStartCard: Boolean) {
        val tourPointsList = tour.tourPoints
        val locationDateFormat = SimpleDateFormat(itemView.resources.getString(R.string.tour_info_location_card_date_format), Locale.FRANCE)
        if (!isStartCard) {
            itemView.tic_location_date?.text = locationDateFormat.format(tour.getEndTime() ?: Date()).toUpperCase(Locale.ROOT)
            itemView.tic_location_title?.setText(R.string.tour_info_text_closed)
            if (tour.isClosed()) {
                tour.getEndTime()?.let {endTime->
                    itemView.tic_location_duration?.text = Utils.getDateStringFromSeconds(endTime.time - tour.getStartTime().time)
                }
                var distance = 0f
                var startPoint = tourPointsList[0]
                for (i in 1 until tourPointsList.size) {
                    val p = tourPointsList[i]
                    distance += p.distanceTo(startPoint)
                    startPoint = p
                }
                itemView.tic_location_distance?.text = String.format("%.2f km", distance / 1000.0f)
            }
        } else {
            itemView.tic_location_date?.text = locationDateFormat.format(tour.getStartTime()).toUpperCase(Locale.ROOT)
            itemView.tic_location_title?.setText(R.string.tour_info_text_ongoing)
            if (!tour.isClosed()) {
                itemView.tic_location_duration?.text = Utils.getDateStringFromSeconds(Date().time - tour.getStartTime().time)
            }
            itemView.tic_location_distance?.text = ""
        }
    }

    fun populate(tourInformation: TourInformation) {
        val locationDateFormat = SimpleDateFormat(itemView.resources.getString(R.string.tour_info_location_card_date_format), Locale.FRANCE)
        itemView.tic_location_date?.text = locationDateFormat.format(tourInformation.startDate).toUpperCase(Locale.ROOT)
        if (FeedItem.STATUS_ON_GOING == tourInformation.status || FeedItem.STATUS_OPEN == tourInformation.status) {
            if (tourInformation.feedType == TimestampedObject.TOUR_CARD) {
                itemView.tic_location_title?.setText(R.string.tour_info_text_ongoing)
            } else if (tourInformation.feedType == TimestampedObject.ENTOURAGE_CARD) {
                itemView.tic_location_title?.setText(R.string.entourage_info_text_open)
            }
        } else {
            if (tourInformation.feedType == TimestampedObject.TOUR_CARD) {
                itemView.tic_location_title?.setText(R.string.tour_info_text_closed)
            } else if (tourInformation.feedType == TimestampedObject.ENTOURAGE_CARD) {
                itemView.tic_location_title?.setText(R.string.entourage_info_text_close)
            }
        }
        if (tourInformation.distance > 0) {
            itemView.tic_location_distance?.text = String.format("%.2f km", tourInformation.distance / 1000.0f)
            itemView.tic_location_distance?.visibility = View.VISIBLE
        } else {
            itemView.tic_location_distance?.visibility = View.GONE
        }
        if (tourInformation.duration > 0) {
            itemView.tic_location_duration?.text = Utils.getDateStringFromSeconds(tourInformation.duration)
            itemView.tic_location_duration?.visibility = View.VISIBLE
        } else {
            itemView.tic_location_duration?.visibility = View.GONE
        }
        if (tourInformation.snapshot != null) {
            itemView.tic_location_image?.setImageBitmap(tourInformation.snapshot)
            itemView.tic_location_image?.visibility = View.VISIBLE
        } else {
            itemView.tic_location_image?.visibility = View.GONE
        }
    }

    companion object {
        @JvmStatic
        val layoutResource: Int
            get() = R.layout.layout_tour_information_location_card_view
    }
}