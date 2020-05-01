package social.entourage.android.api.model.tour;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.util.Date;

import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.LocationPoint;

/**
 * Created by mihaiionescu on 29/02/16.
 */
public class TourTimestamp extends TimestampedObject {

    private final static String HASH_STRING_HEAD = "TourTimestamp-";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private Date date;
    private Date timestamp;
    private int feedType;
    private String status;
    private LocationPoint locationPoint;
    private long duration; //millis
    private float distance; //meters
    private Bitmap snapshot;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public TourTimestamp() {
        date = new Date();
        status = "";
        duration = 0;
        distance = 0.0f;
    }

    public TourTimestamp(Date date, Date timestamp, int feedType, String status, LocationPoint locationPoint, long duration, float distance) {
        this.date = date;
        this.timestamp = timestamp;
        this.feedType = feedType;
        this.status = status;
        this.locationPoint = locationPoint;
        this.duration = duration;
        this.distance = distance;
    }

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(final float distance) {
        this.distance = distance;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(final long duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public LocationPoint getLocationPoint() {
        return locationPoint;
    }

    public void setLocationPoint(final LocationPoint locationPoint) {
        this.locationPoint = locationPoint;
    }

    public Bitmap getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(final Bitmap snapshot) {
        this.snapshot = snapshot;
    }

    public int getFeedType() {
        return feedType;
    }

    // ----------------------------------
    // TimestampedObject overrides
    // ----------------------------------

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @NonNull
    @Override
    public String hashString() {
        return HASH_STRING_HEAD + duration;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || o.getClass() != this.getClass()) return false;
        //return this.date.equals( ((TourTimestamp)o).date );
        return this.duration == ((TourTimestamp)o).duration;
    }

    @Override
    public int getType() {
        return TOUR_STATUS;
    }

    @Override
    public long getId() {
        return 0;
    }

}
