package social.entourage.android.user;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import butterknife.Optional;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.Partner;
import social.entourage.android.api.model.Stats;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.configuration.Configuration;
import social.entourage.android.entourage.information.EntourageInformationFragment;
import social.entourage.android.partner.PartnerFragment;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.user.edit.UserEditAboutFragment;
import social.entourage.android.user.edit.UserEditFragment;
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragment;
import social.entourage.android.user.report.UserReportFragment;
import social.entourage.android.view.PartnerLogoImageView;
import timber.log.Timber;

public class UserFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "fragment_user";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private View toReturn;

    @Inject
    UserPresenter presenter;

    @BindView(R.id.user_profile_scrollview)
    ScrollView scrollView;

    @BindView(R.id.user_profile_edit_button)
    TextView userEditProfile;

    @BindView(R.id.user_profile_report_button)
    ImageButton userReportButton;

    @BindView(R.id.user_photo)
    ImageView userPhoto;

    @Nullable
    @BindView(R.id.user_photo_button)
    TextView userPhotoEdit;

    @BindView(R.id.user_partner_logo)
    PartnerLogoImageView userPartnerLogo;

    @BindView(R.id.user_name)
    UserNameView userName;

    @BindView(R.id.user_member_since)
    TextView userMemberSince;

    @BindView(R.id.user_address)
    TextView userAddress;

    @BindView(R.id.user_profile_about_layout)
    View userAboutLayout;

    @BindView(R.id.user_profile_about)
    TextView userAboutTextView;

    @Nullable
    @BindView(R.id.user_about_edit_button)
    TextView userAboutEditButton;

    @BindView(R.id.user_identification_email_check)
    ImageView userEmailVerifiedImage;

    @BindView(R.id.user_identification_phone_check)
    ImageView userPhoneVerifiedImage;

    @BindView(R.id.user_tours_count)
    TextView userTourCount;

    @Nullable
    @BindView(R.id.user_profile_associations)
    UserAssociations userAssociations;

    @Nullable
    @BindView(R.id.user_message_layout)
    View userMessageLayout;

    @BindView(R.id.user_profile_progressBar)
    ProgressBar progressBar;

    private User user;
    private boolean isMyProfile = false;


    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public static UserFragment newInstance(int userId) {
        UserFragment userFragment = new UserFragment();
        Bundle args = new Bundle();
        args.putInt(User.KEY_USER_ID, userId);
        userFragment.setArguments(args);
        return userFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (toReturn == null) {
            toReturn = inflater.inflate(R.layout.fragment_user, container, false);
        }
        ButterKnife.bind(this, toReturn);
        EntourageEvents.logEvent(EntourageEvents.EVENT_PROFILE_FROM_MENU);
        return toReturn;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        int requestedUserId = getArguments().getInt(User.KEY_USER_ID);
        if (presenter != null) {
            User authenticatedUser = presenter.getAuthenticatedUser();
            if (authenticatedUser != null && requestedUserId == authenticatedUser.getId()) {
                isMyProfile = true;
                user = authenticatedUser;
                configureView();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                presenter.getUser(requestedUserId);
            }
        }

    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerUserComponent.builder()
                .entourageComponent(entourageComponent)
                .userModule(new UserModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        BusProvider.getInstance().unregister(this);
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void configureView() {
        EntourageEvents.logEvent(isMyProfile ? EntourageEvents.EVENT_SCREEN_09_1_ME : EntourageEvents.EVENT_SCREEN_09_1_OTHER);
        if (getActivity() != null && !getActivity().isFinishing()) {
            Stats stats = user.getStats();
            int entourageCount = 0;
            if (stats != null) {
                entourageCount = stats.getEntourageCount();
            }

            userEditProfile.setVisibility(isMyProfile ? View.VISIBLE : View.GONE);
            userReportButton.setVisibility(isMyProfile ? View.GONE : View.VISIBLE);
            if (userAboutEditButton != null) userAboutEditButton.setVisibility(isMyProfile ? View.VISIBLE : View.GONE);

            if (user.getAvatarURL() != null) {
                Picasso.get().load(Uri.parse(user.getAvatarURL()))
                        .placeholder(R.drawable.ic_user_photo)
                        .transform(new CropCircleTransformation())
                        .into(userPhoto);
            }
            else {
                Picasso.get().load(R.drawable.ic_user_photo)
                        .transform(new CropCircleTransformation())
                        .into(userPhoto);
            }
            // Show the partner logo, if available
            String partnerURL = null;
            Partner partner = user.getPartner();
            if (partner != null) {
                partnerURL = partner.getSmallLogoUrl();
            }
            if (partnerURL != null) {
                Picasso.get()
                        .load(Uri.parse(partnerURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .transform(new CropCircleTransformation())
                        .into(userPartnerLogo);
            } else {
                userPartnerLogo.setImageDrawable(null);
            }
            if (userPhotoEdit != null) userPhotoEdit.setVisibility(isMyProfile ? View.VISIBLE : View.GONE);

            userName.setText(user.getDisplayName());
            userName.setRoles(user.getRoles());
            userTourCount.setText(getString(R.string.user_entourage_count_format, entourageCount));

            String userAbout = user.getAbout();
            userAboutLayout.setVisibility( (userAbout == null || userAbout.trim().length() == 0) ? View.GONE : View.VISIBLE);
            userAboutTextView.setText(userAbout);

            boolean userEmailVerified = user.getEmail() != null;
            userPhoneVerifiedImage.setImageResource(R.drawable.verified);
            userEmailVerifiedImage.setImageResource(userEmailVerified ? R.drawable.verified : R.drawable.not_verified);

            if (userAssociations != null) {
                userAssociations.initUserAssociations(user, this);
            }

            //User message layout is available only for the other users, if the conversation field is set
            if (userMessageLayout != null) {
                userMessageLayout.setVisibility(isMyProfile || user.getConversation() == null ? View.GONE : View.VISIBLE);
            }
        }
    }

    private void showUserEditFragment() {
        // Allow editing only of the logged user and if enabled in configuration
        if (!(isMyProfile && Configuration.INSTANCE.showUserEditProfile())) return;
        // Show the edit profile screen
        UserEditFragment fragment = new UserEditFragment();
        try{
            fragment.show(getParentFragmentManager(), UserEditFragment.TAG);
        } catch(IllegalStateException e) {
            Timber.w(e);
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void displayToast(String message) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public void displayToast(@StringRes int messageResIs) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), messageResIs, Toast.LENGTH_SHORT).show();
        }
    }

    public void saveAccount(User user) {
        if (presenter != null) {
            presenter.updateUser(user);
        }
    }

    public User getEditedUser() {
        if (user != null) {
            return user.clone();
        }
        return null;
    }

    // ----------------------------------
    // Presenter Callbacks
    // ----------------------------------

    protected void onUserReceived(User user) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        progressBar.setVisibility(View.GONE);
        if (user == null) {
            displayToast(R.string.user_retrieval_error);
            return;
        }
        this.user = user;
        configureView();
    }

    protected void onUserUpdated(User user) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (user == null) {
            displayToast(R.string.user_text_update_ko);
        }
        else {
            //update the current view
            this.user = user;
            configureView();
            //update the edit view, if available
            if (getFragmentManager() != null) {
                UserEditFragment userEditFragment = (UserEditFragment) getFragmentManager().findFragmentByTag(UserEditFragment.TAG);
                if (userEditFragment != null) {
                    userEditFragment.dismiss();
                }
            }
            displayToast(R.string.user_text_update_ok);
        }
    }

    // ----------------------------------
    // ONCLICK CALLBACKS
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseButtonClicked() {
        dismiss();
    }

    @OnClick(R.id.user_profile_edit_button)
    protected void onEditButtonClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_USER_EDIT_PROFILE);
        showUserEditFragment();
    }

    @OnClick(R.id.user_photo)
    protected void onUserPhotoClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_USER_EDIT_PHOTO);
        showUserEditFragment();
    }

    @OnClick({R.id.user_name, R.id.user_identification_email_layout, R.id.user_identification_phone_layout, R.id.user_number_of_entourages_layout})
    protected void onEditProfileClicked() {
        showUserEditFragment();
    }

    @OnClick(R.id.user_profile_report_button)
    protected void onReportUserClicked() {
        if (user == null) return;
        // Build the email intent
//        Intent intent = new Intent(Intent.ACTION_SENDTO);
//        intent.setData(Uri.parse("mailto:"));
//        // Set the email to
//        String[] addresses = {Constants.EMAIL_CONTACT};
//        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
//        // Set the subject
//        String emailSubject = getString(R.string.user_report_email_subject, user.getDisplayName());
//        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
//        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
//            // Start the intent
//            startActivity(intent);
//        } else {
//            // No Email clients
//            Toast.makeText(getContext(), R.string.error_no_email, Toast.LENGTH_SHORT).show();
//        }

        UserReportFragment userReportFragment = UserReportFragment.newInstance(user.getId());
        userReportFragment.show(getFragmentManager(), UserReportFragment.TAG);
    }

    @Optional
    @OnClick(R.id.user_message_button)
    protected void onMessageUserClicked() {
//        UserDiscussionFragment userDiscussionFragment = UserDiscussionFragment.newInstance(user, false);
//        userDiscussionFragment.show(getFragmentManager(), UserDiscussionFragment.TAG);
        if (user.getConversation() == null) return;
        EntourageInformationFragment entourageInformationFragment = EntourageInformationFragment.newInstance(user.getConversation().getUUID(), FeedItem.ENTOURAGE_CARD, 0);
        entourageInformationFragment.setShowInfoButton(false);
        entourageInformationFragment.show(getFragmentManager(), EntourageInformationFragment.TAG);
    }

    @Optional
    @OnClick(R.id.user_photo_button)
    protected void onPhotoEditClicked() {
        PhotoChooseSourceFragment fragment = new PhotoChooseSourceFragment();
        fragment.show(getParentFragmentManager(), PhotoChooseSourceFragment.TAG);
    }

    @Optional
    @OnClick(R.id.user_about_edit_button)
    protected void onAboutEditClicked() {
        UserEditAboutFragment editAboutFragment = new UserEditAboutFragment();
        editAboutFragment.show(getParentFragmentManager(), UserEditAboutFragment.TAG);
    }

    // ----------------------------------
    // Events Handling
    // ----------------------------------

    @Subscribe
    public void userInfoUpdated(Events.OnUserInfoUpdatedEvent event) {
        if (!isAdded()) {
            return;
        }
        //update the current view
        this.user = EntourageApplication.me(getActivity());
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
        try {
            // Because we are handling this event in the user edit fragment too, we need to make sure that there is no active user edit fragment
            if (getParentFragmentManager().findFragmentByTag(UserEditFragment.TAG) != null) {
                return;
            }
            if (event.getPartner() != null) {
                PartnerFragment.Companion.newInstance(event.getPartner()).show(getParentFragmentManager(), PartnerFragment.TAG);
            } else {
                PartnerFragment.Companion.newInstance(event.getPartnerId()).show(getParentFragmentManager(), PartnerFragment.TAG);
            }
        } catch(IllegalStateException e) {
            Timber.w(e);
        }
    }

}
