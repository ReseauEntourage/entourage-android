package social.entourage.android.api.model;

import social.entourage.android.api.model.map.Announcement;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.Tour;

/**
 * Created by Mihai Ionescu on 18/04/2018.
 */
public class NewsfeedTypes {

    public static Class getNewsfeedClass(String type) {
        if (type.equals(Entourage.NEWSFEED_TYPE)) {
            return Entourage.class;
        }
        else if (type.equals(Announcement.NEWSFEED_TYPE)) {
            return Announcement.class;
        }
        return null;
    }

}
