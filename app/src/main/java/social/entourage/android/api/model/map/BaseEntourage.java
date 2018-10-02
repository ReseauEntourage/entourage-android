package social.entourage.android.api.model.map;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.map.entourage.category.EntourageCategory;
import social.entourage.android.map.entourage.category.EntourageCategoryManager;

/**
 * Created by Mihai Ionescu on 06/07/2018.
 */
public class BaseEntourage extends FeedItem implements Serializable {

    // ----------------------------------
    // Constants
    // ----------------------------------

    private static final long serialVersionUID = -4705290634210244790L;

    private final static String HASH_STRING_HEAD = "Entourage-";

    public static final String TYPE_CONTRIBUTION = "contribution";
    public static final String TYPE_DEMAND = "ask_for_help";
    public static final String TYPE_PRIVATE_CIRCLE = "private_circle";
    public static final String TYPE_NEIGHBORHOOD = "neighborhood";
    public static final String TYPE_CONVERSATION = "conversation";
    public static final String TYPE_OUTING = "outing";

    public static final String NEWSFEED_TYPE = "Entourage";

    public static final float HEATMAP_SIZE = 500; //meters
    private static int MARKER_SIZE = 0;

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @SerializedName("created_at")
    private Date createdTime;

    @SerializedName("group_type")
    String groupType;

    @SerializedName("entourage_type")
    private String entourageType;

    @SerializedName("display_category")
    private String category;

    private String title;

    private String description;

    private TourPoint location;

    private EntourageCloseOutcome outcome;

    private Metadata metadata;


    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public BaseEntourage() {
        super();
    }

