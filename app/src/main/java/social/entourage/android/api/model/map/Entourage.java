package social.entourage.android.api.model.map;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.map.entourage.category.EntourageCategory;
import social.entourage.android.map.entourage.category.EntourageCategoryManager;

/**
 * Created by mihaiionescu on 28/04/16.
 */
public class Entourage extends FeedItem implements Serializable {

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

    public static final String NEWSFEED_TYPE = "Entourage";

    public static final float HEATMAP_SIZE = 500; //meters

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


    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public Entourage() {
        super();
    }

    public Entourage(String entourageType, String category, String title, String description, TourPoint location) {
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

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public boolean isFreezed() {
        return STATUS_CLOSED.equals(status);
    }

    public boolean isSame(Entourage entourage) {
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
        if (TYPE_NEIGHBORHOOD.equalsIgnoreCase(groupType)) {
            return false;
        }
        return super.showHeatmapAsOverlay();
    }

    @Override
    public int getHeatmapResourceId() {
        if (TYPE_NEIGHBORHOOD.equalsIgnoreCase(groupType)) {
            return R.drawable.ic_neighborhood;
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
}
