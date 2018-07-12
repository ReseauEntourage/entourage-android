package social.entourage.android.map.filter;

import android.content.Context;

import social.entourage.android.EntourageApplication;
import social.entourage.android.authentication.AuthenticationController;

/**
 * Created by mihaiionescu on 27/10/16.
 */

public class MapFilterFactory {

    public static MapFilter getMapFilter(boolean isProUser) {
        return new MapFilterPFP();
    }

    public static MapFilter getMapFilter(Context context) {
        EntourageApplication app = EntourageApplication.get(context);
        if (app != null && app.getEntourageComponent() != null) {
            AuthenticationController authenticationController = app.getEntourageComponent().getAuthenticationController();
            if (authenticationController != null) {
                MapFilter mapFilter = authenticationController.getMapFilter();
                if (!(mapFilter instanceof MapFilterPFP)) {
                    mapFilter = new MapFilterPFP();
                    authenticationController.saveMapFilter();
                }
                return mapFilter;
            }
        }
        return new MapFilterPFP();
    }

}
