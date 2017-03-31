package social.entourage.android.api.model.map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import social.entourage.android.api.model.Partner;
import social.entourage.android.api.model.TimestampedObject;

/**
 * Created by mihaiionescu on 24/02/16.
 */
public class TourUser extends TimestampedObject implements Serializable {

    private final static String HASH_STRING_HEAD = "TourUser-";

    @SerializedName("id")
    private int userId;

    @SerializedName("display_name")
    private String displayName;

    @Expose(serialize = false, deserialize = true)
    private String email;

    @Expose(serialize = false, deserialize = true)
    private String status;

    @SerializedName("requested_at")
    private Date requestDate;

    private String message;

    @SerializedName("avatar_url")
    private String avatarURLAsString;

    @SerializedName("partner")
    private Partner partner;

    private boolean isDisplayedAsMember = false;

    @Expose(serialize = false, deserialize = false)
    private FeedItem feedItem;

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName == null ? "" : displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(final Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(final int userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getAvatarURLAsString() {
        return avatarURLAsString;
    }

    public void setAvatarURLAsString(final String avatarURLAsString) {
        this.avatarURLAsString = avatarURLAsString;
    }

    public Partner getPartner() {
        return partner;
    }

    public boolean isDisplayedAsMember() {
        return isDisplayedAsMember;
    }

    public void setDisplayedAsMember(final boolean displayedAsMember) {
        isDisplayedAsMember = displayedAsMember;
    }

    public FeedItem getFeedItem() {
        return feedItem;
    }

    public void setFeedItem(final FeedItem feedItem) {
        this.feedItem = feedItem;
    }

    @Override
    public Date getTimestamp() {
        return requestDate;
    }

    @Override
    public String hashString() {
        return HASH_STRING_HEAD + userId;
    }

    @Override
    public int getType() {
        return isDisplayedAsMember ? FEED_MEMBER_CARD : TOUR_USER_JOIN;
    }

    @Override
    public long getId() {
        return userId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || o.getClass() != this.getClass()) return false;
        //return (this.userId == ((TourUser)o).userId) && (this.status.equals(((TourUser)o).status));
        return this.userId == ((TourUser)o).userId;
    }

    public TourUser clone() {
        TourUser clone = new TourUser();
        clone.userId = this.userId;
        clone.feedItem = this.feedItem;
        clone.displayName = this.displayName;
        clone.email = this.email;
        clone.status = this.status;
        clone.requestDate = this.requestDate;
        clone.message = this.message;
        clone.avatarURLAsString = this.avatarURLAsString;
        clone.partner = this.partner;
        clone.isDisplayedAsMember = this.isDisplayedAsMember;

        return clone;
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public static class TourUserWrapper {

        private TourUser user;

        public TourUser getUser() {
            return user;
        }

        public void setUser(TourUser user) {
            this.user = user;
        }

    }

    public static class TourUsersWrapper {

        private List<TourUser> users;

        public List<TourUser> getUsers() {
            return users;
        }

        public void setUsers(List<TourUser> users) {
            this.users = users;
        }

    }
}
