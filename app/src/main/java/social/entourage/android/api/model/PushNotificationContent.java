package social.entourage.android.api.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by mihaiionescu on 18/03/16.
 */
public class PushNotificationContent implements Serializable {

    private static final long serialVersionUID = -8538280283990931663L;

    public static final String TYPE_NEW_CHAT_MESSAGE = "NEW_CHAT_MESSAGE";
    public static final String TYPE_JOIN_REQUEST_ACCEPTED = "JOIN_REQUEST_ACCEPTED";
    public static final String TYPE_JOIN_REQUEST_CANCELED = "JOIN_REQUEST_CANCELED";
    public static final String TYPE_NEW_JOIN_REQUEST = "NEW_JOIN_REQUEST";
    public static final String TYPE_ENTOURAGE_INVITATION = "ENTOURAGE_INVITATION";
    public static final String TYPE_INVITATION_STATUS = "INVITATION_STATUS";

    private static final String TAG_TOUR= "tour-";
    private static final String TAG_ENTOURAGE = "entourage-";

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

    public String getJoinableUUID() {
        if (extra != null) {
            return String.valueOf(extra.joinableId);
        }
        return "";
    }

    public boolean isTourRelated() {
        return extra != null && Extra.JOINABLE_TYPE_TOUR.equals(extra.joinableType);
    }

    public boolean isEntourageRelated() {
        return extra != null && Extra.JOINABLE_TYPE_ENTOURAGE.equals(extra.joinableType);
    }

    @NonNull
    String getFeedItemName() {
        if (message == null) return "";
        int index = message.lastIndexOf(':');
        if (index == -1 || index >= message.length() - 2) return "";
        return message.substring(index + 2);
    }

    /**
     * Returns the tag used by the notification (it can be null)
     * @return the tag
     */
    @Nullable
    public String getNotificationTag() {
       if (PushNotificationContent.TYPE_NEW_JOIN_REQUEST.equals(getType())
                ||(PushNotificationContent.TYPE_NEW_CHAT_MESSAGE.equals(getType()))) {
            if (isTourRelated()) {
                return  TAG_TOUR+ String.valueOf(getJoinableId());
            } else if (isEntourageRelated()) {
                return TAG_ENTOURAGE + String.valueOf(getJoinableId());
            }
        }
        return null;
    }

    public class Extra implements Serializable {

        private static final long serialVersionUID = 9200497161789347105L;

        static final String JOINABLE_TYPE_TOUR = "Tour";
        static final String JOINABLE_TYPE_ENTOURAGE = "Entourage";

        @SerializedName(value = "joinable_id", alternate = {"feed_id"})
        long joinableId;

        public String joinableUUID = "";

        @SerializedName(value = "joinable_type", alternate = {"feed_type"})
        String joinableType;

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
