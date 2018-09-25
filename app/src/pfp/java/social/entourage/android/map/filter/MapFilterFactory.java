package social.entourage.android.map.filter;

import android.content.Context;

import social.entourage.android.EntourageApplication;
import social.entourage.android.authentication.AuthenticationController;

/**
 * Created by mihaiionescu on 27/10/16.
 */

public class MapFilterFactory {

    public static MapFilter getMapFilter(boolean isProUser) {
        return new MapFilter();
    }

    public static MapFilter getMapFilter() {
        EntourageApplication app = EntourageApplication.get();
        if (app != null && app.getEntourageComponent() != null) {
            AuthenticationController authenticationController = app.getEntourageComponent().getAuthenticationController();
            if (authenticationController != null) {
                return authenticationController.getMapFilter();
            }
        }
        return new MapFilter();
    }

}
