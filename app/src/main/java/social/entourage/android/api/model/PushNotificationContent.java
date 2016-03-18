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

    public long getTourId() {
        if (extra != null) {
            return extra.tourId;
        }
        return 0;
    }

    public int getUserId() {
        if (extra != null) {
            return extra.userId;
        }
        return 0;
    }

    private class Extra implements Serializable {

        @SerializedName("tour_id")
        public long tourId;

        public int userId;

        public String type;
    }

}
