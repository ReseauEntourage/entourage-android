package social.entourage.android.map.tour;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

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

    public void populate(Tour tour, int tourPointIndex) {
        if (tourPointIndex < 0) return;
        List<TourPoint> tourPointsList = tour.getTourPoints();
        if (tourPointIndex >= tourPointsList.size()) return;

        SimpleDateFormat locationDateFormat = new SimpleDateFormat(getResources().getString(R.string.tour_info_location_card_date_format));
        if (tourPointIndex == tourPointsList.size()-1) {
            mLocationDate.setText(locationDateFormat.format(tour.getEndTime()));
            mLocationTitle.setText(R.string.tour_info_text_closed);
        } else {
            mLocationDate.setText(locationDateFormat.format(tour.getStartTime()));
            mLocationTitle.setText(R.string.tour_info_text_ongoing);
        }

        mLocationDuration.setText(tour.getDuration());

        float distance = 0;
        TourPoint startPoint = tourPointsList.get(0);
        for (int i=1; i <= tourPointIndex; i++) {
            TourPoint p = tourPointsList.get(i);
            distance += p.distanceTo(startPoint);
            startPoint = p;
        }
        mLocationDistance.setText(String.format("%.2f km", distance/1000.0f));
    }

}
