package social.entourage.android.api.model.map;

import android.location.Address;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import social.entourage.android.api.model.TimestampedObject;

@SuppressWarnings("unused")
public class Encounter extends TimestampedObject implements Serializable {

    private final static String HASH_STRING_HEAD = "Encounter-";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private long id;

    private long tourId;

    @SerializedName("date")
    private Date creationDate;

    private double longitude;

    private double latitude;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("street_person_name")
    private String streetPersonName;

    private String message;

    private transient Address address;

    private boolean isMyEncounter = false;

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTourId() {
        return tourId;
    }

    public void setTourId(long tourId) {
        this.tourId = tourId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName == null ? "" : userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStreetPersonName() {
        return streetPersonName;
    }

    public void setStreetPersonName(String streetPersonName) {
        this.streetPersonName = streetPersonName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    public boolean isMyEncounter() {
        return isMyEncounter;
    }

    public void setIsMyEncounter(final boolean isMyEncounter) {
        this.isMyEncounter = isMyEncounter;
    }

    @Override
    public Date getTimestamp() {
        return creationDate;
    }

    @Override
    public String hashString() {
        return HASH_STRING_HEAD + id;
    }

    @Override
    public boolean equals(final Object o) {
        return !(o == null || o.getClass() != this.getClass()) && this.id == ((Encounter) o).id;
    }

    @Override
    public int getType() {
        return ENCOUNTER;
    }

    // ----------------------------------
    // WRAPPER
    // ----------------------------------

    public static class EncounterWrapper {

        private Encounter encounter;

        public Encounter getEncounter() {
            return encounter;
        }

        public void setEncounter(final Encounter encounter) {
            this.encounter = encounter;
        }
    }

    public static class EncountersWrapper {

        private List<Encounter> encounters;

        public List<Encounter> getEncounters() {
            return encounters;
        }

        public void setEncounters(final List<Encounter> encounters) {
            this.encounters = encounters;
        }
    }

}
