package social.entourage.android.api.model;

import androidx.annotation.NonNull;

import java.util.Date;

public class LoaderCardItem extends TimestampedObject {
    @Override
    public Date getTimestamp() {
        return null;
    }

    @NonNull
    @Override
    public String hashString() {
        return "LoaderCardItem";
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
