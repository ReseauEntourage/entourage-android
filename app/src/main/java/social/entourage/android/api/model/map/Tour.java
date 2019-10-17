package social.entourage.android.api.model.map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import social.entourage.android.Constants;
import social.entourage.android.R;
import social.entourage.android.api.model.TourType;

@SuppressWarnings("unused")
public class Tour extends FeedItem implements Serializable {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final long serialVersionUID = -5072027793523981962L;

    private final static String HASH_STRING_HEAD = "Tour-";

    public static final String TYPE_TOUR = "tour";

    public static final String KEY_TOUR = "social.entourage.android.KEY_TOUR";
    public static final String KEY_TOUR_ID = "social.entourage.android.KEY_TOUR_ID";
    public static final String KEY_TOURS = "social.entourage.android.KEY_TOURS";

    public static final String NEWSFEED_TYPE = "Tour";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @SerializedName("user_id")
    @Expose(serialize = false, deserialize = true)
    private int userId;

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

    public Tour(String tourType) {
        super();
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

    public String getTourType() {
        return tourType;
    }

    public String getTourStatus() {
        return getStatus();
    }

    @Override
    public Date getCreationTime() { return startTime; }

    @Override
    public Date getStartTime() { return startTime; }

    public Date getEndTime() {
        return endTime;
    }

    public String getDisplayAddress() { return null; };

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

    @NonNull
    @Override
    public String toString() {
        return "tour : " + id + ", type : " + tourType + ", status : " + status + ", points : " + tourPoints.size();
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

    public void updateEncounter(Encounter updatedEncounter) {
        for (Encounter encounter : this.encounters) {
            if (encounter.getId() == updatedEncounter.getId()) {
                this.encounters.remove(encounter);
                break;
            }
        }
        this.encounters.add(updatedEncounter);
    }

    public static long getHoursDiffToNow(Date fromDate) {
        long currentHours = System.currentTimeMillis() / Constants.MILLIS_HOUR;
        long startHours = currentHours;
        if (fromDate != null) {
            startHours = fromDate.getTime() / Constants.MILLIS_HOUR;
        }
        return (currentHours - startHours);
    }

    public static String getStringDiffToNow(Date fromDate) {
        long hours = Tour.getHoursDiffToNow(fromDate);
        if (hours > 24) {
            return "" + (hours / 24) + "j";
        }
        return "" + hours + "h";
    }

    public boolean isSame(Tour tour) {
        if (tour == null) return false;
        if (id != tour.id) return false;
        if (tourPoints.size() != tour.tourPoints.size()) return false;
        if (!status.equals(tour.status)) return false;
        if (numberOfPeople != tour.numberOfPeople) return false;
        if(numberOfUnreadMessages != tour.numberOfUnreadMessages) return false;
        if (getAuthor() != null) {
            if (!getAuthor().isSame(tour.getAuthor())) return false;
        }
        return joinStatus.equals(tour.joinStatus);
    }

    public @DrawableRes int getIconRes() {
        if (TourType.MEDICAL.getName().equals(tourType)) {
            return R.drawable.ic_tour_medical;
        }
        else if (TourType.ALIMENTARY.getName().equals(tourType)) {
            return R.drawable.ic_tour_distributive;
        }
        else if (TourType.BARE_HANDS.getName().equals(tourType)) {
            return R.drawable.ic_tour_social;
        }
        return 0;
    }

    public static @ColorRes int getTypeColorRes(String type) {
        if (TourType.MEDICAL.getName().equals(type)) {
            return R.color.tour_type_medical;
        }
        else if (TourType.ALIMENTARY.getName().equals(type)) {
            return R.color.tour_type_distributive;
        }
        else if (TourType.BARE_HANDS.getName().equals(type)) {
            return R.color.tour_type_social;
        }
        return R.color.accent;
    }

    public @ColorRes int getTypeColorRes() {
        return Tour.getTypeColorRes(tourType);
    }

    @Override
    public Drawable getIconDrawable(final Context context) {
        @DrawableRes int iconRes = getIconRes();
        if (iconRes != 0) {
            return ContextCompat.getDrawable(context, iconRes);
        }
        return super.getIconDrawable(context);
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
        return !(o == null || o.getClass() != this.getClass()) && this.id == ((Tour) o).id;
    }

    @Override
    public int getType() {
        return TOUR_CARD;
    }

    // ----------------------------------
    // FeedItem overrides
    // ----------------------------------

    public String getGroupType() {
        return TYPE_TOUR;
    }

    public String getFeedType() {
        return tourType;
    }

    @Override
    public String getFeedTypeLong(Context context) {
        if (tourType != null) {
            if (tourType.equals(TourType.MEDICAL.getName())) {
                return context.getString(R.string.tour_info_text_type_title, context.getString(R.string.tour_type_medical).toLowerCase());
            } else if (tourType.equals(TourType.ALIMENTARY.getName())) {
                return context.getString(R.string.tour_info_text_type_title, context.getString(R.string.tour_type_alimentary).toLowerCase());
            } else if (tourType.equals(TourType.BARE_HANDS.getName())) {
                return context.getString(R.string.tour_info_text_type_title, context.getString(R.string.tour_type_bare_hands).toLowerCase());
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

        private static final long serialVersionUID = -9137864560567548841L;

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