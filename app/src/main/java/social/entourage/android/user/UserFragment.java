package social.entourage.android.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.Organization;
import social.entourage.android.api.model.Stats;
import social.entourage.android.api.model.User;
import social.entourage.android.api.tape.Events;
import social.entourage.android.authentication.login.LoginActivity;
import social.entourage.android.base.ItemClickSupport;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.user.edit.UserEditFragment;

public class UserFragment extends DialogFragment {

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

    @BindView(R.id.user_photo)
    ImageView userPhoto;

    @BindView(R.id.user_partner_logo)
    ImageView userPartnerLogo;

    @BindView(R.id.user_name)
    TextView userName;

    @BindView(R.id.user_role)
    TextView userRole;

    @BindView(R.id.user_member_since)
    TextView userMemberSince;

    @BindView(R.id.user_address)
    TextView userAddress;

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
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreateView(inflater, container, savedInstanceState);
        if (toReturn == null) {
            toReturn = inflater.inflate(R.layout.fragment_user, container, false);
        }
        ButterKnife.bind(this, toReturn);
        FlurryAgent.logEvent(Constants.EVENT_PROFILE_FROM_MENU);
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        requestedUserId = getArguments().getInt(User.KEY_USER_ID);
        User authenticatedUser = presenter.getAuthenticatedUser();
        if (requestedUserId == authenticatedUser.getId()) {
            isMyProfile = true;
            user = authenticatedUser;
            configureView();
        }
        else {
            progressBar.setVisibility(View.VISIBLE);
            presenter.getUser(requestedUserId);
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
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
    }

    @Override
    public void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
        if (getActivity() != null) {
            Resources res = getResources();
            Stats stats = user.getStats();
            int entourageCount = 0;
            if (stats != null) {
                entourageCount = stats.getEntourageCount();
            }

            userEditProfile.setVisibility(isMyProfile ? View.VISIBLE : View.GONE);

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
            //TODO Show the partner logo, if available

            userName.setText(isMyProfile ? user.getFirstName() : user.getDisplayName());
            userTourCount.setText(""+entourageCount);

            userPhoneVerifiedImage.setImageResource(R.drawable.verified);
            userEmailVerifiedImage.setImageResource(R.drawable.verified);

            if (organizationsAdapter == null) {
                userAssociationsView.setLayoutManager(new LinearLayoutManager(getActivity()));
                List<Organization> organizationList = new ArrayList<>();
                if (user.getOrganization() != null) {
                    organizationList.add(user.getOrganization());
                }
                organizationsAdapter = new UserOrganizationsAdapter(organizationList);
                userAssociationsView.setAdapter(organizationsAdapter);

                ItemClickSupport.addTo(userAssociationsView)
                        .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                            @Override
                            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                                onEditProfileClicked();
                            }
                        });
            }

            boolean isPro = user.isPro();
            userAssociationsTitle.setVisibility( isPro ? View.VISIBLE : View.GONE );
            userAssociationsView.setVisibility( isPro ? View.VISIBLE : View.GONE );
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

    public void deleteAccount() {
        if (presenter != null) {
            if (getActivity() instanceof EntourageActivity) {
                ((EntourageActivity) getActivity()).showProgressDialog(0);
            }
            presenter.deleteAccount();
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
        progressBar.setVisibility(View.GONE);
        if (user == null) {
            displayToast(R.string.user_retrieval_error);
            return;
        }
        this.user = user;
        configureView();
    }

    protected void onDeletedAccount(boolean success) {
        if (getActivity() != null && getActivity() instanceof EntourageActivity) {
            ((EntourageActivity) getActivity()).dismissProgressDialog();
        }
        if (success) {
            //remove the tutorial flag
            SharedPreferences sharedPreferences = getActivity().getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
            HashSet<String> loggedNumbers = (HashSet) sharedPreferences.getStringSet(LoginActivity.KEY_TUTORIAL_DONE, new HashSet<String>());
            loggedNumbers.remove(this.user.getPhone());
            sharedPreferences.edit().putStringSet(LoginActivity.KEY_TUTORIAL_DONE, loggedNumbers).commit();
            //go back to login screen
            if (getActivity() instanceof DrawerActivity) {
                ((DrawerActivity) getActivity()).selectItem(R.id.action_logout);
            }
        }
        else {
            displayToast(R.string.user_delete_account_failure);
        }
    }

    protected void onUserUpdated(User user) {
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

    @OnClick({R.id.user_profile_edit_button, R.id.user_photo, R.id.user_name, R.id.user_identification_email_layout, R.id.user_identification_phone_layout, R.id.user_number_of_entourages_layout})
    protected void onEditProfileClicked() {
        // Allow editing only of the logged user
        if (!isMyProfile) return;
        // Show the edit profile screen
        UserEditFragment fragment = new UserEditFragment();
        fragment.show(getFragmentManager(), UserEditFragment.TAG);
    }

    /*
    @OnClick(R.id.user_button_confirm_changes)
    void confirmChanges() {
        String emailEdit = userEditEmail.getText().toString();
        String codeEdit = userEditCode.getText().toString();
        String confirmationEdit = userEditConfirmation.getText().toString();

        String email = null;
        String code = null;

        if (!emailEdit.equals("")) {
            email = emailEdit;
        }

        if ((!codeEdit.equals("") && codeEdit.length() == 6) &&
                (!confirmationEdit.equals("") && confirmationEdit.length() == 6)) {
            if (codeEdit.equals(confirmationEdit)) {
                code = codeEdit;
            } else {
                displayToast("Erreur de confirmation du code");
            }
        }

        if (email != null || code != null) {
            presenter.updateUser(email, code);
        }
    }
    */

    // ----------------------------------
    // Events Handling
    // ----------------------------------

    @Subscribe
    public void userInfoUpdated(Events.OnUserInfoUpdatedEvent event) {
        if (!isAdded()) {
            return;
        }
        User user = EntourageApplication.me(getActivity());
        //update the current view
        this.user = user;
        configureView();
    }

}
