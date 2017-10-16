package social.entourage.android.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.BaseOrganization;
import social.entourage.android.api.model.Partner;
import social.entourage.android.api.model.Stats;
import social.entourage.android.api.model.User;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.base.ItemClickSupport;
import social.entourage.android.partner.PartnerFragment;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.user.edit.UserEditFragment;
import social.entourage.android.view.PartnerLogoImageView;

public class UserFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "fragment_user";

    private final String TERMS_AND_CONDITIONS_URL= "http://www.entourage.social/cgu";

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

    @BindView(R.id.user_partner_logo)
    PartnerLogoImageView userPartnerLogo;

    @BindView(R.id.user_name)
    TextView userName;

    @BindView(R.id.user_role)
    TextView userRole;

    @BindView(R.id.user_member_since)
    TextView userMemberSince;

    @BindView(R.id.user_address)
    TextView userAddress;

    @BindView(R.id.user_profile_about_layout)
    View userAboutLayout;

    @BindView(R.id.user_profile_about)
    TextView userAboutTextView;

    @BindView(R.id.user_identification_email_check)
    ImageView userEmailVerifiedImage;

    @BindView(R.id.user_identification_phone_check)
    ImageView userPhoneVerifiedImage;

    @BindView(R.id.user_tours_count)
    TextView userTourCount;

    @BindView(R.id.user_associations_title)
    TextView userAssociationsTitle;

    @BindView(R.id.user_associations_view)
    RecyclerView userAssociationsView;

    UserOrganizationsAdapter organizationsAdapter;

    @BindView(R.id.user_profile_progressBar)
    ProgressBar progressBar;

    private int requestedUserId;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (toReturn == null) {
            toReturn = inflater.inflate(R.layout.fragment_user, container, false);
        }
        ButterKnife.bind(this, toReturn);
        EntourageEvents.logEvent(Constants.EVENT_PROFILE_FROM_MENU);
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        requestedUserId = getArguments().getInt(User.KEY_USER_ID);
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
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setTitle(R.string.activity_display_user_title);
        }
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
        EntourageEvents.logEvent(isMyProfile ? Constants.EVENT_SCREEN_09_1_ME : Constants.EVENT_SCREEN_09_1_OTHER);
        if (getActivity() != null && !getActivity().isFinishing()) {
            Stats stats = user.getStats();
            int entourageCount = 0;
            if (stats != null) {
                entourageCount = stats.getEntourageCount();
            }

            userEditProfile.setVisibility(isMyProfile ? View.VISIBLE : View.GONE);
            userReportButton.setVisibility(isMyProfile ? View.GONE : View.VISIBLE);

            if (user.getAvatarURL() != null) {
                Picasso.with(getActivity()).load(Uri.parse(user.getAvatarURL()))
                        .placeholder(R.drawable.ic_user_photo)
                        .transform(new CropCircleTransformation())
                        .into(userPhoto);
            }
            else {
                Picasso.with(getActivity()).load(R.drawable.ic_user_photo)
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
                Picasso.with(getActivity())
                        .load(Uri.parse(partnerURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .transform(new CropCircleTransformation())
                        .into(userPartnerLogo);
            } else {
                userPartnerLogo.setImageDrawable(null);
            }

            userName.setText(isMyProfile ? user.getFirstName() : user.getDisplayName());
            userTourCount.setText(getString(R.string.user_entourage_count_format, entourageCount));

            String userAbout = user.getAbout();
            userAboutLayout.setVisibility( (userAbout == null || userAbout.trim().length() == 0) ? View.GONE : View.VISIBLE);
            userAboutTextView.setText(userAbout);

            boolean userEmailVerified = user.getEmail() != null;
            userPhoneVerifiedImage.setImageResource(R.drawable.verified);
            userEmailVerifiedImage.setImageResource(userEmailVerified ? R.drawable.verified : R.drawable.not_verified);

            List<BaseOrganization> organizationList = new ArrayList<>();
            if (user.getPartner() != null) {
                organizationList.add(user.getPartner());
            }
            if (user.getOrganization() != null) {
                organizationList.add(user.getOrganization());
            }
            if (organizationsAdapter == null) {
                userAssociationsView.setLayoutManager(new LinearLayoutManager(getActivity()));

                organizationsAdapter = new UserOrganizationsAdapter(organizationList);
                userAssociationsView.setAdapter(organizationsAdapter);

                ItemClickSupport.addTo(userAssociationsView)
                        .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                            @Override
                            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                                onEditProfileClicked();
                            }
                        });
            } else {
                organizationsAdapter.setOrganizationList(organizationList);
            }

            //boolean isPro = user.isPro();
            userAssociationsTitle.setVisibility( organizationList.size() > 0 ? View.VISIBLE : View.GONE );
            userAssociationsView.setVisibility( organizationList.size() > 0 ? View.VISIBLE : View.GONE );
        }
    }

    private void showUserEditFragment() {
        // Allow editing only of the logged user
        if (!isMyProfile) return;
        // Show the edit profile screen
        UserEditFragment fragment = new UserEditFragment();
        fragment.show(getFragmentManager(), UserEditFragment.TAG);
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

    @OnClick(R.id.user_profile_close_button)
    protected void onCloseButtonClicked() {
        dismiss();
    }

    @OnClick(R.id.user_profile_edit_button)
    protected void onEditButtonClicked() {
        EntourageEvents.logEvent(Constants.EVENT_USER_EDIT_PROFILE);
        showUserEditFragment();
    }

    @OnClick(R.id.user_photo)
    protected void onUserPhotoClicked() {
        EntourageEvents.logEvent(Constants.EVENT_USER_EDIT_PHOTO);
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
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        // Set the email to
        String[] addresses = {Constants.EMAIL_CONTACT};
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        // Set the subject
        String emailSubject = getString(R.string.user_report_email_subject, user.getDisplayName());
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Start the intent
            startActivity(intent);
        } else {
            // No Email clients
            Toast.makeText(getContext(), R.string.error_no_email, Toast.LENGTH_SHORT).show();
        }
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
        PartnerFragment partnerFragment = PartnerFragment.newInstance(event.getPartnerId());
        partnerFragment.show(getFragmentManager(), PartnerFragment.TAG);
    }

}
