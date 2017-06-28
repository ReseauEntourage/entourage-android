package social.entourage.android.api.model;

import com.google.gson.annotations.Expose;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by mihaiionescu on 26/02/16.
 */
public abstract class TimestampedObject {

    public static final int SEPARATOR = 0;
    public static final int CHAT_MESSAGE_ME = 1;
    public static final int CHAT_MESSAGE_OTHER = 2;
    public static final int TOUR_USER_JOIN = 3;
    public static final int TOUR_STATUS = 4;
    public static final int ENCOUNTER = 5;
    public static final int TOUR_CARD = 6;
    public static final int ENTOURAGE_CARD = 7;
    public static final int FEED_MEMBER_CARD = 8;
    public static final int INVITATION_CARD = 9;
    public static final int DATE_SEPARATOR = 10;
    public static final int TOP_VIEW = 998;
    public static final int BOTTOM_VIEW = 999;

    @Expose(serialize = false)
    private int hashCode;

    public abstract Date getTimestamp();

    public abstract String hashString();

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = hashString().hashCode();
        }
        return hashCode;
    }

    public abstract int getType();

    public abstract long getId();

    /**
     * Copies the local fields (the ones not retrieved from the server) from other object
     * @param other The object to copy from
     */
    public void copyLocalFields(TimestampedObject other) {}


    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public static class TimestampedObjectComparatorOldToNew implements Comparator<TimestampedObject> {
        @Override
        public int compare(final TimestampedObject lhs, final TimestampedObject rhs) {
            if (lhs.getTimestamp() != null && rhs.getTimestamp() != null) {
                Date date1 = lhs.getTimestamp();
                Date date2 = rhs.getTimestamp();
                return date1.compareTo(date2);
            } else {
                return 0;
            }
        }
    }
}
