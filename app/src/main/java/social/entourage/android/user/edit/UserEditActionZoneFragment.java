package social.entourage.android.user.edit;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.api.model.User;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.R;

/**
 * A {@link EntourageDialogFragment} subclass, used to edit the action zone for an user
 * Use the {@link UserEditActionZoneFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserEditActionZoneFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = UserEditActionZoneFragment.class.getSimpleName();

    private static final String KEY_USER_ADDRESS = "social.entourage.android.KEY_USER_ADDRESS";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.action_zone_address)
    TextView addressTextView;

    private User.Address userAddress;

    PlaceAutocompleteFragment autocompleteFragment = null;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public UserEditActionZoneFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userAddress User Address.
     * @return A new instance of fragment UserEditActionZoneFragment.
     */
    public static UserEditActionZoneFragment newInstance(User.Address userAddress) {
        UserEditActionZoneFragment fragment = new UserEditActionZoneFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_USER_ADDRESS, userAddress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userAddress = (User.Address)getArguments().getSerializable(KEY_USER_ADDRESS);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_edit_action_zone, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (userAddress != null) {
            addressTextView.setText(userAddress.getDisplayAddress());
        }

        initializeGooglePlaces();
    }

    // ----------------------------------
    // Buttons Handling
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseButtonClicked() {
        dismiss();
    }

    @OnClick(R.id.action_zone_go_button)
    protected void onSaveButtonClicked() {
        Toast.makeText(getContext(), R.string.error_not_yet_implemented, Toast.LENGTH_SHORT).show();
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private void initializeGooglePlaces() {
        if (getActivity() == null) return;
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        if (autocompleteFragment == null) return;

        autocompleteFragment.setBoundsBias(new LatLngBounds(new LatLng(42, -5), new LatLng(51, 9)));

        autocompleteFragment.setHint(getString(R.string.user_action_zone_hint));
        if (userAddress != null) {
            autocompleteFragment.setText(userAddress.getDisplayAddress());
        }
        if (autocompleteFragment.getView() != null) {
            EditText autocompleteEditText = autocompleteFragment.getView().findViewById(com.google.android.gms.location.places.R.id.place_autocomplete_search_input);
            if (autocompleteEditText != null && getContext() != null) {
                autocompleteEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            }
        }

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (place != null) {
                    userAddress = new User.Address(place.getId());
                }
            }

            @Override
            public void onError(Status status) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.entourage_location_address_not_found, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
