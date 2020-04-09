package social.entourage.android.api.model.map;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import social.entourage.android.location.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.entourage.category.EntourageCategory;
import social.entourage.android.entourage.category.EntourageCategoryManager;

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
    public static final String TYPE_ACTION = "action";

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

    @SerializedName("recipient_consent_obtained")
    private boolean recipientConsentObtained = true;

    @SerializedName("public")
    private boolean isJoinRequestPublic;


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

    @Override
    public Date getCreationTime() { return createdTime; }

    public void setCreationTime(final Date creationTime) {
        this.createdTime = creationTime;
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

    public void setOutcome(final EntourageCloseOutcome outcome) {
        this.outcome = outcome;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(final Metadata metadata) {
        this.metadata = metadata;
    }

    public boolean isRecipientConsentObtained() {
        return recipientConsentObtained;
    }

    public void setRecipientConsentObtained(final boolean recipientConsentObtained) {
        this.recipientConsentObtained = recipientConsentObtained;
    }

    public boolean isJoinRequestPublic() {
        return isJoinRequestPublic;
    }

    public void setJoinRequestPublic(final boolean joinRequestPublic) {
        isJoinRequestPublic = joinRequestPublic;
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
        if(numberOfUnreadMessages != entourage.numberOfUnreadMessages) return false;
        if (!entourageType.equals(entourage.entourageType)) return false;
        if (category != null && !category.equals(entourage.category)) return false;
        if (getAuthor() != null) {
            return getAuthor().isSame(entourage.getAuthor());
        }
        if (isJoinRequestPublic != entourage.isJoinRequestPublic) return false;

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
        if (TYPE_PRIVATE_CIRCLE.equalsIgnoreCase(groupType)) {
            return context.getString(R.string.entourage_type_private_circle);
        }
        if (TYPE_CONVERSATION.equalsIgnoreCase(groupType)) {
            return "";
        }
        if (TYPE_OUTING.equalsIgnoreCase(groupType)) {
            //check si les dates de début et fin sont le même jour ou pas
            Calendar startCalendar = Calendar.getInstance() ;
            startCalendar.setTime(this.metadata.getStartDate());

            Calendar endCalendar = Calendar.getInstance() ;
            endCalendar.setTime(this.metadata.getEndDate());

            if (startCalendar.get(Calendar.DAY_OF_YEAR) == endCalendar.get(Calendar.DAY_OF_YEAR)) {
                return String.format("%1$s %2$s", context.getString(R.string.entourage_type_outing), this.metadata.getStartDateAsString(context));
            }
            else {
                return String.format("%1$s %2$s", context.getString(R.string.entourage_type_outing),this.metadata.getStartEndDatesAsString(context));
            }
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
    public String getDisplayAddress() {
        return this.metadata.getDisplayAddress();
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
            return AppCompatResources.getDrawable(context, R.drawable.ic_event_accent_24dp);
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
            return R.drawable.ic_event_pin;
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
        if (TYPE_OUTING.equalsIgnoreCase(groupType)) return R.string.tour_info_request_join_title_outing;
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
        if (TYPE_OUTING.equalsIgnoreCase(groupType) || outcome == null || !outcome.success) return super.getFreezedCTAText();
        return R.string.tour_cell_button_freezed_success;
    }

    @Override
    public @ColorRes int getFreezedCTAColor() {
        if (TYPE_OUTING.equalsIgnoreCase(groupType) || outcome == null || !outcome.success) return super.getFreezedCTAColor();
        return R.color.accent;
    }

    @Override
    public int getClosingLoaderMessage() {
        if (TYPE_OUTING.equalsIgnoreCase(groupType)) return R.string.loader_title_outing_finish;
        return R.string.loader_title_action_finish;
    }

    @Override
    public int getClosedToastMessage() {
        if (TYPE_OUTING.equalsIgnoreCase(groupType)) return R.string.outing_info_text_close;
        return R.string.entourage_info_text_close;
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

        @SerializedName("ends_at")
        private Date endDate;

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
            DateFormat df = new SimpleDateFormat(context.getString(R.string.entourage_metadata_startAt_format), Locale.FRENCH);
            return df.format(startDate);
        }

        public String getStartDateFullAsString(Context context) {
            if (startDate == null) return "";
            DateFormat df = new SimpleDateFormat(context.getString(R.string.entourage_metadata_startAt_format_full), Locale.FRENCH);
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
            DateFormat df = new SimpleDateFormat(context.getString(R.string.entourage_metadata_startAt_time_format), Locale.FRENCH);
            return df.format(calendar.getTime());
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(final Date endDate) {
            this.endDate = endDate;
        }

        public String getEndDateFullAsString(Context context) {
            if (endDate == null) return "";
            DateFormat df = new SimpleDateFormat(context.getString(R.string.entourage_metadata_startAt_format_full), Locale.FRENCH);
            return df.format(endDate);
        }

        public String getEndTimeAsString(Context context) {
            if (endDate == null) return "";
            //round the minutes to multiple of 15
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.setTime(endDate);
            int minutes = calendar.get(Calendar.MINUTE);
            minutes = (minutes / 15) * 15;
            calendar.set(Calendar.MINUTE, minutes);
            //format it
            DateFormat df = new SimpleDateFormat(context.getString(R.string.entourage_metadata_startAt_time_format), Locale.FRENCH);
            return df.format(calendar.getTime());
        }

        public String getStartEndDatesAsString(Context context) {
            if (startDate == null || endDate == null) return "";
            DateFormat df = new SimpleDateFormat("dd/MM", Locale.FRENCH);
            String _startDate = df.format(startDate);
            String _endDate = df.format(endDate);

            return String.format(context.getString(R.string.entourage_metadata_date_startAt_endAt),_startDate,_endDate);
        }

        public String getStartEndTimesAsString(Context context) {
            if (startDate == null || endDate == null) return "";
            Calendar calendarStart = Calendar.getInstance(Locale.getDefault());
            Calendar calendarEnd = Calendar.getInstance(Locale.getDefault());
            calendarStart.setTime(startDate);
            calendarEnd.setTime(endDate);
            int minutesStart = calendarStart.get(Calendar.MINUTE);
            int minutesEnd = calendarEnd.get(Calendar.MINUTE);
            minutesStart = (minutesStart / 15) * 15;
            minutesEnd = (minutesEnd / 15) * 15;
            calendarStart.set(Calendar.MINUTE, minutesStart);
            calendarEnd.set(Calendar.MINUTE, minutesEnd);
            //format it
            DateFormat df = new SimpleDateFormat("HH'h'mm", Locale.FRENCH);
            String _timeStart = df.format(calendarStart.getTime());
            String _timeEnd = df.format(calendarEnd.getTime());
            return String.format(context.getResources().getString(R.string.entourage_metadata_time_startAt_endAt),_timeStart,_timeEnd);
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

    public static class EntourageCloseOutcome implements Serializable {

        private static final long serialVersionUID = 4175678577343446888L;
        
        private boolean success;

        public EntourageCloseOutcome(boolean success) {
            this.success = success;
        }

    }

}
