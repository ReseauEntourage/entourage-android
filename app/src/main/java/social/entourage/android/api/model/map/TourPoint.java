package social.entourage.android.api.model.map;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
public class TourPoint implements Serializable {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private double latitude;

    private double longitude;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public TourPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng getLocation() {
        return new LatLng(latitude, longitude);
    }

    // ----------------------------------
    // WRAPPER
    // ----------------------------------

    public static class TourPointWrapper {

        @SerializedName("tour_points")
        private List<TourPoint> tourPoints;

        private float distance;

        public List<TourPoint> getTourPoints() {
            return tourPoints;
        }

        public void setTourPoints(List<TourPoint> tourPoint) {
            this.tourPoints = tourPoint;
        }

        public void setDistance(float distance) {
            this.distance = distance;
        }
    }
}
