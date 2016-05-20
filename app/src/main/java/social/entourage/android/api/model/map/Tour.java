package social.entourage.android.api.model.map;

import android.content.Context;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import social.entourage.android.Constants;
import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.TourType;

@SuppressWarnings("unused")
public class Tour extends FeedItem implements Serializable {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final static String HASH_STRING_HEAD = "Tour-";

    public static final String KEY_TOUR = "social.entourage.android.KEY_TOUR";
    public static final String KEY_TOUR_ID = "social.entourage.android.KEY_TOUR_ID";
    public static final String KEY_TOURS = "social.entourage.android.KEY_TOURS";

    private static final String TOUR_FEET = "feet";
    private static final String TOUR_CAR = "car";

    public static final String NEWSFEED_TYPE = "Tour";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @SerializedName("user_id")
    @Expose(serialize = false, deserialize = true)
    private int userId;

    @SerializedName("vehicle_type")
    private String tourVehicleType = TOUR_FEET;

    @SerializedName("tour_type")
    private String tourType = TourType.BARE_HANDS.getName();

    @SerializedName("start_time")
    @Expose(serialize = true, deserialize = true)
    private Date startTime;

    @SerializedName("end_time")
    @Expose(serialize = true, deserialize = true)
    private Date endTime;

    @Expose(serialize = false, deserialize = false)
    private String duration;

    @Expose(serialize = true, deserialize = true)
    private float distance;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("tour_points")
    private List<TourPoint> tourPoints;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("organization_name")
    private String organizationName;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("organization_description")
    private String organizationDescription;

    @Expose(serialize = false)
    private List<Encounter> encounters;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public Tour() {
        super();
        init();
    }

    public Tour(String tourVehicleType, String tourType) {
        super();
        this.tourVehicleType = tourVehicleType;
        this.tourType = tourType;
        this.startTime = new Date();
        init();
    }

    private void init() {
        this.tourPoints = new ArrayList<>();
        this.encounters = new ArrayList<>();
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public int getUserId() {
        return userId;
    }

    public String getTourVehicleType() {
        return tourVehicleType;
    }

    public String getTourType() {
        return tourType;
    }

    public String getTourStatus() {
        return getStatus();
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
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

    public String getOrganizationName() {
        return organizationName;
    }

    public String getOrganizationDescription() {
        return organizationDescription;
    }

    public List<Encounter> getEncounters() {
        return encounters;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setTourVehicleType(String tourVehicleType) {
        this.tourVehicleType = tourVehicleType;
    }

    public void setTourType(String tourType) {
        this.tourType = tourType;
    }

    public void setTourStatus(String tourStatus) {
        this.status = tourStatus;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
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

    @Override
    public String toString() {
        return "tour : " + id + ", vehicule : " + tourVehicleType + ", type : " + tourType + ", status : " + status + ", points : " + tourPoints.size();
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void updateDistance(float distance) {
        this.distance += distance;
    }

    public void addCoordinate(TourPoint location) {
        this.tourPoints.add(location);
    }

    public void addEncounter(Encounter encounter) {
        this.encounters.add(encounter);
    }

    public static long getHoursDiffToNow(Date fromDate) {
        long currentHours = System.currentTimeMillis() / Constants.MILLIS_HOUR;
        long startHours = currentHours;
        if (fromDate != null) {
            startHours = fromDate.getTime() / Constants.MILLIS_HOUR;
        }
        return (currentHours - startHours);
    }

    public boolean isSame(Tour tour) {
        if (tour == null) return false;
        if (id != tour.id) return false;
        if (tourPoints.size() != tour.tourPoints.size()) return false;
        if (!status.equals(tour.status)) return false;
        if (!joinStatus.equals(tour.joinStatus)) return false;

        return true;
    }

    // ----------------------------------
    // TimestampedObject overrides
    // ----------------------------------

    @Override
    public Date getTimestamp() {
        return startTime;
    }

    @Override
    public String hashString() {
        return HASH_STRING_HEAD + id;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || o.getClass() != this.getClass()) return false;
        return this.id == ((Tour)o).id;
    }

    @Override
    public int getType() {
        return TOUR_CARD;
    }

    // ----------------------------------
    // FeedItem overrides
    // ----------------------------------

    public String getFeedType() {
        return tourType;
    }

    @Override
    public String getFeedTypeLong(Context context) {
        if (tourType != null) {
            if (tourType.equals(TourType.MEDICAL.getName())) {
                return context.getString(R.string.tour_info_text_type_title, context.getString(R.string.tour_type_medical));
            } else if (tourType.equals(TourType.ALIMENTARY.getName())) {
                return context.getString(R.string.tour_info_text_type_title, context.getString(R.string.tour_type_alimentary));
            } else if (tourType.equals(TourType.BARE_HANDS.getName())) {
                return context.getString(R.string.tour_info_text_type_title, context.getString(R.string.tour_type_bare_hands));
            }
        }
        return null;
    }

    @Override
    public String getTitle() {
        return organizationName;
    }

    @Override
    public String getDescription() {
        return "";
    }

    public TourPoint getStartPoint() {
        if (tourPoints == null || tourPoints.size() == 0) return null;
        return tourPoints.get(0);
    }

    public TourPoint getEndPoint() {
        if (tourPoints == null || tourPoints.size() < 1) return null;
        return tourPoints.get(tourPoints.size()-1);
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

    public static class TourComparatorNewToOld implements Comparator<Tour> {
        @Override
        public int compare(Tour tour1, Tour tour2) {
            if (tour1.getStartTime() != null && tour2.getStartTime() != null) {
                Date date1 = tour1.getStartTime();
                Date date2 = tour2.getStartTime();
                return date2.compareTo(date1);
            } else {
                return 0;
            }
        }
    }

    public static class TourComparatorOldToNew implements Comparator<Tour> {
        @Override
        public int compare(Tour tour1, Tour tour2) {
            if (tour1.getStartTime() != null && tour2.getStartTime() != null) {
                Date date1 = tour1.getStartTime();
                Date date2 = tour2.getStartTime();
                return date1.compareTo(date2);
            } else {
                return 0;
            }
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