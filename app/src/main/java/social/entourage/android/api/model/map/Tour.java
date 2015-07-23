package social.entourage.android.api.model.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import social.entourage.android.api.model.TourType;

@SuppressWarnings("unused")
public class Tour implements Serializable {

    private long id;

    @SerializedName("tour_type")
    private String tourType = TourType.SOCIAL.getName();

    private transient Date date;

    private String duration;

    private float distance;

    private transient List<LatLng> coordinates;

    private final transient HashMap<Date, String> steps;

    private final transient List<Encounter> encounters;

    public Tour() {
        this.coordinates = new ArrayList<>();
        this.steps = new HashMap<>();
        this.encounters = new ArrayList<>();
    }

    // ----------------------------------
    // GETTERS
    // ----------------------------------

    public long getId() {
        return id;
    }

    public String getTourType() {
        return tourType;
    }

    public Date getDate() {
        return date;
    }

    public String getDuration() {
        return duration;
    }

    public float getDistance() {
        return distance;
    }

    public List<LatLng> getCoordinates() {
        return coordinates;
    }

    public HashMap<Date, String> getSteps() {
        return steps;
    }

    public List<Encounter> getEncounters() {
        return encounters;
    }

    // ----------------------------------
    // SETTERS
    // ----------------------------------

    public void setId(long id) {
        this.id = id;
    }

    public void setTourType(String tourType) {
        this.tourType = tourType;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setCoordinates(List<LatLng> coordinates) {
        this.coordinates = coordinates;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void updateDistance(float distance) {
        this.distance += distance;
    }

    public void addCoordinate(LatLng location) {
        this.coordinates.add(location);
    }

    public void addStep(Date time, String step) {
        this.steps.put(time, step);
    }

    public void addEncounter(Encounter encounter) {
        this.encounters.add(encounter);
    }

}
