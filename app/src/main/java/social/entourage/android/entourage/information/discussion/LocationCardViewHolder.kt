package social.entourage.android.entourage.information.discussion

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.tour_information_location_card_view.view.*
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.api.model.map.Tour
import social.entourage.android.api.model.map.TourTimestamp
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
        populate(data as TourTimestamp)
    }

    fun populate(tour: Tour, isStartCard: Boolean) {
        val tourPointsList = tour.tourPoints
        val locationDateFormat = SimpleDateFormat(itemView.resources.getString(R.string.tour_info_location_card_date_format), Locale.FRANCE)
        if (!isStartCard) {
            itemView.tic_location_date?.text = locationDateFormat.format(tour.endTime).toUpperCase()
            itemView.tic_location_title?.setText(R.string.tour_info_text_closed)
            if (tour.isClosed) {
                if (tour.startTime != null && tour.endTime != null) {
                    itemView.tic_location_duration?.text = Utils.getDateStringFromSeconds(tour.endTime.time - tour.startTime.time)
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
            itemView.tic_location_date?.text = locationDateFormat.format(tour.startTime).toUpperCase()
            itemView.tic_location_title?.setText(R.string.tour_info_text_ongoing)
            if (!tour.isClosed && tour.startTime != null) {
                itemView.tic_location_duration?.text = Utils.getDateStringFromSeconds(Date().time - tour.startTime.time)
            }
            itemView.tic_location_distance?.text = ""
        }
    }

    fun populate(tourTimestamp: TourTimestamp) {
        if (tourTimestamp.date != null) {
            val locationDateFormat = SimpleDateFormat(itemView.resources.getString(R.string.tour_info_location_card_date_format), Locale.FRANCE)
            itemView.tic_location_date?.text = locationDateFormat.format(tourTimestamp.date).toUpperCase()
        }
        if (FeedItem.STATUS_ON_GOING == tourTimestamp.status || FeedItem.STATUS_OPEN == tourTimestamp.status) {
            if (tourTimestamp.feedType == TimestampedObject.TOUR_CARD) {
                itemView.tic_location_title?.setText(R.string.tour_info_text_ongoing)
            } else if (tourTimestamp.feedType == TimestampedObject.ENTOURAGE_CARD) {
                itemView.tic_location_title?.setText(R.string.entourage_info_text_open)
            }
        } else {
            if (tourTimestamp.feedType == TimestampedObject.TOUR_CARD) {
                itemView.tic_location_title?.setText(R.string.tour_info_text_closed)
            } else if (tourTimestamp.feedType == TimestampedObject.ENTOURAGE_CARD) {
                itemView.tic_location_title?.setText(R.string.entourage_info_text_close)
            }
        }
        if (tourTimestamp.distance > 0) {
            itemView.tic_location_distance?.text = String.format("%.2f km", tourTimestamp.distance / 1000.0f)
            itemView.tic_location_distance?.visibility = View.VISIBLE
        } else {
            itemView.tic_location_distance?.visibility = View.GONE
        }
        if (tourTimestamp.duration > 0) {
            itemView.tic_location_duration?.text = Utils.getDateStringFromSeconds(tourTimestamp.duration)
            itemView.tic_location_duration?.visibility = View.VISIBLE
        } else {
            itemView.tic_location_duration?.visibility = View.GONE
        }
        if (tourTimestamp.snapshot != null) {
            itemView.tic_location_image?.setImageBitmap(tourTimestamp.snapshot)
            itemView.tic_location_image?.visibility = View.VISIBLE
        } else {
            itemView.tic_location_image?.visibility = View.GONE
        }
    }

    companion object {
        @JvmStatic
        val layoutResource: Int
            get() = R.layout.tour_information_location_card_view
    }
}