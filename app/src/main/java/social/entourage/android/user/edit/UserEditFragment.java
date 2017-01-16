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
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.Organization;
import social.entourage.android.api.model.User;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.user.UserFragment;
import social.entourage.android.user.UserOrganizationsAdapter;
import social.entourage.android.user.edit.association.UserEditAssociationFragment;
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragment;

public class UserEditFragment extends DialogFragment {

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

    @BindView(R.id.user_email)
    TextView userEmail;

    @BindView(R.id.user_phone)
    TextView userPhone;

    @BindView(R.id.user_address)
    TextView userAddress;

    @BindView(R.id.user_associations_title)
    TextView userAssociationsTitle;

    @BindView(R.id.user_associations_public)
    TextView userAssociationsPublicInfo;

    @BindView(R.id.user_associations_view)
    RecyclerView userAssociationsView;

    @BindView(R.id.user_add_association_separator)
    View userAddAssociationSeparator;

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
            userEmail.setText(editedUser.getEmail());
            userPhone.setText(editedUser.getPhone());
            userAddress.setText("");

            List<Organization> organizationList = new ArrayList<>();
            if (organizationsAdapter == null) {
                userAssociationsView.setLayoutManager(new LinearLayoutManager(getActivity()));
                if (editedUser.getOrganization() != null) {
                    organizationList.add(editedUser.getOrganization());
                }
                organizationsAdapter = new UserOrganizationsAdapter(organizationList);
                userAssociationsView.setAdapter(organizationsAdapter);
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
        UserEditAssociationFragment userEditAssociationFragment = new UserEditAssociationFragment();
        userEditAssociationFragment.show(getFragmentManager(), UserEditAssociationFragment.TAG);
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

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------

    protected void onUserUpdated(User user) {
        if (user == null) {
            displayToast(getString(R.string.user_text_update_ko));
        }
        else {
            displayToast(getString(R.string.user_text_update_ok));
            dismiss();
        }
    }

}
