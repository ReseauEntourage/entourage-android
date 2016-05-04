package social.entourage.android.api.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by mihaiionescu on 26/02/16.
 */
public abstract class TimestampedObject {

    public static final int SEPARATOR = 0;
    public static final int CHAT_MESSAGE_ME = 1;
    public static final int CHAT_MESSAGE_OTHER = 2;
    public static final int TOUR_USER = 3;
    public static final int TOUR_STATUS = 4;
    public static final int ENCOUNTER = 5;

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
