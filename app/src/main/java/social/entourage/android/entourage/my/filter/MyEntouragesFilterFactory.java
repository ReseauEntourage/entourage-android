package social.entourage.android.entourage.my.filter;

import android.content.Context;

import social.entourage.android.EntourageApplication;
import social.entourage.android.authentication.AuthenticationController;

/**
 * Created by Mihai Ionescu on 18/12/2017.
 */

public class MyEntouragesFilterFactory {

    public static MyEntouragesFilter getMyEntouragesFilter(Context context) {
        EntourageApplication app = EntourageApplication.get(context);
        if (app != null && app.getEntourageComponent() != null) {
            AuthenticationController authenticationController = app.getEntourageComponent().getAuthenticationController();
            if (authenticationController != null) {
                return authenticationController.getMyEntouragesFilter();
            }
        }
        return new MyEntouragesFilter();
    }

    public static void saveMyEntouragesFilter(MyEntouragesFilter myEntouragesFilter, Context context) {
        EntourageApplication app = EntourageApplication.get(context);
        if (app != null && app.getEntourageComponent() != null) {
            AuthenticationController authenticationController = app.getEntourageComponent().getAuthenticationController();
            if (authenticationController != null) {
                authenticationController.saveMyEntouragesFilter();
            }
        }
    }

}
