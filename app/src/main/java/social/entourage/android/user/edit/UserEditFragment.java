package social.entourage.android.user.edit;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import social.entourage.android.MainActivity;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.BaseOrganization;
import social.entourage.android.api.model.User;
import social.entourage.android.api.tape.Events;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.UserPreferences;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.partner.PartnerFragment;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.user.UserFragment;
import social.entourage.android.user.UserOrganizationsAdapter;
import social.entourage.android.user.edit.partner.UserEditPartnerFragment;
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragment;
import timber.log.Timber;

public class UserEditFragment extends EntourageDialogFragment implements UserEditActionZoneFragment.FragmentListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "user_edit_fragment";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    UserEditPresenter presenter;

    @BindView(R.id.scrollView)
    ScrollView scrollView;

    private int scrollViewY = 0;

    @BindView(R.id.user_photo)
    ImageView userPhoto;

    @BindView(R.id.user_edit_firstname)
    TextView userFirstname;

    @BindView(R.id.user_edit_lastname)
    TextView userLastname;

    @BindView(R.id.user_email)
    TextView userEmail;

    @BindView(R.id.user_phone)
    TextView userPhone;

    @BindView(R.id.user_about)
    TextView userAbout;

    @BindView(R.id.user_notifications_image)
    ImageView userNotificationsStatusImage;

    @BindView(R.id.user_associations_view)
    RecyclerView userAssociationsView;

    @BindView(R.id.user_associations_layout)
    View userAssociationLayout;

    @BindView(R.id.user_action_zone)
    TextView userActionZone;

    @BindView(R.id.user_action_zone_button)
    Button userActionZoneButton;

    @BindView(R.id.user_edit_progressBar)
    ProgressBar progressBar;

    private UserOrganizationsAdapter organizationsAdapter;

    private User editedUser;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public UserEditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_user_edit, container, false);
        ButterKnife.bind(this, v);
        EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_09_2);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        configureView();
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerUserEditComponent.builder()
                .entourageComponent(entourageComponent)
                .userEditModule(new UserEditModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        configureNotifications();

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        BusProvider.getInstance().unregister(this);
    }

    public void configureView() {
        if (getActivity() != null) {
            if (editedUser == null) {
                User me = EntourageApplication.me(getActivity());
                if (me == null) return;
                editedUser = me.clone();
                if (editedUser == null) return;
            }

            if (editedUser.getAvatarURL() != null) {
                Picasso.get().load(Uri.parse(editedUser.getAvatarURL()))
                        .placeholder(R.drawable.ic_user_photo)
                        .transform(new CropCircleTransformation())
                        .into(userPhoto);
            }
            else {
                Picasso.get().load(R.drawable.ic_user_photo)
                        .transform(new CropCircleTransformation())
                        .into(userPhoto);
            }

            userFirstname.setText(editedUser.getFirstName());
            userLastname.setText(editedUser.getLastName());
            userEmail.setText(editedUser.getEmail());
            userPhone.setText(editedUser.getPhone());
            userAbout.setText(editedUser.getAbout());

            List<BaseOrganization> organizationList = new ArrayList<>();
            if (editedUser.getPartner() != null) {
                organizationList.add(editedUser.getPartner());
            }
            if (editedUser.getOrganization() != null) {
                organizationList.add(editedUser.getOrganization());
            }

            if(organizationList.size() > 0) {
                if (organizationsAdapter == null) {
                    userAssociationsView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    organizationsAdapter = new UserOrganizationsAdapter(organizationList);
                    userAssociationsView.setAdapter(organizationsAdapter);
                } else {
                    organizationsAdapter.setOrganizationList(organizationList);
                }
                userAssociationLayout.setVisibility(View.VISIBLE);
            }

            User.Address address = editedUser.getAddress();
            if (address == null) {
                userActionZone.setText("");
                userActionZoneButton.setText(R.string.user_edit_action_zone_button_no_address);
            } else {
                String displayAddress = address.getDisplayAddress();
                if (displayAddress == null || displayAddress.length() == 0) {
                    userActionZone.setText("");
                } else {
                    userActionZone.setText(getString(R.string.user_edit_action_zone_format, address.getDisplayAddress()));
                }
                userActionZoneButton.setText(R.string.user_edit_action_zone_button);
            }
        }
    }

    public User getEditedUser() {
        return editedUser;
    }

    public void displayToast(String message) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public void saveNewPassword(String newPassword) {
        if (presenter != null) {
            progressBar.setVisibility(View.VISIBLE);
            presenter.saveNewPassword(newPassword);
        }
    }

    // ----------------------------------
    // Buttons Handling
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseButtonClicked() {
        dismiss();
    }

    @OnClick({R.id.user_firstname_layout, R.id.user_lastname_layout})
    protected void onEditFirstname() {
        showEditProfile(UserEditProfileFragment.EDIT_NAME);
    }

    @OnClick(R.id.user_email_layout)
    protected void onEditEmail() {
        showEditProfile(UserEditProfileFragment.EDIT_EMAIL);
    }

    @OnClick(R.id.user_password_layout)
    protected void onEditPassword() {
        UserEditPasswordFragment fragment = new UserEditPasswordFragment();
        fragment.show(getFragmentManager(), UserEditPasswordFragment.TAG);
    }

    @OnClick(R.id.user_about_edit_button)
    protected void onEditAboutClicked() {
        scrollViewY = scrollView.getScrollY();
        UserEditAboutFragment editAboutFragment = new UserEditAboutFragment();
        editAboutFragment.show(getFragmentManager(), UserEditAboutFragment.TAG);
    }

    @OnClick(R.id.user_delete_account_button)
    protected void onDeleteAccountClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.user_delete_account_dialog)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (presenter != null) {
                            if (getActivity() instanceof EntourageActivity) {
                                ((EntourageActivity) getActivity()).showProgressDialog(0);
                            }
                            presenter.deleteAccount();
                        }
                    }
                })
                .setNegativeButton(R.string.no, null);
        builder.show();
    }

    @OnClick(R.id.title_action_button)
    protected void onSaveButtonClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_USER_SAVE);
        // If we have an user fragment in the stack, let it handle the update
        UserFragment fragment = (UserFragment)getFragmentManager().findFragmentByTag(UserFragment.TAG);
        if (fragment != null) {
            fragment.saveAccount(editedUser);
        } else if (presenter != null) {
            //else we handle it
            presenter.updateUser(editedUser);
        }
    }

    @OnClick(R.id.user_photo_button)
    protected void onPhotoClicked() {
        PhotoChooseSourceFragment fragment = new PhotoChooseSourceFragment();
        fragment.show(getFragmentManager(), PhotoChooseSourceFragment.TAG);
    }

    @OnClick(R.id.user_add_association_button)
    public void onAddAssociationClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_USER_TOBADGE);
        UserEditPartnerFragment userEditPartnerFragment = new UserEditPartnerFragment();
        userEditPartnerFragment.show(getFragmentManager(), UserEditPartnerFragment.TAG);
    }

    @OnClick(R.id.user_notifications_layout)
    protected void onShowNotificationsSettingsClicked() {
        try {
            EntourageEvents.logEvent(EntourageEvents.EVENT_USER_TONOTIFICATIONS);
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getActivity().getPackageName());
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", getActivity().getPackageName());
                intent.putExtra("app_uid", getActivity().getApplicationInfo().uid);
            } else {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
            }
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Timber.e(ex, "Cannot open Notifications Settings page");
        } catch (Exception ignored) {

        }
    }

    // ----------------------------------
    // Protected methods
    // ----------------------------------

    protected void scrollToOriginalPosition() {
        scrollView.scrollTo(0, scrollViewY);
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private void showEditProfile(int editType) {
        UserEditProfileFragment fragment = UserEditProfileFragment.newInstance(editType);
        fragment.show(getFragmentManager(), UserEditProfileFragment.TAG);
    }

    private void configureNotifications() {
        boolean areNotificationsEnabled = NotificationManagerCompat.from(getContext()).areNotificationsEnabled();
        userNotificationsStatusImage.setImageResource(areNotificationsEnabled ? R.drawable.verified  : R.drawable.not_verified);
    }

    // ----------------------------------
    // Events Handling
    // ----------------------------------

    @Subscribe
    public void userInfoUpdated(Events.OnUserInfoUpdatedEvent event) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        User me = EntourageApplication.me(getActivity());
        editedUser.setAvatarURL(me.getAvatarURL());
        editedUser.setPartner(me.getPartner());
        editedUser.setAddress(me.getAddress());

        configureView();
    }

    @Subscribe
    public void onPartnerViewRequested(Events.OnPartnerViewRequestedEvent event) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (event == null) {
            return;
        }
        if (event.getPartner() != null) {
            PartnerFragment.Companion.newInstance(event.getPartner()).show(getParentFragmentManager(), PartnerFragment.TAG);
        } else {
            PartnerFragment.Companion.newInstance(event.getPartnerId()).show(getParentFragmentManager(), PartnerFragment.TAG);
        }
    }

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------

    protected void onUserUpdated(User user) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (user == null) {
            displayToast(getString(R.string.user_text_update_ko));
        }
        else {
            displayToast(getString(R.string.user_text_update_ok));
            dismiss();
        }
    }

    protected void onSaveNewPassword(String newPassword) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (newPassword == null) {
            displayToast(getString(R.string.user_text_update_ko));
        }
        else {
            if (editedUser != null) {
                editedUser.setSmsCode(newPassword);
            }
            displayToast(getString(R.string.user_text_update_ok));
        }
        progressBar.setVisibility(View.GONE);
    }

    protected void onDeletedAccount(boolean success) {
        boolean hasActivity = getActivity() != null && !getActivity().isFinishing() && getActivity() instanceof EntourageActivity;
        if (hasActivity) {
            ((EntourageActivity) getActivity()).dismissProgressDialog();
        }
        if (success) {
            //logout and go back to login screen
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectItem(R.id.action_logout);
            }
        }
        else {
            if (hasActivity) {
                Toast.makeText(getActivity(), R.string.user_delete_account_failure, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ----------------------------------
    // Edit Action Zone
    // ----------------------------------

    @OnClick(R.id.user_action_zone_button)
    protected void onActionZoneEditClicked() {
        if (getFragmentManager() == null) return;
        UserEditActionZoneFragment userEditActionZoneFragment = UserEditActionZoneFragment.newInstance(editedUser.getAddress());
        userEditActionZoneFragment.setFragmentListener(this);
        userEditActionZoneFragment.show(getFragmentManager(), UserEditActionZoneFragment.TAG);
    }

    @Override
    public void onUserEditActionZoneFragmentDismiss() {

    }

    @Override
    public void onUserEditActionZoneFragmentAddressSaved() {
        storeActionZone(false);
    }

    @Override
    public void onUserEditActionZoneFragmentIgnore() {
        storeActionZone(true) ;
    }

    public void storeActionZone(final boolean ignoreActionZone) {
        AuthenticationController authenticationController = EntourageApplication.get().getEntourageComponent().getAuthenticationController();
        UserPreferences userPreferences = authenticationController.getUserPreferences();
        if (userPreferences != null) {
            userPreferences.setIgnoringActionZone(ignoreActionZone);
            authenticationController.saveUserPreferences();
        }
        UserEditActionZoneFragment userEditActionZoneFragment = (UserEditActionZoneFragment)getFragmentManager().findFragmentByTag(UserEditActionZoneFragment.TAG);
        if (userEditActionZoneFragment != null && !userEditActionZoneFragment.isStateSaved()) {
            userEditActionZoneFragment.dismiss();
        }
    }
}
