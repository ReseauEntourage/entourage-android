package social.entourage.android.map.entourage;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.TourPoint;

/**
 *
 */
public class CreateEntourageFragment extends DialogFragment implements EntourageLocationFragment.OnFragmentInteractionListener {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.createentourage";

    protected static final String KEY_ENTOURAGE_TYPE = "social.entourage.android.KEY_ENTOURAGE_TYPE";
    private static final String KEY_ENTOURAGE_LOCATION = "social.entourage.android.KEY_ENTOURAGE_LOCATION";

    private static final int TITLE_MAX_CHAR_COUNT = 150;

    private static final int VOICE_RECOGNITION_TITLE_CODE = 1;
    private static final int VOICE_RECOGNITION_DESCRIPTION_CODE = 2;

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private OnFragmentInteractionListener mListener;

    @Inject
    CreateEntouragePresenter presenter;

    @BindView(R.id.create_entourage_type)
    TextView typeTextView;

    @BindView(R.id.create_entourage_position)
    TextView positionTextView;

    @BindView(R.id.create_entourage_title)
    EditText titleEditText;

    @BindView(R.id.create_entourage_title_hint)
    TextView titleHintTextView;

    @BindView(R.id.create_entourage_title_char_count)
    TextView titleCharCountTextView;

    @BindView(R.id.create_entourage_description)
    EditText descriptionEditText;

    @BindView(R.id.create_entourage_description_hint)
    TextView descriptionHintTextView;

    private String entourageType;
    private LatLng location;

    private boolean isSaving = false;

    private Entourage editedEntourage;

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

