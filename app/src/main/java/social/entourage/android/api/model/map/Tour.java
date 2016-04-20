package social.entourage.android.api.model.map;

import android.location.Address;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import social.entourage.android.Constants;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.TourType;

@SuppressWarnings("unused")
public class Tour implements Serializable {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String KEY_TOUR = "social.entourage.android.KEY_TOUR";
    public static final String KEY_TOUR_ID = "social.entourage.android.KEY_TOUR_ID";
    public static final String KEY_TOURS = "social.entourage.android.KEY_TOURS";
    public static final String TOUR_CLOSED = "closed";
    public static final String TOUR_ON_GOING = "ongoing";
    public static final String TOUR_FREEZED = "freezed";
    private static final String TOUR_FEET = "feet";
    private static final String TOUR_CAR = "car";
    public static final String JOIN_STATUS_NOT_REQUESTED = "not_requested";
    public static final String JOIN_STATUS_PENDING = "pending";
    public static final String JOIN_STATUS_ACCEPTED = "accepted";
    public static final String JOIN_STATUS_REJECTED = "rejected";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Expose(serialize = false, deserialize = true)
    private long id;

    @SerializedName("user_id")
    @Expose(serialize = false, deserialize = true)
    private int userId;

    @SerializedName("vehicle_type")
    private String tourVehicleType = TOUR_FEET;

    @SerializedName("tour_type")
    private String tourType = TourType.BARE_HANDS.getName();

    @SerializedName("status")
    private String tourStatus = TOUR_ON_GOING;

    @Expose(serialize = false, deserialize = false)
    private Date date;

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

    @Expose(serialize = false, deserialize = false)
    private transient Address startAddress;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("number_of_people")
    private int numberOfPeople;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("author")
    private TourAuthor author;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("join_status")
    private String joinStatus;

    @Expose(serialize = false, deserialize = false)
    private int badgeCount = 0;

    //CardInfo cache support

    @Expose(serialize = false, deserialize = false)
    transient List<TimestampedObject> cachedCardInfoList;

    @Expose(serialize = false, deserialize = false)
    transient List<TimestampedObject> addedCardInfoList;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public Tour() {
        init();
    }

    public Tour(String tourVehicleType, String tourType) {
        this.tourVehicleType = tourVehicleType;
        this.tourType = tourType;
        this.startTime = new Date();
        init();
    }

    private void init() {
        this.tourPoints = new ArrayList<>();
        this.encounters = new ArrayList<>();
        this.cachedCardInfoList = new ArrayList<>();
        this.addedCardInfoList = new ArrayList<>();
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public long getId() {
        return id;
    }

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
        return tourStatus;
    }

    public Date getDate() {
        return date;
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

    public Address getStartAddress() {
        return startAddress;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public TourAuthor getAuthor() {
        return author;
    }

    public String getJoinStatus() {
        return joinStatus;
    }

    public List<TimestampedObject> getCachedCardInfoList() {
        return cachedCardInfoList;
    }

    public List<TimestampedObject> getAddedCardInfoList() {
        return addedCardInfoList;
    }

    public void setId(long id) {
        this.id = id;
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
        this.tourStatus = tourStatus;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public void setStartAddress(final Address startAddress) {
        this.startAddress = startAddress;
    }

    public void setAuthor(TourAuthor author) {
        this.author = author;
    }

    public void setJoinStatus(String joinStatus) {
        this.joinStatus = joinStatus;
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
    public String toString() {
        return "tour : " + id + ", vehicule : " + tourVehicleType + ", type : " + tourType + ", status : " + tourStatus + ", points : " + tourPoints.size();
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

    public boolean isClosed() {
        return !tourStatus.equals(TOUR_ON_GOING);
    }

    public boolean isPrivate() {
        return joinStatus.equals(JOIN_STATUS_ACCEPTED);
    }

    public boolean isFreezed() {
        return tourStatus.equals(TOUR_FREEZED);
    }

    public void addCardInfo(TimestampedObject cardInfo) {
        if (cardInfo == null) return;
        if (cachedCardInfoList.contains(cardInfo)) {
            return;
        }
        cachedCardInfoList.add(cardInfo);
        addedCardInfoList.add(cardInfo);

        Collections.sort(cachedCardInfoList, new TimestampedObject.TimestampedObjectComparatorOldToNew());
    }

    public int addCardInfoList(List<TimestampedObject> cardInfoList) {
        if (cardInfoList == null) return 0;
        Iterator<TimestampedObject> iterator = cardInfoList.iterator();
        while (iterator.hasNext()) {
            TimestampedObject timestampedObject = iterator.next();
            if (cachedCardInfoList.contains(timestampedObject)) {
                continue;
            }
            cachedCardInfoList.add(timestampedObject);
            addedCardInfoList.add(timestampedObject);
        }
        if (addedCardInfoList.size() > 0) {
            Collections.sort(cachedCardInfoList, new TimestampedObject.TimestampedObjectComparatorOldToNew());
            Collections.sort(addedCardInfoList, new TimestampedObject.TimestampedObjectComparatorOldToNew());
        }
        return addedCardInfoList.size();
    }

    public void clearAddedCardInfoList() {
        addedCardInfoList.clear();
    }

    public static long getHoursDiffToNow(Date fromDate) {
        long currentHours = System.currentTimeMillis() / Constants.MILLIS_HOUR;
        long startHours = currentHours;
        if (fromDate != null) {
            startHours = fromDate.getTime() / Constants.MILLIS_HOUR;
        }
        return (currentHours - startHours);
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