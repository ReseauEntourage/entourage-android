package social.entourage.android.api.model.map;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by NTE on 10/07/15.
 */
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

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Date getPassingTime() {
        return passingTime;
    }

    public void setPassingTime(Date passingTime) {
        this.passingTime = passingTime;
    }
}
