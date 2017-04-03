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
    public static final String TYPE_ENTOURAGE_INVITATION = "ENTOURAGE_INVITATION";
    public static final String TYPE_INVITATION_STATUS = "INVITATION_STATUS";

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
        return extra != null && Extra.JOINABLE_TYPE_TOUR.equals(extra.joinableType);
    }

    public boolean isEntourageRelated() {
        return extra != null && Extra.JOINABLE_TYPE_ENTOURAGE.equals(extra.joinableType);
    }

    public class Extra implements Serializable {

        public static final String JOINABLE_TYPE_TOUR = "Tour";
        public static final String JOINABLE_TYPE_ENTOURAGE = "Entourage";

        @SerializedName(value = "joinable_id", alternate = {"feed_id"})
        public long joinableId;

        @SerializedName(value = "joinable_type", alternate = {"feed_type"})
        public String joinableType;

        @SerializedName("user_id")
        public int userId;

        @SerializedName("entourage_id")
        public long entourageId;

        @SerializedName("inviter_id")
        public int inviterId;

        @SerializedName("invitee_id")
        public int inviteeId;

        @SerializedName("invitation_id")
        public int invitationId;

        public String type;
    }

}
