package social.entourage.android.api.model.feed;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import social.entourage.android.api.model.Partner;

public class FeedItemAuthor implements Serializable {

    private static final long serialVersionUID = 3412722274231780458L;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @SerializedName("id")
    private int userID;

    @SerializedName("display_name")
    private String userName;

    @SerializedName("avatar_url")
    private String avatarURLAsString;

    @SerializedName("partner")
    private Partner partner;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public FeedItemAuthor(final String avatarURLAsString, final int userID, final String userName, Partner partner) {
        this.avatarURLAsString = avatarURLAsString;
        this.userID = userID;
        this.userName = userName;
        this.partner = partner;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------


    public String getAvatarURLAsString() {
        return avatarURLAsString;
    }

    public void setAvatarURLAsString(final String avatarURLAsString) {
        this.avatarURLAsString = avatarURLAsString;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(final int userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName == null ? "" : userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(final Partner partner) {
        this.partner = partner;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public boolean isSame(FeedItemAuthor author) {
        if (author == null) return false;
        if (userID != author.userID) return false;
        if (avatarURLAsString != null) {
            if (!avatarURLAsString.equals(author.avatarURLAsString)) return false;
        } else {
            if (author.avatarURLAsString != null) return false;
        }
        if (partner != null) {
            return partner.isSame(author.partner);
        } else {
            return author.partner == null;
        }
    }

}
