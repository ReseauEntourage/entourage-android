package social.entourage.android;

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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.about.AboutFragment;
import social.entourage.android.api.AppRequest;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.UserResponse;
import social.entourage.android.api.model.ApplicationInfo;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.carousel.CarouselFragment;
import social.entourage.android.configuration.Configuration;
import social.entourage.android.involvement.GetInvolvedFragment;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.map.entourage.my.MyEntouragesFragment;
import social.entourage.android.map.tour.my.MyToursFragment;
import social.entourage.android.newsfeed.FeedItemOptionsFragment;
import social.entourage.android.user.UserFragment;
import social.entourage.android.user.edit.UserEditFragment;
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragment;
import social.entourage.android.user.edit.photo.PhotoEditFragment;

/**
 * The base class for DrawerPresenter<br/>
 * The derived classes will be per app
 * Created by Mihai Ionescu on 27/04/2018.
 */
public abstract class DrawerBasePresenter {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final String LOG_TAG = "UPDATE_USER_INFO";
    private final String MARKET_PREFIX = "market://details?id=";
    private final static String KEY_EMAIL = "email";
    private final static String KEY_SMS_COE= "sms_code";
    private final static String KEY_PHONE= "phone";
    private final static String KEY_DEVICE_ID = "device_id";
    private final static String KEY_DEVICE_TYPE = "device_type";
    private final static String KEY_DEVICE_LOCATION = "device_location";
    private final static String ANDROID_DEVICE = "android";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    protected final DrawerActivity activity;
    private final AppRequest appRequest;
    private final UserRequest userRequest;
    private boolean checkForUpdate = true;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    DrawerBasePresenter(final DrawerActivity activity, final AppRequest appRequest, final UserRequest userRequest) {
        this.activity = activity;
        this.appRequest = appRequest;
        this.userRequest = userRequest;
    }

    // ----------------------------------
    // MENU HANDLING
    // ----------------------------------