    public BaseEntourage(String entourageType, String category, String title, String description, TourPoint location) {
        super();
        this.entourageType = entourageType;
        this.category = category;
        this.title = title;
        this.description = description;
        this.location = location;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(final Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public TourPoint getLocation() {
        return location;
    }

    public void setLocation(final TourPoint location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getEntourageType() {
        return entourageType;
    }

    public void setEntourageType(final String entourageType) {
        this.entourageType = entourageType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(final String groupType) {
        this.groupType = groupType;
    }

    public EntourageCloseOutcome getOutcome() {
        return outcome;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(final Metadata metadata) {
        this.metadata = metadata;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public boolean isFreezed() {
        return STATUS_CLOSED.equals(status);
    }

    public boolean isSame(BaseEntourage entourage) {
        if (entourage == null) return false;
        if (id != entourage.id) return false;
        if (!status.equals(entourage.status)) return false;
        if (!joinStatus.equals(entourage.joinStatus)) return false;
        if (numberOfPeople != entourage.numberOfPeople) return false;
        if (!entourageType.equals(entourage.entourageType)) return false;
        if (category != null && !category.equals(entourage.category)) return false;
        if (getAuthor() != null) {
            if (!getAuthor().isSame(entourage.getAuthor())) return false;
        }

        return true;
    }

    /**
     * Returns the distance from the entourage starting point to the current location
     * If the current location or the starting point is null, it returns zero
     * @return distance in kilometers
     */
    public int distanceToCurrentLocation() {
        EntourageLocation entourageLocation = EntourageLocation.getInstance();
        Location location = entourageLocation.getCurrentLocation();
        TourPoint startPoint = getStartPoint();
        if (location == null || startPoint == null) {
            return 0;
        }
        float distance = startPoint.distanceTo(new TourPoint(location.getLatitude(), location.getLongitude()));
        return (int)Math.floor(distance/1000.0f);
    }

    /**
     * Returns the distance from the entourage starting point to the given location
     * If the location or the starting point is null, it returns Integer.MAX_VALUE
     * @return distance in meters
     */
    public int distanceToLocation(LatLng location) {
        TourPoint startPoint = getStartPoint();
        if (location == null || startPoint == null) {
            return Integer.MAX_VALUE;
        }
        float distance = startPoint.distanceTo(new TourPoint(location.latitude, location.longitude));
        return (int)Math.floor(distance);
    }

    // ----------------------------------
    // FeedItem overrides
    // ----------------------------------

    @Override
    public String getFeedType() {
        return entourageType;
    }

    @Override
    public String getFeedTypeLong(Context context) {
        if (TYPE_NEIGHBORHOOD.equalsIgnoreCase(groupType)) {
            return context.getString(R.string.entourage_type_neighborhood);
        }
        if (TYPE_OUTING.equalsIgnoreCase(groupType)) {
            return context.getString(R.string.entourage_type_outing);
        }
        if (entourageType != null) {
            if (TYPE_DEMAND.equals(entourageType)) {
                return context.getString(R.string.entourage_type_format, context.getString(R.string.entourage_type_demand));
            }
            else if (TYPE_CONTRIBUTION.equals(entourageType)) {
                return context.getString(R.string.entourage_type_format, context.getString(R.string.entourage_type_contribution));
            }
        }
        return "";
    }

    @Override
    public int getFeedTypeColor() {
        if (TYPE_NEIGHBORHOOD.equalsIgnoreCase(groupType)) {
            return R.color.action_type_neighborhood;
        }
        if (TYPE_OUTING.equalsIgnoreCase(groupType)) {
            return R.color.action_type_outing;
        }
        EntourageCategory entourageCategory = EntourageCategoryManager.getInstance().findCategory(this);
        if (entourageCategory != null) {
            return entourageCategory.getTypeColorRes();
        }
        return super.getFeedTypeColor();
    }

    @Override
    public Date getStartTime() {
        return createdTime;
    }

    @Override
    public Date getEndTime() {
        return updatedTime;
    }

    public void setEndTime(Date endTime) {}

    @Override
    public TourPoint getStartPoint() {
        return location;
    }

    @Override
    public TourPoint getEndPoint() {
        return null;
    }

    @Override
    public Drawable getIconDrawable(Context context) {
        if (TYPE_NEIGHBORHOOD.equalsIgnoreCase(groupType)) {
            return AppCompatResources.getDrawable(context, R.drawable.ic_neighborhood);
        }
        if (TYPE_CONVERSATION.equalsIgnoreCase(groupType)) {
            return null;
        }
        if (TYPE_OUTING.equalsIgnoreCase(groupType)) {
            return AppCompatResources.getDrawable(context, R.drawable.ic_action_outing);
        }
        EntourageCategory entourageCategory = EntourageCategoryManager.getInstance().findCategory(this);
        if (entourageCategory != null) {
            Drawable categoryIcon = AppCompatResources.getDrawable(context, entourageCategory.getIconRes()).mutate();
            categoryIcon.clearColorFilter();
            categoryIcon.setColorFilter(ContextCompat.getColor(context, entourageCategory.getTypeColorRes()), PorterDuff.Mode.SRC_IN);
            return categoryIcon;
        }
        return super.getIconDrawable(context);
    }

    @Override
    public String getIconURL() {
        if (TYPE_CONVERSATION.equalsIgnoreCase(groupType)) {
            TourAuthor author = getAuthor();
            if (author != null) {
                return author.getAvatarURLAsString();
            }
        }
        return super.getIconURL();
    }

    @Override
    public boolean showHeatmapAsOverlay() {
        if (TYPE_NEIGHBORHOOD.equalsIgnoreCase(groupType) || TYPE_OUTING.equalsIgnoreCase(groupType)) {
            return false;
        }
        return super.showHeatmapAsOverlay();
    }

    @Override
    public int getHeatmapResourceId() {
        if (TYPE_NEIGHBORHOOD.equalsIgnoreCase(groupType)) {
            return R.drawable.ic_neighborhood_marker;
        }
        if (TYPE_OUTING.equalsIgnoreCase(groupType)) {
            return R.drawable.ic_action_outing_marker;
        }
        return super.getHeatmapResourceId();
    }

    @Override
    public boolean canBeClosed() {
        if (TYPE_NEIGHBORHOOD.equalsIgnoreCase(groupType)) {
            return false;
        }
        return super.canBeClosed();
    }

    @Override
    public boolean showAuthor() {
        if (TYPE_NEIGHBORHOOD.equalsIgnoreCase(groupType)) {
            return false;
        }
        return super.showAuthor();
    }

    @Override
    public @StringRes int getJoinRequestTitle() {
        return R.string.tour_info_request_join_title_entourage;
    }

    @Override
    public @StringRes int getJoinRequestButton() {
        return R.string.tour_info_request_join_button_entourage;
    }

    @Override
    public int getQuitDialogTitle() {
        return R.string.entourage_info_quit_entourage_title;
    }

    @Override
    public int getQuitDialogMessage() {
        return R.string.entourage_info_quit_entourage_description;
    }

    @Override
    public @StringRes int getFreezedCTAText() {
        if (TYPE_OUTING.equalsIgnoreCase(groupType) || outcome == null || outcome.success == false) return super.getFreezedCTAText();
        return R.string.tour_cell_button_freezed_success;
    }

    @Override
    public @ColorRes int getFreezedCTAColor() {
        if (TYPE_OUTING.equalsIgnoreCase(groupType) || outcome == null || outcome.success == false) return super.getFreezedCTAColor();
        return R.color.accent;
    }

    public static int getMarkerSize(Context context) {
        if (MARKER_SIZE == 0) {
            MARKER_SIZE = context.getResources().getDimensionPixelOffset(R.dimen.entourage_map_marker);
        }
        return MARKER_SIZE;
    }

    // ----------------------------------
    // TimestampedObject overrides
    // ----------------------------------

    @Override
    public Date getTimestamp() {
        return createdTime;
    }

    @Override
    public String hashString() {
        return HASH_STRING_HEAD + id;
    }

    @Override
    public boolean equals(final Object o) {
        return !(o == null || o.getClass() != this.getClass()) && this.id == ((Entourage) o).id;
    }

    @Override
    public int getType() {
        return ENTOURAGE_CARD;
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public static class EntourageJoinInfo {

        private Integer distance;

        public EntourageJoinInfo(int distance) {
            this.distance = distance;
        }

        public Integer getDistance() {
            return distance;
        }

        public void setDistance(final Integer distance) {
            this.distance = distance;
        }

    }

    public static class Metadata implements Serializable {

        @SerializedName("starts_at")
        private Date startDate;

        @SerializedName("display_address")
        private String displayAddress;

        @SerializedName("place_name")
        private String placeName;

        @SerializedName("street_address")
        private String streetAddress;

        @SerializedName("google_place_id")
        private String googlePlaceId;

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(final Date startDate) {
            this.startDate = startDate;
        }

        public String getStartDateAsString(Context context) {
            if (startDate == null) return "";
            DateFormat df = new SimpleDateFormat(context.getString(R.string.entourage_metadata_startAt_format), Locale.getDefault());
            return df.format(startDate);
        }

        public String getStartTimeAsString(Context context) {
            if (startDate == null) return "";
            //round the minutes to multiple of 15
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.setTime(startDate);
            int minutes = calendar.get(Calendar.MINUTE);
            minutes = (minutes / 15) * 15;
            calendar.set(Calendar.MINUTE, minutes);
            //format it
            DateFormat df = new SimpleDateFormat(context.getString(R.string.entourage_metadata_startAt_time_format), Locale.getDefault());
            return df.format(calendar.getTime());
        }

        public String getDisplayAddress() {
            return displayAddress;
        }

        public void setDisplayAddress(final String displayAddress) {
            this.displayAddress = displayAddress;
        }

        public String getPlaceName() {
            return placeName;
        }

        public void setPlaceName(final String placeName) {
            this.placeName = placeName;
        }

        public String getStreetAddress() {
            return streetAddress;
        }

        public void setStreetAddress(final String streetAddress) {
            this.streetAddress = streetAddress;
        }

        public String getGooglePlaceId() {
            return googlePlaceId;
        }

        public void setGooglePlaceId(final String googlePlaceId) {
            this.googlePlaceId = googlePlaceId;
        }
    }

    // ----------------------------------
    // WRAPPERS
    // ----------------------------------

    public static class EntourageWrapper {

        private Entourage entourage;

        public Entourage getEntourage() {
            return entourage;
        }

        public void setEntourage(final Entourage entourage) {
            this.entourage = entourage;
        }

    }

    public static class EntourageCloseWrapper {

        private String status;

        private EntourageCloseOutcome outcome;

        public EntourageCloseWrapper(String status, boolean success) {
            this.status = status;
            outcome = new EntourageCloseOutcome(success);
        }

    }

    public static class EntourageCloseOutcome {

        private boolean success;

        public EntourageCloseOutcome(boolean success) {
            this.success = success;
        }

    }

}
