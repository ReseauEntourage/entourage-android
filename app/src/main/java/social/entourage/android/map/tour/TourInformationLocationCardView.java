package social.entourage.android.map.tour;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import social.entourage.android.R;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;

/**
 * Linear Layout that represents a location card in the tour info screen
 */
public class TourInformationLocationCardView extends LinearLayout {

    private TextView mLocationDate;
    private ImageView mLocationImage;
    private TextView mLocationTitle;
    private TextView mLocationDuration;
    private TextView mLocationDistance;

    public TourInformationLocationCardView(Context context) {
        super(context);
        init(null, 0);
    }

    public TourInformationLocationCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TourInformationLocationCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        this.setOrientation(VERTICAL);
        inflate(getContext(), R.layout.tour_information_location_card_view, this);

        mLocationDate = (TextView) this.findViewById(R.id.tic_location_date);
        mLocationImage = (ImageView) this.findViewById(R.id.tic_location_image);
        mLocationTitle = (TextView) this.findViewById(R.id.tic_location_title);
        mLocationDuration = (TextView) this.findViewById(R.id.tic_location_duration);
        mLocationDistance = (TextView) this.findViewById(R.id.tic_location_distance);
    }

    public void populate(Tour tour, boolean isStartCard) {

        List<TourPoint> tourPointsList = tour.getTourPoints();

        SimpleDateFormat locationDateFormat = new SimpleDateFormat(getResources().getString(R.string.tour_info_location_card_date_format));
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

}
