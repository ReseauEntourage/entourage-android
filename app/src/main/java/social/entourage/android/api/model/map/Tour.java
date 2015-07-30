package social.entourage.android.api.model.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import social.entourage.android.api.model.TourType;

@SuppressWarnings("unused")
public class Tour implements Serializable {

    private static final String TOUR_ON_GOING = "ongoing";
    private static final String TOUR_CLOSED = "closed";
    private static final String TOUR_FEET = "feet";
    private static final String TOUR_CAR = "car";

    @Expose(serialize = false)
    private long id;

    @SerializedName("vehicule_type")
    private String tourVehiculeType = TOUR_FEET;

    @SerializedName("tour_type")
    private String tourType = TourType.SOCIAL.getName();

    @SerializedName("status")
    private String tourStatus = TOUR_ON_GOING;

    @Expose(serialize = false)
    private Date date;

    @Expose(serialize = false)
    private String duration;

    @Expose(serialize = false)
    private float distance;

    @Expose(serialize = false)
    private List<LatLng> coordinates;

    @Expose(serialize = false)
    private final HashMap<Date, String> steps;

    @Expose(serialize = false)
    private final List<Encounter> encounters;

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

    public String getTourVehiculeType() {
        return tourVehiculeType;
    }

    public String getTourType() {
        return tourType;
    }

    public String getTourStatus() {
        return tourStatus;
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

    public void setTourVehiculeType(String tourVehiculeType) {
        this.tourVehiculeType = tourVehiculeType;
    }

    public void setTourType(String tourType) {
        this.tourType = tourType;
    }

    public void setTourStatus(String tourStatus) {
        this.tourStatus = tourStatus;
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

    public void closeTour() {
        this.tourStatus = TOUR_CLOSED;
    }

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
