package social.entourage.android.map.tour.information.discussion;

import java.util.Date;

import social.entourage.android.api.model.TimestampedObject;

/**
 * Created by mihaiionescu on 02/03/16.
 */
public class Separator extends TimestampedObject {
    @Override
    public Date getTimestamp() {
        return null;
    }

    @Override
    public String hashString() {
        return null;
    }

    @Override
    public int getType() {
        return SEPARATOR;
    }

    @Override
    public long getId() {
        return 0;
    }
}
