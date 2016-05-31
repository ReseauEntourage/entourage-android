package social.entourage.android.api.model.map;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

import social.entourage.android.R;

/**
 * Created by mihaiionescu on 28/04/16.
 */
public class Entourage extends FeedItem implements Serializable {

    // ----------------------------------
    // Constants
    // ----------------------------------

    private final static String HASH_STRING_HEAD = "Entourage-";

    public static final String TYPE_CONTRIBUTION = "contribution";
    public static final String TYPE_DEMAND = "ask_for_help";

    public static final String NEWSFEED_TYPE = "Entourage";

    public static final float HEATMAP_SIZE = 500; //meters

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @SerializedName("updated_at")
    private Date updatedTime;

    @SerializedName("created_at")
    private Date createdTime;

    @SerializedName("entourage_type")
    private String entourageType;

    private String title;

    private String description;

    private TourPoint location;


    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public Entourage() {
        super();
    }

    public Entourage(String entourageType, String title, String description, TourPoint location) {
        super();
        this.entourageType = entourageType;
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

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(final Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public boolean isFreezed() {
        return false;
    }

    public boolean isSame(Entourage entourage) {
        if (entourage == null) return false;
        if (id != entourage.id) return false;
        if (!status.equals(entourage.status)) return false;
        if (!joinStatus.equals(entourage.joinStatus)) return false;

        return true;
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
    public Date getStartTime() {
        return createdTime;
    }

    @Override
    public Date getEndTime() {
        return createdTime;
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
        if (o == null || o.getClass() != this.getClass()) return false;
        return this.id == ((Entourage)o).id;
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
}
