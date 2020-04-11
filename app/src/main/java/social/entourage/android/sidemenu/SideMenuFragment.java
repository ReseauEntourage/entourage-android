package social.entourage.android.sidemenu;


import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import social.entourage.android.BuildConfig;
import social.entourage.android.MainActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.model.Partner;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.view.EntourageSnackbar;
import social.entourage.android.view.PartnerLogoImageView;

/**
 * Side menu fragment
 */
public class SideMenuFragment extends Fragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = SideMenuFragment.class.getSimpleName();

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.drawer_header_user_name)
    TextView userName;

    @BindView(R.id.sidemenu_app_version)
    TextView appVersion;

    @BindView(R.id.sidemenu_app_debug_info)
    TextView appDebugInfo;

    @BindView(R.id.drawer_header_user_photo)
    ImageView userPhoto;

    @BindView(R.id.drawer_header_user_partner_logo)
    PartnerLogoImageView userPartnerLogo;

    @BindView(R.id.action_edit_profile)
    TextView userEditProfileTextView;

    @Nullable
    @BindView(R.id.action_update_info)
    SideMenuItemView updatePrivateCircleInfoItemView;

    @Nullable
    @BindView(R.id.action_charte)
    SideMenuItemView charterItemView;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------


    public SideMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusProvider.INSTANCE.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        BusProvider.INSTANCE.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_side_menu, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialiseView();
        updateUserView();
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void userInfoUpdated(Events.OnUserInfoUpdatedEvent event) {
        updateUserView();
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initialiseView() {
        appVersion.setText(getString(R.string.about_version_format, BuildConfig.VERSION_FULL_NAME ));
        appDebugInfo.setText(getString(R.string.about_debug_info_format,BuildConfig.VERSION_DISPLAY_BRANCH_NAME, FirebaseInstanceId.getInstance().getId()));

        appVersion.setOnLongClickListener(v -> handleLongPress());
        appDebugInfo.setOnLongClickListener(v -> handleLongPress());

        //add listener to user photo and name, that opens the user profile screen
        userPhoto.setOnClickListener(v -> selectMenuAction(R.id.action_user));
        userName.setOnClickListener(v -> selectMenuAction(R.id.action_user));
        //add listener to modify profile text view
        userEditProfileTextView.setOnClickListener(v -> selectMenuAction(R.id.action_edit_profile));

        //add listeners to side menu items
        if (getView() != null) {
            LinearLayout sideMenuItemsLayout = getView().findViewById(R.id.sidemenuitems_layout);
            if (sideMenuItemsLayout != null) {
                int itemsCount = sideMenuItemsLayout.getChildCount();
                for (int j = 0; j < itemsCount; j++) {
                    View child = sideMenuItemsLayout.getChildAt(j);
                    if (child instanceof SideMenuItemView) {
                        child.setOnClickListener(v -> selectMenuAction(v.getId()));
                    }
                }
            }
        }
    }

    private void updateUserView() {
        User user = EntourageApplication.me(getActivity());
        if (user != null) {
            userName.setText(user.getDisplayName());
            String avatarURL = user.getAvatarURL();
            if (avatarURL != null) {
                Picasso.get()
                        .load(Uri.parse(avatarURL))
                        .placeholder(R.drawable.ic_user_photo_small)
                        .transform(new CropCircleTransformation())
                        .into(userPhoto);
            } else {
                userPhoto.setImageResource(R.drawable.ic_user_photo_small);
            }
            // Show partner logo
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
            // Show Update Private Circle item only if the user is member of any
            if (updatePrivateCircleInfoItemView != null) {
                updatePrivateCircleInfoItemView.setVisibility( user.getMemberships(Entourage.TYPE_NEIGHBORHOOD).size() > 0 ? View.VISIBLE : View.GONE);
            }
            // Changed the ethics charter text depending on signed/unsigned
            if (charterItemView != null) {
                charterItemView.setTitle(user.hasSignedEthicsCharter() ? R.string.action_charter_signed : R.string.action_charter_unsigned);
            }
        }
    }

    private void selectMenuAction(int action) {
        if (getActivity() == null || !(getActivity() instanceof MainActivity)) return;
        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.selectItem(action);
    }

    private boolean handleLongPress() {
        selectMenuAction(R.id.sidemenu_app_version);
        EntourageSnackbar.INSTANCE.make(
                getView().findViewById(R.id.sideMenuCoordinatorLayout),
                R.string.debug_info_clipboard,
                Snackbar.LENGTH_SHORT
        ).show();
        return true;
    }

}
