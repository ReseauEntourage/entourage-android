package social.entourage.android;

import android.content.Intent;
import android.net.Uri;
import androidx.annotation.IdRes;

import android.widget.Toast;

import javax.inject.Inject;

import social.entourage.android.api.AppRequest;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.entourage.EntourageCloseFragment;
import social.entourage.android.privateCircle.PrivateCircleChooseFragment;

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
                PrivateCircleChooseFragment privateCircleChooseFragment = new PrivateCircleChooseFragment();
                privateCircleChooseFragment.show(activity.getSupportFragmentManager(), PrivateCircleChooseFragment.TAG);
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
}
