package social.entourage.android.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import social.entourage.android.api.model.map.TourPoint;

/**
 * Created by NTE on 03/08/15.
 */
public class TourPointWrapper {

    @SerializedName("tour_points")
    private List<TourPoint> tourPoints;

    public List<TourPoint> getTourPoints() {
        return tourPoints;
    }

    public void setTourPoints(List<TourPoint> tourPoint) {
        this.tourPoints = tourPoint;
    }
}
