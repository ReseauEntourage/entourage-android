package social.entourage.android;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
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
import social.entourage.android.guide.GuideMapEntourageFragment;
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
    // PRIVATE METHODS
    // ----------------------------------

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
