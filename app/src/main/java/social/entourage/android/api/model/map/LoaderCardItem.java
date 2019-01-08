package social.entourage.android.api.model.map;

import social.entourage.android.api.model.TimestampedObject;

import java.util.Date;

public class LoaderCardItem extends TimestampedObject {
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
        return LOADER_CARD;
    }

    @Override
    public long getId() {
        return 0;
    }
}
