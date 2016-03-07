package social.entourage.android.api.model.map;

import java.util.Date;

import social.entourage.android.api.model.TimestampedObject;

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
    private String status;
    private long duration; //millis
    private float distance; //meters

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public TourTimestamp() {
        date = new Date();
        status = "";
        duration = 0;
        distance = 0.0f;
    }

    public TourTimestamp(Date date, Date timestamp, String status, long duration, float distance) {
        this.date = date;
        this.timestamp = timestamp;
        this.status = status;
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


    // ----------------------------------
    // TimestampedObject ovverides
    // ----------------------------------

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

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
}
