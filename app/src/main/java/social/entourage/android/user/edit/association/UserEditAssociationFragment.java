package social.entourage.android.user.edit.association;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.api.model.User;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.R;
import social.entourage.android.user.UserOrganizationsAdapter;
import social.entourage.android.user.edit.UserEditFragment;

/**
 *
 * Edit the association that an user supports
 *
 */
public class UserEditAssociationFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "user_edit_association_fragment";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.user_edit_asso_listview)
    ListView associationsListView;

    private UserEditAssociationAdapter adapter;

    private User user;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public UserEditAssociationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_edit_association, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureView();
    }

    private void configureView() {
        UserEditFragment userEditFragment = (UserEditFragment) getFragmentManager().findFragmentByTag(UserEditFragment.TAG);
        if (userEditFragment == null) {
            return;
        }
        user = userEditFragment.getEditedUser();

        if (adapter == null) {
            adapter = new UserEditAssociationAdapter();
            associationsListView.setAdapter(adapter);
        }
    }

    // ----------------------------------
    // Buttons Handling
    // ----------------------------------

    @OnClick(R.id.user_edit_asso_close_button)
    protected void onCloseButtonClicked() {
        dismiss();
    }

    @OnClick(R.id.user_edit_asso_save_button)
    protected void onSaveButtonClicked() {

    }

}
