package social.entourage.android.api.model.map;

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

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String KEY_TOUR = "social.entourage.android.KEY_TOUR";
    public static final String KEY_TOURS = "social.entourage.android.KEY_TOURS";
    public static final String TOUR_CLOSED = "closed";
    public static final String TOUR_ON_GOING = "ongoing";
    private static final String TOUR_FEET = "feet";
    private static final String TOUR_CAR = "car";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Expose(serialize = false)
    private long id;

    @SerializedName("vehicle_type")
    private String tourVehicleType = TOUR_FEET;

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

    @Expose(serialize = false, deserialize = true)
    @SerializedName("tour_points")
    private List<TourPoint> tourPoints;

    @Expose(serialize = false)
    private final HashMap<Date, String> steps;

    @Expose(serialize = false)
    private final List<Encounter> encounters;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public Tour() {
        this.tourPoints = new ArrayList<>();
        this.steps = new HashMap<>();
        this.encounters = new ArrayList<>();
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public long getId() {
        return id;
    }

    public String getTourVehicleType() {
        return tourVehicleType;
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

    public List<TourPoint> getTourPoints() {
        return tourPoints;
    }

    public HashMap<Date, String> getSteps() {
        return steps;
    }

    public List<Encounter> getEncounters() {
        return encounters;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTourVehicleType(String tourVehicleType) {
        this.tourVehicleType = tourVehicleType;
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

    public void setTourPoints(List<TourPoint> tourPoints) {
        this.tourPoints = tourPoints;
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

    public void addCoordinate(TourPoint location) {
        this.tourPoints.add(location);
    }

    public void addStep(Date time, String step) {
        this.steps.put(time, step);
    }

    public void addEncounter(Encounter encounter) {
        this.encounters.add(encounter);
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public static class Tours implements Serializable {

        private List<Tour> tours;

        public Tours(List<Tour> tours) {
            this.tours = tours;
        }

        public List<Tour> getTours() {
            return tours;
        }
    }

    // ----------------------------------
    // WRAPPERS
    // ----------------------------------

    public static class TourWrapper {

        private Tour tour;

        public Tour getTour() {
            return tour;
        }

        public void setTour(Tour tour) {
            this.tour = tour;
        }
    }

    public static class ToursWrapper {

        private List<Tour> tours;

        public List<Tour> getTours() {
            return tours;
        }

        public void setTours(List<Tour> tours) {
            this.tours = tours;
        }

    }
}
