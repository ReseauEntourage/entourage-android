package social.entourage.android.api.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import social.entourage.android.location.EntourageLocation;

@SuppressWarnings("unused")
public class LocationPoint implements Serializable {
    
    private static final long serialVersionUID = -5620241951845660404L;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private double latitude;

    private double longitude;

    private float accuracy;

    @SerializedName("passing_time")
    private Date passingTime;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public LocationPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = 0.0f;
        passingTime = new Date();
    }

    public LocationPoint(double latitude, double longitude, float accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        passingTime = new Date();
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

    public float getAccuracy() {
        return accuracy;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAccuracy(final float accuracy) {
        this.accuracy = accuracy;
    }

    public LatLng getLocation() {
        return new LatLng(latitude, longitude);
    }

    // ----------------------------------
    // HELPERS
    // ----------------------------------

    public float distanceTo(LocationPoint otherPoint) {
        if (otherPoint == null) return 0;
        float[] result = {0};
        Location.distanceBetween(this.latitude, this.longitude, otherPoint.latitude, otherPoint.longitude, result);
        return result[0];
    }

    public String distanceToCurrentLocation(float maxDistanceToShow) {
        String distanceAsString = "";

        Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
        if (currentLocation != null) {
            float distance = distanceTo(new LocationPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
            if(distance<maxDistanceToShow) {
                if (distance < 1000.0f) {
                    distanceAsString = String.format("%.0f m", distance);
                } else {
                    distanceAsString = String.format("%.0f km", distance / 1000.0f);
                }
            }
        }

        return distanceAsString;
    }

    // ----------------------------------
    // WRAPPER
    // ----------------------------------

    public static class TourPointWrapper {

        @SerializedName("tour_points")
        private List<LocationPoint> tourPoints;

        private float distance;

        public List<LocationPoint> getTourPoints() {
            return tourPoints;
        }

        public void setTourPoints(List<LocationPoint> locationPoint) {
            this.tourPoints = locationPoint;
        }

        public void setDistance(float distance) {
            this.distance = distance;
        }
    }
}