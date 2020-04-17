package social.entourage.android;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import javax.inject.Inject;

import social.entourage.android.about.EntourageAboutFragment;
import social.entourage.android.api.AppRequest;
import social.entourage.android.api.UserRequest;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Presenter controlling the MainActivity
 * @see MainActivity
 */
public class MainPresenter extends MainBasePresenter {

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    @Inject
    MainPresenter(final MainActivity activity, final AppRequest appRequest, final UserRequest userRequest) {
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
            case R.id.mainprofile_app_version:
                final android.content.ClipboardManager clipboardManager = (ClipboardManager)EntourageApplication.get().getSystemService(CLIPBOARD_SERVICE);
                if(clipboardManager!=null){
                    ClipData clipData = ClipData.newPlainText("FirebaseID", FirebaseInstanceId.getInstance().getId());
                    clipboardManager.setPrimaryClip(clipData);
                }
                break;
            case R.id.action_about:
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_ABOUT);
                EntourageAboutFragment aboutFragment = new EntourageAboutFragment();
                aboutFragment.show(activity.getSupportFragmentManager(), EntourageAboutFragment.TAG);
                break;
            default:
                super.handleMenu(menuId);
                break;
        }
    }
}