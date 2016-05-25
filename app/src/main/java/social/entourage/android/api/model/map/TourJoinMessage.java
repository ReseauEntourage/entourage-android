package social.entourage.android.api.model.map;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by mihaiionescu on 27/04/16.
 */
public class TourJoinMessage implements Serializable {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private String message;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public TourJoinMessage(String message) {
        this.message = message;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------


    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    // ----------------------------------
    // WRAPPERS
    // ----------------------------------

    public static class TourJoinMessageWrapper {

        @SerializedName("request")
        private TourJoinMessage joinMessage;

        public TourJoinMessage getJoinMessage() {
            return joinMessage;
        }

        public void setJoinMessage(final TourJoinMessage joinMessage) {
            this.joinMessage = joinMessage;
        }
    }
}
