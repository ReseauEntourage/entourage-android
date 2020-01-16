package social.entourage.android;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import javax.inject.Inject;

import social.entourage.android.api.AppRequest;
import social.entourage.android.api.UserRequest;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Presenter controlling the DrawerActivity
 * @see DrawerActivity
 */
public class DrawerPresenter extends DrawerBasePresenter {

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Inject
    DrawerPresenter(final DrawerActivity activity, final AppRequest appRequest, final UserRequest userRequest) {
        super(activity, appRequest, userRequest);
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------


    @Override
    protected void handleMenu(final int menuId) {
        if (activity == null) return;
        switch (menuId) {
            case R.id.action_ambassador:
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_AMBASSADOR);
                Intent ambassadorIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getLink(Constants.AMBASSADOR_ID)));
                try {
                    activity.startActivity(ambassadorIntent);
                } catch (Exception ex) {
                    Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sidemenu_app_version:
                final android.content.ClipboardManager clipboardManager = (ClipboardManager)EntourageApplication.get().getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("FirebaseID", FirebaseInstanceId.getInstance().getId());
                clipboardManager.setPrimaryClip(clipData);
                break;
            default:
                super.handleMenu(menuId);
                break;
        }

    }
}
