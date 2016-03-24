package social.entourage.android.user;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.BackPressable;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.Organization;
import social.entourage.android.api.model.User;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.api.tape.Events.*;

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

    @Bind(R.id.user_profile_scrollview)
    ScrollView scrollView;

    @Bind(R.id.user_profile_edit_button)
    TextView userEditProfile;

    @Bind(R.id.user_photo)
    ImageView userPhoto;

    @Bind(R.id.user_name)
    TextView userName;

    @Bind(R.id.user_role)
    TextView userRole;

    @Bind(R.id.user_member_since)
    TextView userMemberSince;

    @Bind(R.id.user_address)
    TextView userAddress;

    @Bind(R.id.user_identification_email_check)
    ImageView userEmailVerifiedImage;

    @Bind(R.id.user_identification_phone_check)
    ImageView userPhoneVerifiedImage;

    @Bind(R.id.user_tours_count)
    TextView userTourCount;

    @Bind(R.id.user_associations_view)
    RecyclerView userAssociationsView;

    UserOrganizationsAdapter organizationsAdapter;

    @Bind(R.id.user_profile_progressBar)
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
        User authentificatedUser = presenter.getAuthentificatedUser();
        if (requestedUserId == authentificatedUser.getId()) {
            isMyProfile = true;
            user = authentificatedUser;
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
            int tourCount = user.getStats().getTourCount();
            int encountersCount = user.getStats().getEncounterCount();

            userEditProfile.setVisibility(isMyProfile ? View.VISIBLE : View.GONE);

            if (user.getAvatarURL() != null) {
                Picasso.with(getActivity()).load(Uri.parse(user.getAvatarURL()))
                        .transform(new CropCircleTransformation())
                        .into(userPhoto);
            }
            else {
                Picasso.with(getActivity()).load(R.drawable.ic_user_photo)
                        .transform(new CropCircleTransformation())
                        .into(userPhoto);
            }

            userName.setText(isMyProfile ? user.getFirstName() : user.getDisplayName());
            userTourCount.setText(""+tourCount);

            userPhoneVerifiedImage.setImageResource(R.drawable.verified);

            if (organizationsAdapter == null) {
                userAssociationsView.setLayoutManager(new LinearLayoutManager(getActivity()));
                List<Organization> organizationList = new ArrayList<>();
                if (user.getOrganization() != null) {
                    organizationList.add(user.getOrganization());
                }
                organizationsAdapter = new UserOrganizationsAdapter(organizationList);
                userAssociationsView.setAdapter(organizationsAdapter);
            }

            /*
            scrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, 0);
                }
            }, 100);
            */

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

    // ----------------------------------
    // Presenter Callbacks
    // ----------------------------------

    protected void onUserReceived(User user) {
        progressBar.setVisibility(View.GONE);
        if (user == null) {
            displayToast(getString(R.string.user_retrieval_error));
            return;
        }
        this.user = user;
        configureView();
    }

    // ----------------------------------
    // ONCLICK CALLBACKS
    // ----------------------------------

    @OnClick(R.id.user_profile_close_button)
    protected void onCloseButtonClicked() {
        dismissAllowingStateLoss();
    }

    @OnClick(R.id.user_profile_edit_button)
    protected void onEditProfileClicked() {
        //TODO Open the edit profile screen
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

}
