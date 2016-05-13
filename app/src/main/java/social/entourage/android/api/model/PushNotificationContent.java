package social.entourage.android.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by mihaiionescu on 18/03/16.
 */
public class PushNotificationContent implements Serializable {

    public static final String TYPE_NEW_CHAT_MESSAGE = "NEW_CHAT_MESSAGE";
    public static final String TYPE_JOIN_REQUEST_ACCEPTED = "JOIN_REQUEST_ACCEPTED";
    public static final String TYPE_NEW_JOIN_REQUEST = "NEW_JOIN_REQUEST";

    public Extra extra;

    public String message;

    public String getType() {
        if (extra != null) {
            return extra.type;
        }
        return "";
    }

    public int getUserId() {
        if (extra != null) {
            return extra.userId;
        }
        return 0;
    }

    public long getJoinableId() {
        if (extra != null) {
            return extra.joinableId;
        }
        return 0;
    }

    public boolean isTourRelated() {
        if (extra != null) {
            return Extra.JOINABLE_TYPE_TOUR.equals(extra.joinableType);
        }
        return false;
    }

    public boolean isEntourageRelated() {
        if (extra != null) {
            return Extra.JOINABLE_TYPE_ENTOURAGE.equals(extra.joinableType);
        }
        return false;
    }

    private class Extra implements Serializable {

        public static final String JOINABLE_TYPE_TOUR = "Tour";
        public static final String JOINABLE_TYPE_ENTOURAGE = "Entourage";

        @SerializedName("joinable_id")
        public long joinableId;

        @SerializedName("joinable_type")
        public String joinableType;

        @SerializedName("user_id")
        public int userId;

        public String type;
    }

}
