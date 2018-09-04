package social.entourage.android.sidemenu;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.model.Partner;
import social.entourage.android.api.model.User;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.CropCircleTransformation;
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

    @BindView(R.id.drawer_header_user_photo)
    ImageView userPhoto;

    @BindView(R.id.drawer_header_user_partner_logo)
    PartnerLogoImageView userPartnerLogo;

    @BindView(R.id.drawer_header_edit_profile)
    TextView userEditProfileTextView;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------


    public SideMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
        //add listener to user photo and name, that opens the user profile screen
        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                selectMenuAction(R.id.action_user);
            }
        });
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                selectMenuAction(R.id.action_user);
            }
        });
        //add listener to modify profile text view
        userEditProfileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                selectMenuAction(R.id.action_edit_user);
            }
        });

        //add listeners to side menu items
        LinearLayout sideMenuItemsLayout = getView().findViewById(R.id.sidemenuitems_layout);
        if (sideMenuItemsLayout != null) {
            int itemsCount = sideMenuItemsLayout.getChildCount();
            for (int j = 0; j < itemsCount; j++) {
                View child = sideMenuItemsLayout.getChildAt(j);
                if (child instanceof SideMenuItemView) {
                    child.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectMenuAction(v.getId());
                        }
                    });
                }
            }
        }
    }

    private void updateUserView() {
        User user = EntourageApplication.me();
        if (user != null) {
            userName.setText(user.getDisplayName());
            String avatarURL = user.getAvatarURL();
            if (avatarURL != null) {
                Picasso.with(getContext())
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
                Picasso.with(getContext())
                        .load(Uri.parse(partnerURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .transform(new CropCircleTransformation())
                        .into(userPartnerLogo);
            } else {
                userPartnerLogo.setImageDrawable(null);
            }
        }
    }

    private void selectMenuAction(int action) {
        if (getActivity() == null || !(getActivity() instanceof DrawerActivity)) return;
        DrawerActivity drawerActivity = (DrawerActivity)getActivity();
        drawerActivity.selectItem(action);
    }

}
