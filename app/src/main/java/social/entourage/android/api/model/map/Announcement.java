package social.entourage.android.api.model.map;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import social.entourage.android.api.model.TimestampedObject;

/**
 * Announcement received from the server
 * Created by Mihai Ionescu on 02/11/2017.
 */

public class Announcement extends TimestampedObject {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String NEWSFEED_TYPE = "Announcement";

    private final static String HASH_STRING_HEAD = "Announcement-";

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private long id;

    private String title;

    private String body;

    private String action;

    private String url;

    @SerializedName("icon_url")
    private String iconUrl;

    private TourAuthor author;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getAction() {
        return action;
    }

    public String getUrl() {
        return url;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public TourAuthor getAuthor() {
        return author;
    }


    // ----------------------------------
    // TimestampedObject overrides
    // ----------------------------------

    @Override
    public Date getTimestamp() {
        return null;
    }

    @Override
    public String hashString() {
        return HASH_STRING_HEAD + id;
    }

    @Override
    public int getType() {
        return ANNOUNCEMENT_CARD;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Announcement)) return false;
        return this.id == ((Announcement)obj).id;
    }

    // ----------------------------------
    // WRAPPERS
    // ----------------------------------

}