    public static CreateEntourageFragment newInstance(Entourage entourage) {
        CreateEntourageFragment fragment = new CreateEntourageFragment();
        Bundle args = new Bundle();
        args.putSerializable(FeedItem.KEY_FEEDITEM, entourage);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_entourage_create, container, false);
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

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == VOICE_RECOGNITION_TITLE_CODE) {
                List<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (!textMatchList.isEmpty()) {
                    if (titleEditText.getText().toString().equals("")) {
                        titleEditText.setText(textMatchList.get(0));
                    } else {
                        titleEditText.setText(titleEditText.getText() + " " + textMatchList.get(0));
                    }
                    titleEditText.setSelection(titleEditText.getText().length());
                }
            }
            else if (requestCode == VOICE_RECOGNITION_DESCRIPTION_CODE) {
                List<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (!textMatchList.isEmpty()) {
                    if (descriptionEditText.getText().toString().equals("")) {
                        descriptionEditText.setText(textMatchList.get(0));
                    } else {
                        descriptionEditText.setText(descriptionEditText.getText() + " " + textMatchList.get(0));
                    }
                    descriptionEditText.setSelection(descriptionEditText.getText().length());
                }
            }
        }

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
        if (isSaving) return;
        if (isValid()) {
            if (presenter != null) {
                isSaving = true;
                TourPoint entourageLocation = new TourPoint(0, 0);
                if (location != null) {
                    entourageLocation.setLatitude(location.latitude);
                    entourageLocation.setLongitude(location.longitude);
                }
                if (editedEntourage != null) {
                    editedEntourage.setTitle(titleEditText.getText().toString());
                    editedEntourage.setDescription(descriptionEditText.getText().toString());
                    editedEntourage.setLocation(entourageLocation);
                    presenter.editEntourage(editedEntourage);
                } else {
                    presenter.createEntourage(
                            entourageType,
                            titleEditText.getText().toString(),
                            descriptionEditText.getText().toString(),
                            entourageLocation);
                }
            } else {
                Toast.makeText(getActivity(), R.string.entourage_create_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.create_entourage_position_layout)
    protected void onPositionClicked() {
        FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_CREATE_CHANGE_LOCATION);
        EntourageLocationFragment fragment = EntourageLocationFragment.newInstance(location, positionTextView.getText().toString(), this);
        fragment.show(getFragmentManager(), EntourageLocationFragment.TAG);
    }

    @OnClick(R.id.create_entourage_title_mic)
    protected void onTitleMicClick() {
        // Try to start SPEECH TO TEXT
        if (!startRecording(VOICE_RECOGNITION_TITLE_CODE)) {
            // Failed, show the keyboard
            showKeyboard(titleEditText);
        }
    }

    @OnClick(R.id.create_entourage_description_mic)
    protected void onDescriptionMicClick() {
        // Try to start SPEECH TO TEXT
        if (!startRecording(VOICE_RECOGNITION_DESCRIPTION_CODE)) {
            // Failed, show the keyboard
            showKeyboard(descriptionEditText);
        }
    }

    // ----------------------------------
    // Microphone handling
    // ----------------------------------

    private boolean startRecording(int callbackCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.encounter_leave_voice_message));
        try {
            FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_SPEECH);
            startActivityForResult(intent, callbackCode);
        } catch (ActivityNotFoundException e) {
            return false;
        }
        return true;
    }

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------

    protected void onEntourageCreated(Entourage entourage) {
        isSaving = false;
        if (getActivity() != null) {
            if (entourage == null) {
                Toast.makeText(getActivity(), R.string.entourage_create_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), R.string.entourage_create_ok, Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }
    }

    protected void onEntourageEdited(Entourage entourage) {
        isSaving = false;
        if (getActivity() != null) {
            if (entourage == null) {
                Toast.makeText(getActivity(), R.string.entourage_save_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), R.string.entourage_save_ok, Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private void initializeView() {
        Bundle args = getArguments();
        if (args != null) {
            editedEntourage = (Entourage)args.getSerializable(FeedItem.KEY_FEEDITEM);
            if (editedEntourage != null) {
                entourageType = editedEntourage.getEntourageType();
            } else {
                entourageType = args.getString(KEY_ENTOURAGE_TYPE, Entourage.TYPE_CONTRIBUTION);
            }
        }
        initializeTypeTextView();
        initializeLocation();
        initializeTitleEditText();
        initializeDescriptionEditText();
    }

    private void initializeTypeTextView() {
        if (Entourage.TYPE_CONTRIBUTION.equals(entourageType)) {
            typeTextView.setText(R.string.entourage_create_contribution_title);
        } else {
            typeTextView.setText(R.string.entourage_create_demand_title);
        }
    }

    private void initializeLocation() {
        Bundle args = getArguments();
        if (args != null) {
            if (editedEntourage != null) {
                location = editedEntourage.getLocation().getLocation();
            } else {
                location = args.getParcelable(KEY_ENTOURAGE_LOCATION);
            }
            if (location != null) {
                GeocoderTask geocoderTask = new GeocoderTask();
                geocoderTask.execute(location);
            }
        }
    }

    private void initializeTitleEditText() {
        if (Entourage.TYPE_CONTRIBUTION.equals(entourageType)) {
            titleHintTextView.setText(R.string.entourage_create_title_contribution_hint);
            titleEditText.setHint(R.string.entourage_create_title_contribution_hint);
        } else {
            titleHintTextView.setText(R.string.entourage_create_title_demand_hint);
            titleEditText.setHint(R.string.entourage_create_title_demand_hint_long);
        }

        if (editedEntourage != null) {
            titleEditText.setText(editedEntourage.getTitle());
        }

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

        if (Entourage.TYPE_CONTRIBUTION.equals(entourageType)) {
            descriptionEditText.setHint(R.string.entourage_create_description_contribution_hint);
        } else {
            descriptionEditText.setHint(R.string.entourage_create_description_demand_hint);
        }

        if (editedEntourage != null) {
            descriptionEditText.setText(editedEntourage.getDescription());
        }

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
        /*
        String description = descriptionEditText.getText().toString().trim();
        if (description.length() == 0) {
            Toast.makeText(getActivity(), R.string.entourage_create_error_description_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        */
        return true;
    }

    protected void showKeyboard(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    private class GeocoderTask extends AsyncTask<LatLng, Void, String> {

        @Override
        protected String doInBackground(final LatLng... params) {
            try {
                Geocoder geoCoder = new Geocoder(getActivity(), Locale.getDefault());
                LatLng location = params[0];
                List<Address> addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1);
                String addressLine = "";
                if (addresses != null && addresses.size() > 0) {
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

    // ----------------------------------
    // EntourageLocationFragment.OnFragmentInteractionListener
    // ----------------------------------

    public void onEntourageLocationChoosen(LatLng location, String address) {
        if (location != null) {
            this.location = location;
            if (address != null) {
                positionTextView.setText(address);
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {

    }
}
