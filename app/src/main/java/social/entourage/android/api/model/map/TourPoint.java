package social.entourage.android.api.model.map;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("unused")
public class TourPoint implements Serializable {

    private double latitude;

    private double longitude;

    @SerializedName("passing_time")
    private Date passingTime;

    public TourPoint(double latitude, double longitude, Date passingTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.passingTime = passingTime;
    }

    public TourPoint() {

    }

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
}
