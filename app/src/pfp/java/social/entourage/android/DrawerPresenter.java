package social.entourage.android;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.AppRequest;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.UserResponse;
import social.entourage.android.api.model.ApplicationInfo;
import social.entourage.android.map.entourage.my.MyEntouragesFragment;
import social.entourage.android.map.tour.my.MyToursFragment;
import social.entourage.android.message.push.RegisterGCMService;
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragment;
import social.entourage.android.user.edit.photo.PhotoEditFragment;

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
    // MENU HANDLING
    // ----------------------------------

    @Override
    protected void handleMenu(@IdRes int menuId) {
        if (activity == null) return;
        switch (menuId) {
            case R.id.action_update_info:
                Toast.makeText(activity, R.string.error_not_yet_implemented, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_contact:
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                String[] addresses = {activity.getString(R.string.contact_email)};
                intent.putExtra(Intent.EXTRA_EMAIL, addresses);
                if (intent.resolveActivity(activity.getPackageManager()) != null) {
                    activity.startActivity(intent);
                } else {
                    Toast.makeText(activity, R.string.error_no_email, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_feedback:
                activity.showWebViewForLinkId(Constants.FEEDBACK_ID);
                break;
            case R.id.action_propose:
                Toast.makeText(activity, R.string.error_not_yet_implemented, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_how_to:
                Intent howToIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.how_to_url)));
                try {
                    activity.startActivity(howToIntent);
                } catch (Exception ex) {
                    Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.handleMenu(menuId);
                break;
        }
    }

    // ----------------------------------
    // DISPLAY SCREENS METHODS
    // ----------------------------------

}
