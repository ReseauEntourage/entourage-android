package social.entourage.android.api.model.map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by mihaiionescu on 28/04/16.
 */
public class Entourage implements Serializable {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TYPE_CONTRIBUTION = "contribution";
    public static final String TYPE_DEMAND = "ask_for_help";

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @Expose(serialize = false, deserialize = true)
    private long id;

    @SerializedName("updated_at")
    private Date updatedTime;

    @SerializedName("created_at")
    private Date createdTime;

    @SerializedName("entourage_type")
    private String type;

    private String title;

    private String description;

    private TourPoint location;

    private String status;

    private TourAuthor author;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public Entourage() {

    }

    public Entourage(String type, String title, String description, TourPoint location) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.location = location;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------


    public TourAuthor getAuthor() {
        return author;
    }

    public void setAuthor(final TourAuthor author) {
        this.author = author;
    }

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

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public TourPoint getLocation() {
        return location;
    }

    public void setLocation(final TourPoint location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(final Date updatedTime) {
        this.updatedTime = updatedTime;
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
