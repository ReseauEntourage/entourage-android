package social.entourage.android.user.edit;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.BaseOrganization;
import social.entourage.android.api.model.User;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.user.UserFragment;
import social.entourage.android.user.UserOrganizationsAdapter;
import social.entourage.android.user.edit.partner.UserEditPartnerFragment;
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragment;

public class UserEditFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "user_edit_fragment";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    UserEditPresenter presenter;

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

    @BindView(R.id.user_address)
    TextView userAddress;

    @BindView(R.id.user_about)
    TextView userAbout;

    @BindView(R.id.user_notifications_image)
    ImageView userNotificationsStatusImage;

    @BindView(R.id.user_associations_title)
    TextView userAssociationsTitle;

    @BindView(R.id.user_associations_public)
    TextView userAssociationsPublicInfo;

    @BindView(R.id.user_associations_view)
    RecyclerView userAssociationsView;

    @BindView(R.id.user_add_association_separator)
    View userAddAssociationSeparator;

    @BindView(R.id.user_edit_progressBar)
    ProgressBar progressBar;

    UserOrganizationsAdapter organizationsAdapter;

    private User editedUser;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public UserEditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_user_edit, container, false);
        ButterKnife.bind(this, v);
        EntourageEvents.logEvent(Constants.EVENT_SCREEN_09_2);
        return v;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
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
                User user = EntourageApplication.me(getActivity());
                editedUser = user.clone();
            }

            if (editedUser.getAvatarURL() != null) {
                Picasso.with(getActivity()).load(Uri.parse(editedUser.getAvatarURL()))
                        .placeholder(R.drawable.ic_user_photo)
                        .transform(new CropCircleTransformation())
                        .into(userPhoto);
            }
            else {
                Picasso.with(getActivity()).load(R.drawable.ic_user_photo)
                        .transform(new CropCircleTransformation())
                        .into(userPhoto);
            }

            userFirstname.setText(editedUser.getFirstName());
            userLastname.setText(editedUser.getLastName());
            userEmail.setText(editedUser.getEmail());
            userPhone.setText(editedUser.getPhone());
            userAddress.setText("");
            userAbout.setText(editedUser.getAbout());

            List<BaseOrganization> organizationList = new ArrayList<>();
            if (editedUser.getPartner() != null) {
                organizationList.add(editedUser.getPartner());
            }
            if (editedUser.getOrganization() != null) {
                organizationList.add(editedUser.getOrganization());
            }
            if (organizationsAdapter == null) {
                userAssociationsView.setLayoutManager(new LinearLayoutManager(getActivity()));
                organizationsAdapter = new UserOrganizationsAdapter(organizationList);
                userAssociationsView.setAdapter(organizationsAdapter);
            } else {
                organizationsAdapter.setOrganizationList(organizationList);
            }

            userAddAssociationSeparator.setVisibility(organizationList.size() > 0 ? View.VISIBLE : View.GONE);

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

    @OnClick(R.id.user_edit_close_button)
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

    @OnClick(R.id.user_save_button)
    protected void onSaveButtonClicked() {
        EntourageEvents.logEvent(Constants.EVENT_USER_SAVE);
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
    protected void onAddAssociationClicked() {
        EntourageEvents.logEvent(Constants.EVENT_USER_TOBADGE);
        UserEditPartnerFragment userEditPartnerFragment = new UserEditPartnerFragment();
        userEditPartnerFragment.show(getFragmentManager(), UserEditPartnerFragment.TAG);
    }

    @OnClick(R.id.user_notifications_layout)
    protected void onShowNotificationsSettingsClicked() {
        try {
            EntourageEvents.logEvent(Constants.EVENT_USER_TONOTIFICATIONS);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = new Intent();
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", getActivity().getPackageName());
                intent.putExtra("app_uid", getActivity().getApplicationInfo().uid);
                startActivity(intent);
            } else {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                startActivity(intent);
            }
        } catch (ActivityNotFoundException ex) {
            Log.d("Exception", "Cannot open Notifications Settings page");
        } catch (Exception ex) {

        }
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

        User user = EntourageApplication.me(getActivity());
        editedUser.setAvatarURL(user.getAvatarURL());
        editedUser.setPartner(user.getPartner());

        configureView();
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
            if (getActivity() instanceof DrawerActivity) {
                ((DrawerActivity) getActivity()).selectItem(R.id.action_logout);
            }
        }
        else {
            if (hasActivity) {
                Toast.makeText(getActivity(), R.string.user_delete_account_failure, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
