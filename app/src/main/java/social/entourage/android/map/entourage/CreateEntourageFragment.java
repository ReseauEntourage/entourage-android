package social.entourage.android.map.entourage;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

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
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.map.entourage.category.EntourageCategory;
import social.entourage.android.map.entourage.category.EntourageCategoryFragment;
import social.entourage.android.map.entourage.category.EntourageCategoryManager;

/**
 *
 */
public class CreateEntourageFragment extends EntourageDialogFragment implements EntourageLocationFragment.OnFragmentInteractionListener, CreateEntourageListener {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.createentourage";

    private static final String KEY_ENTOURAGE_LOCATION = "social.entourage.android.KEY_ENTOURAGE_LOCATION";

    private static final int VOICE_RECOGNITION_TITLE_CODE = 1;
    private static final int VOICE_RECOGNITION_DESCRIPTION_CODE = 2;

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @Inject
    CreateEntouragePresenter presenter;

    @BindView(R.id.create_entourage_category)
    TextView categoryTextView;

    @BindView(R.id.create_entourage_position)
    TextView positionTextView;

    @BindView(R.id.create_entourage_title)
    TextView titleEditText;

    @BindView(R.id.create_entourage_title_label)
    TextView titleLabelTextView;

//    @BindView(R.id.create_entourage_title_char_count)
//    TextView titleCharCountTextView;

    @BindView(R.id.create_entourage_description)
    TextView descriptionEditText;

    @BindView(R.id.create_entourage_description_label)
    TextView descriptionLabelTextView;

    private EntourageCategory entourageCategory;
    private LatLng location;

    private boolean isSaving = false;

    private Entourage editedEntourage;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public CreateEntourageFragment() {
        // Required empty public constructor
    }

    public static CreateEntourageFragment newInstance(LatLng location) {
        CreateEntourageFragment fragment = new CreateEntourageFragment();
        Bundle args = new Bundle();
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
                    //titleEditText.setSelection(titleEditText.getText().length());
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
                    //descriptionEditText.setSelection(descriptionEditText.getText().length());
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
                    if (entourageCategory != null) {
                        editedEntourage.setEntourageType(entourageCategory.getEntourageType());
                        editedEntourage.setCategory(entourageCategory.getCategory());
                    }
                    presenter.editEntourage(editedEntourage);
                } else {
                    presenter.createEntourage(
                            entourageCategory.getEntourageType(),
                            entourageCategory.getCategory(),
                            titleEditText.getText().toString(),
                            descriptionEditText.getText().toString(),
                            entourageLocation);
                }
            } else {
                Toast.makeText(getActivity(), R.string.entourage_create_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.create_entourage_category_layout)
    protected void onEditTypeClicked() {
        EntourageCategoryFragment fragment = EntourageCategoryFragment.newInstance(entourageCategory);
        fragment.setListener(this);
        fragment.show(getFragmentManager(), EntourageCategoryFragment.TAG);
    }

    @OnClick(R.id.create_entourage_position_layout)
    protected void onPositionClicked() {
        EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_CREATE_CHANGE_LOCATION);
        EntourageLocationFragment fragment = EntourageLocationFragment.newInstance(location, positionTextView.getText().toString(), this);
        fragment.show(getFragmentManager(), EntourageLocationFragment.TAG);
    }

    @OnClick(R.id.create_entourage_title_layout)
    protected void onEditTitleClicked() {
        CreateEntourageTitleFragment entourageTitleFragment = CreateEntourageTitleFragment.newInstance(titleEditText.getText().toString(), entourageCategory);
        entourageTitleFragment.setListener(this);
        entourageTitleFragment.show(getFragmentManager(), CreateEntourageTitleFragment.TAG);
    }

    @OnClick(R.id.create_entourage_description_layout)
    protected void onEditDescriptionClicked() {
        CreateEntourageDescriptionFragment descriptionFragment = CreateEntourageDescriptionFragment.newInstance(descriptionEditText.getText().toString(), entourageCategory);
        descriptionFragment.setListener(this);
        descriptionFragment.show(getFragmentManager(), CreateEntourageDescriptionFragment.TAG);
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
            EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_SPEECH);
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
        }
        initializeCategory();
        initializeLocation();
        initializeTitleEditText();
        initializeDescriptionEditText();
    }

    private void initializeCategory() {
        if (editedEntourage != null) {
            String entourageType = editedEntourage.getEntourageType();
            String category = editedEntourage.getCategory();
            entourageCategory = EntourageCategoryManager.getInstance().findCategory(entourageType, category);
            if (entourageCategory != null) {
                entourageCategory.setDefault(true);
            }
        } else {
            entourageCategory = null;
        }
        updateCategoryTextView();
    }

    private void updateCategoryTextView() {
        if (entourageCategory == null) {
            categoryTextView.setText("");
        } else {
            categoryTextView.setText(
                    getString(
                            Entourage.TYPE_DEMAND.equalsIgnoreCase(entourageCategory.getEntourageType()) ? R.string.entourage_create_type_demand : R.string.entourage_create_type_contribution,
                            entourageCategory.getTitle()
                    )
            );
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
        if (editedEntourage != null) {
            titleEditText.setText(editedEntourage.getTitle());
        }
    }

    private void initializeDescriptionEditText() {
        if (editedEntourage != null) {
            descriptionEditText.setText(editedEntourage.getDescription());
        }
    }

    private boolean isValid() {
        if (entourageCategory == null) {
            Toast.makeText(getActivity(), R.string.entourage_create_error_category_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        String title = titleEditText.getText().toString().trim();
        if (title.length() == 0) {
            Toast.makeText(getActivity(), R.string.entourage_create_error_title_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
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
            catch (IOException ignored) {

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

    public void onEntourageLocationChosen(LatLng location, String address) {
        if (location != null) {
            this.location = location;
            if (address != null) {
                positionTextView.setText(address);
            }
        }
    }

    // ----------------------------------
    // CreateEntourageListener
    // ----------------------------------

    @Override
    public void onTitleChanged(final String title) {
        titleEditText.setText(title);
    }

    @Override
    public void onDescriptionChanged(final String description) {
        descriptionEditText.setText(description);
    }

    @Override
    public void onCategoryChosen(final EntourageCategory category) {
        this.entourageCategory = category;
        updateCategoryTextView();
    }
}
