package social.entourage.android.api.model.map;

import android.location.Address;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import social.entourage.android.api.model.TimestampedObject;

/**
 * Created by mihaiionescu on 18/05/16.
 */
public abstract class BaseEntourage extends TimestampedObject implements Serializable {

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @Expose(serialize = false, deserialize = true)
    protected long id;

    protected String status;

    protected TourAuthor author;

    @Expose(serialize = false, deserialize = false)
    protected transient Address startAddress;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("number_of_people")
    protected int numberOfPeople;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("join_status")
    protected String joinStatus;

    @Expose(serialize = false, deserialize = false)
    protected int badgeCount = 0;

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------


    public TourAuthor getAuthor() {
        return author;
    }

    public void setAuthor(final TourAuthor author) {
        this.author = author;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(final int badgeCount) {
        this.badgeCount = badgeCount;
    }

    public void increaseBadgeCount() {
        badgeCount++;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getJoinStatus() {
        return joinStatus;
    }

    public void setJoinStatus(final String joinStatus) {
        this.joinStatus = joinStatus;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(final int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public Address getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(final Address startAddress) {
        this.startAddress = startAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    // ----------------------------------
    // ABSTRACT METHODS
    // ----------------------------------

    public abstract String getTitle();

    public abstract String getDescription();

}
