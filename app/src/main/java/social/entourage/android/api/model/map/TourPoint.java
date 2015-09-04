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

    @SerializedName("passing_time")
    private Date passingTime;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public TourPoint(double latitude, double longitude, Date passingTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.passingTime = passingTime;
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

    public Date getPassingTime() {
        return passingTime;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setPassingTime(Date passingTime) {
        this.passingTime = passingTime;
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
