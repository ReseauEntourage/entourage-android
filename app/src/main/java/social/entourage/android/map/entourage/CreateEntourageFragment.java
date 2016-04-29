package social.entourage.android.map.entourage;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;

/**
 *
 */
public class CreateEntourageFragment extends DialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.createentourage";

    private static final String KEY_ENTOURAGE_TYPE = "social.entourage.android.KEY_ENTOURAGE_TYPE";
    private static final String KEY_ENTOURAGE_LOCATION = "social.entourage.android.KEY_ENTOURAGE_LOCATION";

    private static final int TITLE_MAX_CHAR_COUNT = 150;

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private OnFragmentInteractionListener mListener;

    @Inject
    CreateEntouragePresenter presenter;

    @Bind(R.id.create_entourage_type)
    TextView typeTextView;

    @Bind(R.id.create_entourage_position)
    TextView positionTextView;

    @Bind(R.id.create_entourage_title)
    EditText titleEditText;

    @Bind(R.id.create_entourage_title_hint)
    TextView titleHintTextView;

    @Bind(R.id.create_entourage_title_char_count)
    TextView titleCharCountTextView;

    @Bind(R.id.create_entourage_description)
    EditText descriptionEditText;

    @Bind(R.id.create_entourage_description_hint)
    TextView descriptionHintTextView;

    private String entourageType;
    private LatLng location;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public CreateEntourageFragment() {
        // Required empty public constructor
    }

    public static CreateEntourageFragment newInstance(String entourageType, LatLng location) {
        CreateEntourageFragment fragment = new CreateEntourageFragment();
        Bundle args = new Bundle();
        args.putString(KEY_ENTOURAGE_TYPE, entourageType);
        args.putParcelable(KEY_ENTOURAGE_LOCATION, location);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_entourage, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());

        initializeView();
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerCreateEntourageComponent.builder()
                .entourageComponent(entourageComponent)
                .createEntourageModule(new CreateEntourageModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.background)));
    }

    // ----------------------------------
    // Interactions handling
    // ----------------------------------

    @OnClick(R.id.create_entourage_close)
    protected void onCloseClicked() {
        dismiss();
    }

    @OnClick(R.id.create_entourage_validate_button)
    protected void onValidateClicked() {
        if (isValid()) {
            if (presenter != null) {
                TourPoint entourageLocation = new TourPoint(0, 0);
                if (location != null) {
                    entourageLocation.setLatitude(location.latitude);
                    entourageLocation.setLongitude(location.longitude);
                }
                presenter.createEntourage(
                        entourageType,
                        titleEditText.getText().toString(),
                        descriptionEditText.getText().toString(),
                        entourageLocation);
            } else {
                Toast.makeText(getActivity(), R.string.entourage_create_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.create_entourage_position_layout)
    protected void onPositionClicked() {

    }

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------

    protected void onEntourageCreated(Entourage entourage) {
        if (entourage == null) {
            Toast.makeText(getActivity(), R.string.entourage_create_error, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), R.string.entourage_create_ok, Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private void initializeView() {
        initializeTypeTextView();
        initializeLocation();
        initializeTitleEditText();
        initializeDescriptionEditText();
    }

    private void initializeTypeTextView() {
        Bundle args = getArguments();
        if (args != null) {
            entourageType = args.getString(KEY_ENTOURAGE_TYPE, Entourage.TYPE_CONTRIBUTION);
            if (Entourage.TYPE_CONTRIBUTION.equals(entourageType)) {
                typeTextView.setText(R.string.entourage_create_contribution_title);
            } else {
                typeTextView.setText(R.string.entourage_create_demand_title);
            }
        }
    }

    private void initializeLocation() {
        Bundle args = getArguments();
        if (args != null) {
            location = args.getParcelable(KEY_ENTOURAGE_LOCATION);
            if (location != null) {
                GeocoderTask geocoderTask = new GeocoderTask();
                geocoderTask.execute(location);
            }
        }
    }

    private void initializeTitleEditText() {
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() == 0) {
                    titleHintTextView.setVisibility(View.INVISIBLE);
                } else {
                    titleHintTextView.setVisibility(View.VISIBLE);
                }
                String charCountString = getContext().getString(R.string.entourage_create_title_char_count_format, s.length(), TITLE_MAX_CHAR_COUNT);
                titleCharCountTextView.setText(charCountString);
            }
        });

        String charCountString = getContext().getString(R.string.entourage_create_title_char_count_format, titleEditText.length(), TITLE_MAX_CHAR_COUNT);
        titleCharCountTextView.setText(charCountString);
    }

    private void initializeDescriptionEditText() {
        descriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() == 0) {
                    descriptionHintTextView.setVisibility(View.INVISIBLE);
                } else {
                    descriptionHintTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private boolean isValid() {
        String title = titleEditText.getText().toString().trim();
        if (title.length() == 0) {
            Toast.makeText(getActivity(), R.string.entourage_create_error_title_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (title.length() > TITLE_MAX_CHAR_COUNT) {
            Toast.makeText(getActivity(), R.string.entourage_create_error_title_too_long, Toast.LENGTH_SHORT).show();
            return false;
        }
        String description = descriptionEditText.getText().toString().trim();
        if (description.length() == 0) {
            Toast.makeText(getActivity(), R.string.entourage_create_error_description_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private class GeocoderTask extends AsyncTask<LatLng, Void, String> {

        @Override
        protected String doInBackground(final LatLng... params) {
            try {
                Geocoder geoCoder = new Geocoder(getActivity(), Locale.getDefault());
                LatLng location = params[0];
                List<Address> addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1);
                String addressLine = "";
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    if (address.getMaxAddressLineIndex() >= 0) {
                        addressLine = addresses.get(0).getAddressLine(0);
                    }
                }
                return addressLine;
            }
            catch (IOException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(final String address) {
            CreateEntourageFragment.this.positionTextView.setText(address);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

    }
}
