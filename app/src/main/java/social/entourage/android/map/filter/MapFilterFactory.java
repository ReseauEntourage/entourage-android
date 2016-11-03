package social.entourage.android.map.filter;

import android.content.Context;

import social.entourage.android.EntourageApplication;
import social.entourage.android.api.model.User;

/**
 * Created by mihaiionescu on 27/10/16.
 */

public class MapFilterFactory {

    public static MapFilter getMapFilter(boolean isProUser) {
        if (isProUser) return MapFilterPro.getInstance();
        return MapFilterPublic.getInstance();
    }

    public static MapFilter getMapFilter(Context context) {
        User me = EntourageApplication.me(context);
        if (me == null) return MapFilterPublic.getInstance();
        return getMapFilter(me.isPro());
    }

}