    protected void handleMenu(@IdRes int menuId) {
        if (activity == null) return;
        switch (menuId) {
            case R.id.action_tours:
                activity.loadFragmentWithExtras();
                break;
            case R.id.action_guide:
                displaySolidarityGuide();
                EntourageEvents.logEvent(Constants.EVENT_OPEN_GUIDE_FROM_SIDEMENU);
                break;
            case R.id.action_user:
                EntourageEvents.logEvent(Constants.EVENT_MENU_TAP_MY_PROFILE);
                UserFragment userFragment = (UserFragment) activity.getSupportFragmentManager().findFragmentByTag(UserFragment.TAG);
                if (userFragment == null) {
                    userFragment = UserFragment.newInstance(activity.getAuthenticationController().getUser().getId());
                }
                userFragment.show(activity.getSupportFragmentManager(), UserFragment.TAG);
                break;
            case R.id.action_edit_user:
                UserEditFragment fragment = new UserEditFragment();
                fragment.show(activity.getSupportFragmentManager(), UserEditFragment.TAG);
                break;
            case R.id.action_logout:
                EntourageEvents.logEvent(Constants.EVENT_MENU_LOGOUT);
                activity.logout();
                break;
            case R.id.action_settings:
                Toast.makeText(activity, R.string.error_not_yet_implemented, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_about:
                /*
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("entourage-staging://tutorial"));
                        try {
                            startActivity(intent);
                        } catch (Exception ex) {
                            Log.d("DEEPLINK", ex.toString());
                        }
                    }
                }, 8*1000);
                */

                EntourageEvents.logEvent(Constants.EVENT_MENU_ABOUT);
                AboutFragment aboutFragment = new AboutFragment();
                aboutFragment.show(activity.getSupportFragmentManager(), AboutFragment.TAG);
                break;
            case R.id.action_blog:
                EntourageEvents.logEvent(Constants.EVENT_MENU_BLOG);
                activity.showWebViewForLinkId(Constants.SCB_LINK_ID);
                break;
            case R.id.action_charte:
                EntourageEvents.logEvent(Constants.EVENT_MENU_CHART);
                Intent charteIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getLink(Constants.CHARTE_LINK_ID)));
                try {
                    activity.startActivity(charteIntent);
                } catch (Exception ex) {
                    Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_goal:
                EntourageEvents.logEvent(Constants.EVENT_MENU_GOAL);
                activity.showWebViewForLinkId(Constants.GOAL_LINK_ID);
                break;
            case R.id.action_atd:
                EntourageEvents.logEvent(Constants.EVENT_MENU_ATD);
                Intent atdIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getLink(Constants.ATD_LINK_ID)));
                try {
                    activity.startActivity(atdIntent);
                } catch (Exception ex) {
                    Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_donation:
                EntourageEvents.logEvent(Constants.EVENT_MENU_DONATION);
                Intent donationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getLink(Constants.DONATE_LINK_ID)));
                try {
                    activity.startActivity(donationIntent);
                } catch (Exception ex) {
                    Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_involvement:
                GetInvolvedFragment getInvolvedFragment = GetInvolvedFragment.newInstance();
                getInvolvedFragment.show(activity.getSupportFragmentManager(), GetInvolvedFragment.TAG);
                break;
            default:
                //Snackbar.make(contentView, getString(R.string.drawer_error, menuItem.getTitle()), Snackbar.LENGTH_LONG).show();
                Toast.makeText(activity, R.string.error_not_yet_implemented, Toast.LENGTH_SHORT).show();
        }
    }

    // ----------------------------------
    // DISPLAY SCREENS METHODS
    // ----------------------------------

    private void displayAppUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog dialog = builder.setView(R.layout.dialog_version_update)
                .setCancelable(false)
                .create();
        dialog.show();
        Button updateButton = (Button) dialog.findViewById(R.id.update_dialog_button);
        if (updateButton != null) {
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Uri uri = Uri.parse(MARKET_PREFIX + activity.getPackageName());
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (Exception e) {
                        Toast.makeText(activity, R.string.error_google_play_store_not_installed, Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                }
            });
        }
    }

    protected void displayMyTours() {
        if (activity != null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            MyToursFragment fragment = new MyToursFragment();
            fragment.show(fragmentManager, MyToursFragment.TAG);
        }
    }

    protected void displayFeedItemOptions(FeedItem feedItem) {
        if (activity != null && activity.isSafeToCommit()) {
            FeedItemOptionsFragment feedItemOptionsFragment = FeedItemOptionsFragment.newInstance(feedItem);
            feedItemOptionsFragment.show(activity.getSupportFragmentManager(), FeedItemOptionsFragment.TAG);
        }

    }

    protected void displaySolidarityGuide() {
        // does nothing
    }

    protected void proposePOI() {
        // does nothing
    }

    protected void displayTutorial() {
        if (!Configuration.getInstance().showTutorial()) return;
        if (activity != null && activity.isSafeToCommit()) {
            CarouselFragment carouselFragment = new CarouselFragment();
            try {
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                carouselFragment.show(fragmentManager, CarouselFragment.TAG);
            } catch (Exception e) {
                // This is just to see if we still get the Illegal state exception
                EntourageEvents.logEvent(Constants.EVENT_CAROUSEL_EXCEPTION);
            }
        }
    }

    // ----------------------------------
    // HELPER METHODS
    // ----------------------------------

    protected String getDeviceID() {
        return EntourageApplication.get().getSharedPreferences()
                .getString(EntourageApplication.KEY_REGISTRATION_ID, null);
    }

    // ----------------------------------
    // API CALLS METHODS
    // ----------------------------------

    public void checkForUpdate() {
        if (checkForUpdate) {
            Call<ResponseBody> call = appRequest.checkForUpdate();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == 426) {
                        if (!BuildConfig.DEBUG) {
                            displayAppUpdateDialog();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("CheckForUpdate", "Error connecting to API");
                }
            });
            checkForUpdate=false;
        }
    }

    public void updateUser(String email, String smsCode, String phone, Location location) {
        if (activity != null) {

            String deviceId = getDeviceID();

            if (deviceId != null) {

                ArrayMap<String, Object> user = new ArrayMap<>();
                if (email != null) {
                    user.put(KEY_EMAIL, email);
                }
                if (smsCode != null) {
                    user.put(KEY_SMS_COE, smsCode);
                }
                if (phone != null) {
                    user.put(KEY_PHONE, phone);
                }
                if (location != null) {
                    user.put(KEY_DEVICE_LOCATION, location);
                }
                user.put(KEY_DEVICE_ID, deviceId);
                user.put(KEY_DEVICE_TYPE, ANDROID_DEVICE);

                Call<UserResponse> call = userRequest.updateUser(user);
                call.enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        if (response.isSuccessful()) {
                            if (activity.authenticationController.isAuthenticated()) {
                                activity.authenticationController.saveUser(response.body().getUser());
                            }
                            Log.d(LOG_TAG, "success");
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) {
                        Log.d(LOG_TAG, t.getLocalizedMessage());
                    }
                });
            }
        }
    }

    public void updateUserPhoto(String amazonFile) {
        if (activity != null) {

            ArrayMap<String, Object> user = new ArrayMap<>();
            user.put("avatar_key", amazonFile);
            ArrayMap<String, Object> request = new ArrayMap<>();
            request.put("user", user);

            Call<UserResponse> call = userRequest.updateUser(request);
            call.enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    activity.dismissProgressDialog();
                    if (response.isSuccessful()) {
                        if (activity.authenticationController.isAuthenticated()) {
                            activity.authenticationController.saveUser(response.body().getUser());
                        }
                        PhotoEditFragment photoEditFragment = (PhotoEditFragment)activity.getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
                        if (photoEditFragment != null) {
                            if (photoEditFragment.onPhotoSent(true)) {
                                PhotoChooseSourceFragment photoChooseSourceFragment = (PhotoChooseSourceFragment)activity.getSupportFragmentManager().findFragmentByTag(PhotoChooseSourceFragment.TAG);
                                if (photoChooseSourceFragment != null) {
                                    photoChooseSourceFragment.dismiss();
                                }
                            }
                        }
                        Log.d(LOG_TAG, "success");
                    }
                    else {
                        Toast.makeText(activity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show();
                        PhotoEditFragment photoEditFragment = (PhotoEditFragment)activity.getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
                        if (photoEditFragment != null) {
                            photoEditFragment.onPhotoSent(false);
                        }
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    activity.dismissProgressDialog();
                    Log.d(LOG_TAG, t.getLocalizedMessage());
                    Toast.makeText(activity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show();
                    PhotoEditFragment photoEditFragment = (PhotoEditFragment)activity.getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
                    if (photoEditFragment != null) {
                        photoEditFragment.onPhotoSent(false);
                    }
                }
            });
        }
    }

    public void updateApplicationInfo(String pushNotificationToken) {
        if (activity == null) {
            return;
        }
        ApplicationInfo applicationInfo = new ApplicationInfo(pushNotificationToken);
        ApplicationInfo.ApplicationWrapper applicationWrapper = new ApplicationInfo.ApplicationWrapper();
        applicationWrapper.setApplicationInfo(applicationInfo);
        Call<ResponseBody> call = appRequest.updateApplicationInfo(applicationWrapper);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(final Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(LOG_TAG, "updating application info with success");
                }
                else {
                    Log.d(LOG_TAG, "updating application info error");
                }
            }

            @Override
            public void onFailure(final Call<ResponseBody> call, final Throwable t) {
                Log.d(LOG_TAG, t.getLocalizedMessage());
            }
        });
    }

}
