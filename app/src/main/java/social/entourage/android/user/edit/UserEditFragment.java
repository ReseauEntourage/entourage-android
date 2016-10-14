package social.entourage.android.user.edit;


import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.model.Organization;
import social.entourage.android.api.model.User;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.user.UserFragment;
import social.entourage.android.user.UserOrganizationsAdapter;
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragment;

public class UserEditFragment extends DialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "user_edit_fragment";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Bind(R.id.user_photo)
    ImageView userPhoto;

    @Bind(R.id.user_edit_firstname)
    TextView userFirstname;

    @Bind(R.id.user_email)
    TextView userEmail;

    @Bind(R.id.user_phone)
    TextView userPhone;

    @Bind(R.id.user_address)
    TextView userAddress;

    @Bind(R.id.user_associations_title)
    TextView userAssociationsTitle;

    @Bind(R.id.user_associations_view)
    RecyclerView userAssociationsView;

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
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.fragment_user_edit, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureView();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

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
                        .transform(new CropCircleTransformation())
                        .into(userPhoto);
            }
            else {
                Picasso.with(getActivity()).load(R.drawable.ic_user_photo)
                        .transform(new CropCircleTransformation())
                        .into(userPhoto);
            }

            userFirstname.setText(editedUser.getFirstName());
            userEmail.setText(editedUser.getEmail());
            userPhone.setText(editedUser.getPhone());
            userAddress.setText("");

            if (organizationsAdapter == null) {
                userAssociationsView.setLayoutManager(new LinearLayoutManager(getActivity()));
                List<Organization> organizationList = new ArrayList<>();
                if (editedUser.getOrganization() != null) {
                    organizationList.add(editedUser.getOrganization());
                }
                organizationsAdapter = new UserOrganizationsAdapter(organizationList);
                userAssociationsView.setAdapter(organizationsAdapter);
            }

            boolean isPro = editedUser.isPro();
            userAssociationsTitle.setVisibility( isPro ? View.VISIBLE : View.GONE );
            userAssociationsView.setVisibility( isPro ? View.VISIBLE : View.GONE );

        }
    }

    public User getEditedUser() {
        return editedUser;
    }

    // ----------------------------------
    // Buttons Handling
    // ----------------------------------

    @OnClick(R.id.user_edit_close_button)
    protected void onCloseButtonClicked() {
        dismiss();
    }

    @OnClick(R.id.user_name_layout)
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

    @OnClick(R.id.user_delete_account_button)
    protected void onDeleteAccountClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.user_delete_account_dialog)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        UserFragment fragment = (UserFragment)getFragmentManager().findFragmentByTag(UserFragment.TAG);
                        if (fragment != null) {
                            fragment.deleteAccount();
                        }
                    }
                })
                .setNegativeButton(R.string.no, null);
        builder.show();
    }

    @OnClick(R.id.user_save_button)
    protected void onSaveButtonClicked() {
        UserFragment fragment = (UserFragment)getFragmentManager().findFragmentByTag(UserFragment.TAG);
        if (fragment != null) {
            fragment.saveAccount(editedUser);
        }
    }

    @OnClick(R.id.user_photo_button)
    protected void onPhotoClicked() {
        PhotoChooseSourceFragment fragment = new PhotoChooseSourceFragment();
        fragment.show(getFragmentManager(), PhotoChooseSourceFragment.TAG);
    }

    private void showEditProfile(int editType) {
        UserEditProfileFragment fragment = UserEditProfileFragment.newInstance(editType);
        fragment.show(getFragmentManager(), UserEditProfileFragment.TAG);
    }

    // ----------------------------------
    // Events Handling
    // ----------------------------------

    @Subscribe
    public void userInfoUpdated(Events.OnUserInfoUpdatedEvent event) {
        User user = EntourageApplication.me(getActivity());
        editedUser.setAvatarURL(user.getAvatarURL());

        if (editedUser.getAvatarURL() != null) {
            Picasso.with(getActivity()).load(Uri.parse(editedUser.getAvatarURL()))
                    .transform(new CropCircleTransformation())
                    .into(userPhoto);
        }
        else {
            Picasso.with(getActivity()).load(R.drawable.ic_user_photo)
                    .transform(new CropCircleTransformation())
                    .into(userPhoto);
        }
    }

}
