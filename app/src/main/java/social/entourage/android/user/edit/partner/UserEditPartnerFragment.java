package social.entourage.android.user.edit.partner;


import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.EntourageApplication;
import social.entourage.android.api.PartnerRequest;
import social.entourage.android.api.model.Partner;
import social.entourage.android.api.model.User;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.R;
import social.entourage.android.user.edit.UserEditFragment;

/**
 *
 * Edit the association that an user supports
 *
 */
public class UserEditPartnerFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "user_edit_association_fragment";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.user_edit_partner_search)
    EditText searchEditText;

    @BindView(R.id.user_edit_partner_listview)
    ListView partnersListView;

    private UserEditPartnerAdapter adapter;

    private User user;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public UserEditPartnerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_edit_partner, container, false);
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

        // Configure the partners list
        if (adapter == null) {
            adapter = new UserEditPartnerAdapter();
            partnersListView.setAdapter(adapter);
        }

        // Initialize the search field
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                String mSearchString = "";
                boolean hideKeyboard = false;
                if (event == null) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        mSearchString = v.getText().toString().trim();
                        hideKeyboard = true;
                    }
                }
                else if (event.getKeyCode() == KeyEvent.ACTION_DOWN) {
                    mSearchString = v.getText().toString().trim();
                    hideKeyboard = true;
                }

                if (hideKeyboard) {
                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(),
                            InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    return true;
                }

                return false;
            }
        });

        // retrieve the list of partners
        getAllPartners();
    }

    // ----------------------------------
    // Buttons Handling
    // ----------------------------------

    @OnClick(R.id.user_edit_partner_close_button)
    protected void onCloseButtonClicked() {
        dismiss();
    }

    @OnClick(R.id.user_edit_partner_save_button)
    protected void onSaveButtonClicked() {

    }

    // ----------------------------------
    // Network
    // ----------------------------------

    private void getAllPartners() {

        final PartnerRequest request = EntourageApplication.get(getContext()).getEntourageComponent().getPartnerRequest();
        request.getAllPartners().enqueue(new Callback<Partner.PartnersWrapper>() {
            @Override
            public void onResponse(final Call<Partner.PartnersWrapper> call, final Response<Partner.PartnersWrapper> response) {
                if (response.isSuccessful()) {
                    List<Partner> partnerList = response.body().getPartners();
                    adapter.setPartnerList(partnerList);
                    for (int i = 0; i < partnerList.size(); i++) {
                        Partner partner = partnerList.get(i);
                        if (partner.isDefault()) {
                            partnersListView.setItemChecked(i, true);
                            adapter.selectedPartnerPosition = i;
                        }
                    }
                }
            }

            @Override
            public void onFailure(final Call<Partner.PartnersWrapper> call, final Throwable t) {

            }
        });

    }

}
