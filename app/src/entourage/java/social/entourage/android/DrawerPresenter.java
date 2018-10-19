package social.entourage.android;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import javax.inject.Inject;

import social.entourage.android.api.AppRequest;
import social.entourage.android.api.UserRequest;
import social.entourage.android.guide.GuideMapEntourageFragment;
import social.entourage.android.navigation.BottomNavigationDataSource;

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
                EntourageEvents.logEvent(Constants.EVENT_MENU_AMBASSADOR);
                Intent ambassadorIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getLink(Constants.AMBASSADOR_ID)));
                try {
                    activity.startActivity(ambassadorIntent);
                } catch (Exception ex) {
                    Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.handleMenu(menuId);
                break;
        }

    }

    @Override
    protected void displaySolidarityGuide() {
        if (activity == null) return;
        //TODO Fix the showing of solidarity guide
        // Change the Guide Option text
//        FloatingActionButton button = activity.mapOptionsMenu.findViewById(R.id.button_poi_launcher);
//        button.setLabelText(activity.getString(R.string.map_poi_close_button));
//        // Make the 'Propose POI' button visible
//        FloatingActionButton proposePOIButton = activity.mapOptionsMenu.findViewById(R.id.button_poi_propose);
//        if (proposePOIButton != null) {
//            proposePOIButton.setVisibility(View.VISIBLE);
//        }
//        // Hide the overlay
//        if (activity.mapOptionsMenu.isOpened()) {
//            activity.mapOptionsMenu.close(false);
//        }
        // Inform the map that the guide will be shown
        if (activity.mapEntourageFragment != null) {
            activity.mapEntourageFragment.onGuideWillShow();
        }
        // Show the fragment
        GuideMapEntourageFragment guideMapEntourageFragment = (GuideMapEntourageFragment) activity.getSupportFragmentManager().findFragmentByTag(GuideMapEntourageFragment.TAG);
        if (guideMapEntourageFragment == null) {
            guideMapEntourageFragment = new GuideMapEntourageFragment();
        }
        activity.selectNavigationTab(BottomNavigationDataSource.TAB_MAP);
        activity.loadFragment(guideMapEntourageFragment, GuideMapEntourageFragment.TAG);
    }

    @Override
    protected void proposePOI() {
        GuideMapEntourageFragment guideMapEntourageFragment = (GuideMapEntourageFragment) activity.getSupportFragmentManager().findFragmentByTag(GuideMapEntourageFragment.TAG);
        if (guideMapEntourageFragment != null) {
            guideMapEntourageFragment.proposePOI();
        }
    }
}
