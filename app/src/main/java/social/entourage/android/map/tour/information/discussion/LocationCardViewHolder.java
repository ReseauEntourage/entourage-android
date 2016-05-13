package social.entourage.android.map.tour.information.discussion;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourTimestamp;
import social.entourage.android.base.BaseCardViewHolder;

/**
 * Linear Layout that represents a location card in the tour info screen
 */
public class LocationCardViewHolder extends BaseCardViewHolder {

    private TextView mLocationDate;
    private ImageView mLocationImage;
    private TextView mLocationTitle;
    private TextView mLocationDuration;
    private TextView mLocationDistance;

    public LocationCardViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {

        mLocationDate = (TextView) itemView.findViewById(R.id.tic_location_date);
        mLocationImage = (ImageView) itemView.findViewById(R.id.tic_location_image);
        mLocationTitle = (TextView) itemView.findViewById(R.id.tic_location_title);
        mLocationDuration = (TextView) itemView.findViewById(R.id.tic_location_duration);
        mLocationDistance = (TextView) itemView.findViewById(R.id.tic_location_distance);
    }

    @Override
    public void populate(final TimestampedObject data) {
        populate((TourTimestamp) data);
    }

    public void populate(Tour tour, boolean isStartCard) {

        List<TourPoint> tourPointsList = tour.getTourPoints();

        SimpleDateFormat locationDateFormat = new SimpleDateFormat(itemView.getResources().getString(R.string.tour_info_location_card_date_format));
        if (!isStartCard) {
            mLocationDate.setText(locationDateFormat.format(tour.getEndTime()));
            mLocationTitle.setText(R.string.tour_info_text_closed);
            if (tour.isClosed()) {
                if (tour.getStartTime() != null && tour.getEndTime() != null) {
                    Date duration = new Date(tour.getEndTime().getTime() - tour.getStartTime().getTime());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    mLocationDuration.setText(dateFormat.format(duration));
                }
                float distance = 0;
                TourPoint startPoint = tourPointsList.get(0);
                for (int i=1; i < tourPointsList.size(); i++) {
                    TourPoint p = tourPointsList.get(i);
                    distance += p.distanceTo(startPoint);
                    startPoint = p;
                }
                mLocationDistance.setText(String.format("%.2f km", distance/1000.0f));
            }
        } else {
            mLocationDate.setText(locationDateFormat.format(tour.getStartTime()));
            mLocationTitle.setText(R.string.tour_info_text_ongoing);
            if (!tour.isClosed() && tour.getStartTime() != null) {
                Date duration = new Date((new Date()).getTime() - tour.getStartTime().getTime());
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                mLocationDuration.setText(dateFormat.format(duration));
            }
            mLocationDistance.setText("");
        }

    }

    public void populate(TourTimestamp tourTimestamp) {
        SimpleDateFormat locationDateFormat = new SimpleDateFormat(itemView.getResources().getString(R.string.tour_info_location_card_date_format));
        mLocationDate.setText(locationDateFormat.format(tourTimestamp.getDate()));

        if (Tour.TOUR_ON_GOING.equals(tourTimestamp.getStatus())) {
            mLocationTitle.setText(R.string.tour_info_text_ongoing);
        }
        else {
            mLocationTitle.setText(R.string.tour_info_text_closed);
        }

        if (tourTimestamp.getDistance() > 0) {
            mLocationDistance.setText(String.format("%.2f km", tourTimestamp.getDistance()/1000.0f));
            mLocationDistance.setVisibility(View.VISIBLE);
        }
        else {
            mLocationDistance.setVisibility(View.GONE);
        }

        if (tourTimestamp.getDuration() > 0) {
            Date duration = new Date(tourTimestamp.getDuration());
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            mLocationDuration.setText(dateFormat.format(duration));
            mLocationDuration.setVisibility(View.VISIBLE);
        }
        else {
            mLocationDuration.setVisibility(View.GONE);
        }

        if (tourTimestamp.getSnapshot() != null) {
            mLocationImage.setImageBitmap(tourTimestamp.getSnapshot());
            mLocationImage.setVisibility(View.VISIBLE);
        }
        else {
            mLocationImage.setVisibility(View.GONE);
        }
    }

    public static int getLayoutResource() {
        return R.layout.tour_information_location_card_view;
    }

}
