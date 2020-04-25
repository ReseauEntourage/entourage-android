package social.entourage.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.collection.ArrayMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.AppRequest;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.UserResponse;
import social.entourage.android.api.model.ApplicationInfo;
import social.entourage.android.carousel.CarouselFragment;
import social.entourage.android.configuration.Configuration;
import social.entourage.android.involvement.GetInvolvedFragment;
import social.entourage.android.user.AvatarUpdatePresenter;
import social.entourage.android.user.UserFragment;
import social.entourage.android.user.edit.UserEditFragment;
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragment;
import social.entourage.android.user.edit.photo.PhotoEditFragment;
import timber.log.Timber;

/**
 * The base class for MainPresenter<br/>
 * The derived classes will be per app
 * Created by Mihai Ionescu on 27/04/2018.
 */
public abstract class MainBasePresenter implements AvatarUpdatePresenter {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final String LOG_TAG = "UPDATE_USER_INFO";
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

    protected final MainActivity activity;
    private final AppRequest appRequest;
    private final UserRequest userRequest;
    private boolean checkForUpdate = true;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    MainBasePresenter(final MainActivity activity, final AppRequest appRequest, final UserRequest userRequest) {
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
            case R.id.action_user:
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_TAP_MY_PROFILE);
                UserFragment userFragment = (UserFragment) activity.getSupportFragmentManager().findFragmentByTag(UserFragment.TAG);
                if (userFragment == null) {
                    userFragment = UserFragment.newInstance(activity.getAuthenticationController().getUser().getId());
                }
                userFragment.show(activity.getSupportFragmentManager(), UserFragment.TAG);
                break;
            case R.id.action_edit_profile:
                UserEditFragment fragment = new UserEditFragment();
                fragment.show(activity.getSupportFragmentManager(), UserEditFragment.TAG);
                break;
            case R.id.action_logout:
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_LOGOUT);
                activity.logout();
                break;
            case R.id.action_blog:
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_BLOG);
                activity.showWebViewForLinkId(Constants.SCB_LINK_ID);
                break;
            case R.id.action_charte:
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_CHART);
                Intent charteIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getLink(Constants.CHARTE_LINK_ID)));
                try {
                    activity.startActivity(charteIntent);
                } catch (Exception ex) {
                    Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_goal:
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_GOAL);
                activity.showWebViewForLinkId(Constants.GOAL_LINK_ID);
                break;
            case R.id.action_donation:
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_DONATION);
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
        Button updateButton = dialog.findViewById(R.id.update_dialog_button);
        if (updateButton != null) {
            updateButton.setOnClickListener(v -> {
                try {
                    Uri uri = Uri.parse(activity.getString(R.string.market_url,activity.getPackageName()));
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception e) {
                    Toast.makeText(activity, R.string.error_google_play_store_not_installed, Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }
            });
        }
    }

    void displayTutorial(boolean forced) {
        if (!forced && !Configuration.INSTANCE.showTutorial()) return;
        //Configuration.INSTANCE.showTutorial() is always false
        if (activity != null && activity.isSafeToCommit()) {
            try {
                CarouselFragment carouselFragment = new CarouselFragment();
                carouselFragment.show(activity.getSupportFragmentManager(), CarouselFragment.TAG);
            } catch (Exception e) {
                // This is just to see if we still get the Illegal state exception
                Timber.e(e);
            }
        }
    }

    // ----------------------------------
    // HELPER METHODS
    // ----------------------------------

    private String getDeviceID() {
        return EntourageApplication.get().getSharedPreferences()
                .getString(EntourageApplication.KEY_REGISTRATION_ID, null);
    }

    private void setDeviceID(String pushNotificationToken) {
        final SharedPreferences sharedPreferences = EntourageApplication.get().getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(EntourageApplication.KEY_REGISTRATION_ID, pushNotificationToken);
        editor.apply();
    }

    // ----------------------------------
    // API CALLS METHODS
    // ----------------------------------

    void checkForUpdate() {
        if (checkForUpdate) {
            Call<ResponseBody> call = appRequest.checkForUpdate();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.code() == 426) {
                        if (!BuildConfig.DEBUG) {
                            displayAppUpdateDialog();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Timber.w(t, "Error connecting to API");
                }
            });
            checkForUpdate=false;
        }
    }

    void updateUser(String email, String smsCode, String phone, Location location) {
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
                    public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                        if (response.isSuccessful()) {
                            if (activity.authenticationController.isAuthenticated()) {
                                UserResponse responseBody = response.body();
                                if(responseBody !=null)
                                    activity.authenticationController.saveUser(responseBody.getUser());
                            }
                            Timber.tag(LOG_TAG).d("success");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                        Timber.tag(LOG_TAG).e(t);
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
                public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                    activity.dismissProgressDialog();
                    if (response.isSuccessful()) {
                        if (activity.authenticationController.isAuthenticated()) {
                            UserResponse responseBody = response.body();
                            if(responseBody !=null)
                                activity.authenticationController.saveUser(responseBody.getUser());
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
                    }
                    else {
                        Toast.makeText(activity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show();
                        PhotoEditFragment photoEditFragment = (PhotoEditFragment)activity.getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
                        if (photoEditFragment != null) {
                            photoEditFragment.onPhotoSent(false);
                        }
                        Timber.tag(LOG_TAG).e(activity.getString(R.string.user_photo_error_not_saved));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                    activity.dismissProgressDialog();
                    Timber.tag(LOG_TAG).e(t);
                    Toast.makeText(activity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show();
                    PhotoEditFragment photoEditFragment = (PhotoEditFragment)activity.getSupportFragmentManager().findFragmentByTag(PhotoEditFragment.TAG);
                    if (photoEditFragment != null) {
                        photoEditFragment.onPhotoSent(false);
                    }
                }
            });
        }
    }

    void deleteApplicationInfo() {
        String previousDeviceID = getDeviceID();
        if (previousDeviceID == null || previousDeviceID.equals("")) {
            return;
        }
        ApplicationInfo applicationInfo = new ApplicationInfo(previousDeviceID);
        ApplicationInfo.ApplicationWrapper applicationWrapper = new ApplicationInfo.ApplicationWrapper();
        applicationWrapper.setApplicationInfo(applicationInfo);
        Call<ResponseBody> call = appRequest.deleteApplicationInfo(applicationWrapper);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull final Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Timber.d("deleting application info with success");
                }
                else {
                    Timber.e("deleting application info error");
                }
            }

            @Override
            public void onFailure(@NonNull final Call<ResponseBody> call, @NonNull final Throwable t) {
                Timber.e(t);
            }
        });
        setDeviceID(null);
    }


    void updateApplicationInfo(String pushNotificationToken) {
        if (activity == null) {
            return;
        }
        //delete old one if existing
        if(!pushNotificationToken.equals(getDeviceID())){
            deleteApplicationInfo();
        }
        //then add new one
        setDeviceID(pushNotificationToken);
        ApplicationInfo applicationInfo = new ApplicationInfo(pushNotificationToken);
        ApplicationInfo.ApplicationWrapper applicationWrapper = new ApplicationInfo.ApplicationWrapper();
        applicationWrapper.setApplicationInfo(applicationInfo);
        applicationWrapper.setNotificationStatus(ApplicationInfo.NOTIF_PERMISSION_AUTHORIZED);
        Call<ResponseBody> call = appRequest.updateApplicationInfo(applicationWrapper);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull final Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Timber.d("updating application info with success");
                }
                else {
                    Timber.e("updating application info error");
                }
            }

            @Override
            public void onFailure(@NonNull final Call<ResponseBody> call, @NonNull final Throwable t) {
                Timber.e(t);
            }
        });
    }

}
