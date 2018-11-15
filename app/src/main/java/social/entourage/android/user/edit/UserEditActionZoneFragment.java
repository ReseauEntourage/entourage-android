package social.entourage.android.user.edit;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.EntourageApplication;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.AuthenticationController;
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

    @Nullable
    @BindView(R.id.action_zone_ignore_button)
    Button ignoreButton;

    protected User.Address userAddress;

    private boolean saving = false;

    SupportPlaceAutocompleteFragment autocompleteFragment = null;

    private List<FragmentListener> fragmentListeners = new ArrayList<>();

    private boolean isFromLogin = false;

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

        if (!isFromLogin && ignoreButton != null) {
            //Hide the ignore button if the fragment is not shown from the login screen
            ignoreButton.setVisibility(View.GONE);
        }

        initializeGooglePlaces();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        for (FragmentListener fragmentListener: fragmentListeners) {
            fragmentListener.onUserEditActionZoneFragmentDismiss();
        }
    }

    @Override
    protected int getSlideStyle() {
        return isFromLogin ? 0 : super.getSlideStyle();
    }

    public void setFragmentListener(final FragmentListener fragmentListener) {
        if (fragmentListener == null) return;
        fragmentListeners.add(fragmentListener);
    }

    public void addFragmentListener(final FragmentListener fragmentListener) {
        if (fragmentListener == null) return;
        fragmentListeners.add(fragmentListener);
    }

    public void removeFragmentListener(final FragmentListener fragmentListener) {
        fragmentListeners.remove(fragmentListener);
    }

    public void setFromLogin(final boolean fromLogin) {
        isFromLogin = fromLogin;
    }

    // ----------------------------------
    // Buttons Handling
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseButtonClicked() {
        dismiss();
        for (FragmentListener fragmentListener: fragmentListeners) {
            fragmentListener.onUserEditActionZoneFragmentDismiss();
        }
    }

    @OnClick(R.id.action_zone_go_button)
    protected void onSaveButtonClicked() {
        if (saving) return;
        saveAddress();
    }

    @Optional
    @OnClick(R.id.action_zone_ignore_button)
    protected void onIgnoreButtonClicked() {
        for (FragmentListener fragmentListener: fragmentListeners) {
            fragmentListener.onUserEditActionZoneFragmentIgnore();
        }
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private void initializeGooglePlaces() {

        if (autocompleteFragment == null) {
            autocompleteFragment = new SupportPlaceAutocompleteFragment();
        }
        if (userAddress != null) {
            Bundle args = new Bundle();
            args.putString(KEY_USER_ADDRESS, userAddress.getDisplayAddress());
            autocompleteFragment.setArguments(args);
        }
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.place_autocomplete_fragment, autocompleteFragment).commit();

        autocompleteFragment.setBoundsBias(new LatLngBounds(new LatLng(42, -5), new LatLng(51, 9)));

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

    private void saveAddress() {
        if (userAddress == null) {
            Toast.makeText(getActivity(), R.string.user_action_zone_no_address_error, Toast.LENGTH_SHORT).show();
            return;
        }
        saving = true;
        UserRequest userRequest = EntourageApplication.get().getEntourageComponent().getUserRequest();
        Call<User.AddressWrapper> call = userRequest.updateAddress(new User.AddressWrapper(userAddress));
        call.enqueue(new Callback<User.AddressWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<User.AddressWrapper> call, @NonNull final Response<User.AddressWrapper> response) {
                if (response.isSuccessful()) {
                    AuthenticationController authenticationController = EntourageApplication.get().getEntourageComponent().getAuthenticationController();
                    User me = authenticationController.getUser();
                    if (me != null && response.body() != null) {
                        me.setAddress(response.body().getAddress());
                        authenticationController.saveUser(me);
                    }
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), R.string.user_action_zone_send_ok, Toast.LENGTH_SHORT).show();
                    }
                    for (FragmentListener fragmentListener: fragmentListeners) {
                        fragmentListener.onUserEditActionZoneFragmentAddressSaved();
                    }
                } else {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), R.string.user_action_zone_send_failed, Toast.LENGTH_SHORT).show();
                    }
                }
                saving = false;
            }

            @Override
            public void onFailure(@NonNull final Call<User.AddressWrapper> call, @NonNull final Throwable t) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.user_action_zone_send_failed, Toast.LENGTH_SHORT).show();
                }
                saving = false;
            }
        });
    }

    // ----------------------------------
    // Inner classes
    // ----------------------------------

    public static class SupportPlaceAutocompleteFragment extends com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment {

        @Override
        public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            setHint(getString(R.string.user_action_zone_hint));
            if (getArguments() != null) {
                String address = getArguments().getString(KEY_USER_ADDRESS, "");
                setText(address);
            }
            if (getView() != null) {
                EditText autocompleteEditText = getView().findViewById(com.google.android.gms.location.places.R.id.place_autocomplete_search_input);
                if (autocompleteEditText != null && getContext() != null) {
                    autocompleteEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                }
            }
        }

        @Override
        public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
            super.onActivityResult(requestCode, resultCode, intent);
            if (requestCode == 30421) {
                if (resultCode == -1) {
                    if (this.getActivity() == null) return;
                    Place place = PlaceAutocomplete.getPlace(this.getActivity(), intent);
                    if (place == null || place.getAddress() == null) return;

                    String address = place.getAddress().toString();
                    int lastCommaIndex = address.lastIndexOf(',');
                    if (lastCommaIndex > 0) {
                        //remove the last part, which is the country
                        address = address.substring(0, lastCommaIndex);
                    }

                    this.setText(address);
                }
            }
        }
    }

    public interface FragmentListener {

        void onUserEditActionZoneFragmentDismiss();
        void onUserEditActionZoneFragmentAddressSaved();
        void onUserEditActionZoneFragmentIgnore();

    }

}
