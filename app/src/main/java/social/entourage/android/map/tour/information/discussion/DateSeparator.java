package social.entourage.android.map.tour.information.discussion;

import java.util.Date;

import social.entourage.android.api.model.TimestampedObject;

/**
 * Created by mihaiionescu on 14/03/2017.
 */

public class DateSeparator extends TimestampedObject {

    private Date date;

    @Override
    public int getType() {
        return DATE_SEPARATOR;
    }

    @Override
    public Date getTimestamp() {
        return date;
    }

    @Override
    public String hashString() {
        return "";
    }

    @Override
    public long getId() {
        return 0;
    }

    public void setDate(final Date date) {
        this.date = date;
    }
}
