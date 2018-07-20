package social.entourage.android.map.entourage.create;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.map.BaseEntourage;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.base.EntourageLinkMovementMethod;
import social.entourage.android.location.LocationFragment;
import social.entourage.android.map.entourage.category.EntourageCategory;
import social.entourage.android.map.entourage.category.EntourageCategoryFragment;
import social.entourage.android.map.entourage.category.EntourageCategoryManager;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.view.HtmlTextView;

/**
 * Base fragment for creating and editing an action/entourage
 */
public class BaseCreateEntourageFragment extends EntourageDialogFragment implements LocationFragment.OnFragmentInteractionListener, CreateEntourageListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.createentourage";

    protected static final String KEY_ENTOURAGE_LOCATION = "social.entourage.android.KEY_ENTOURAGE_LOCATION";

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

    @BindView(R.id.create_entourage_date)
    TextView entourageDateTextView;

    protected EntourageCategory entourageCategory;
    protected LatLng location;
    protected String groupType;
    protected Calendar entourageDate = Calendar.getInstance();
    protected BaseEntourage.Metadata entourageMetadata;

    protected boolean isSaving = false;

    protected Entourage editedEntourage;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public BaseCreateEntourageFragment() {
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_entourage_create, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get().getEntourageComponent());

        initializeView();
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
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

    @OnClick(R.id.title_close_button)
    protected void onCloseClicked() {
        dismiss();
    }

    @OnClick(R.id.title_action_button)
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
                    editedEntourage.setGroupType(groupType);
                    editedEntourage.setMetadata(entourageMetadata);
                    presenter.editEntourage(editedEntourage);
                } else {
                    presenter.createEntourage(
                            entourageCategory != null ? entourageCategory.getEntourageType() : null,
                            entourageCategory != null ? entourageCategory.getCategory() : null,
                            titleEditText.getText().toString(),
                            descriptionEditText.getText().toString(),
                            entourageLocation,
                            groupType,
                            entourageMetadata);
                }
            } else {
                Toast.makeText(getActivity(), R.string.entourage_create_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.create_entourage_category_layout)
    protected void onEditTypeClicked() {
        if (getFragmentManager() == null) return;
        EntourageCategoryFragment fragment = EntourageCategoryFragment.newInstance(entourageCategory);
        fragment.setListener(this);
        fragment.show(getFragmentManager(), EntourageCategoryFragment.TAG);
    }

    @OnClick(R.id.create_entourage_position_layout)
    protected void onPositionClicked() {
        if (getFragmentManager() == null) return;
        EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_CREATE_CHANGE_LOCATION);
        LocationFragment fragment = LocationFragment.newInstance(location, positionTextView.getText().toString(), this);
        fragment.show(getFragmentManager(), LocationFragment.TAG);
    }

    @OnClick(R.id.create_entourage_title_layout)
    protected void onEditTitleClicked() {
        if (getFragmentManager() == null) return;
        CreateEntourageTitleFragment entourageTitleFragment = CreateEntourageTitleFragment.newInstance(titleEditText.getText().toString(), entourageCategory);
        entourageTitleFragment.setListener(this);
        entourageTitleFragment.show(getFragmentManager(), CreateEntourageTitleFragment.TAG);
    }

    @OnClick(R.id.create_entourage_description_layout)
    protected void onEditDescriptionClicked() {
        if (getFragmentManager() == null) return;
        CreateEntourageDescriptionFragment descriptionFragment = CreateEntourageDescriptionFragment.newInstance(descriptionEditText.getText().toString(), entourageCategory);
        descriptionFragment.setListener(this);
        descriptionFragment.show(getFragmentManager(), CreateEntourageDescriptionFragment.TAG);
    }

    @OnClick(R.id.create_entourage_date_layout)
    protected void onEditDateClicked() {
        showDatePicker();
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
                BusProvider.getInstance().post(new Events.OnFeedItemInfoViewRequestedEvent(entourage));
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

    protected void initializeView() {
        Bundle args = getArguments();
        if (args != null) {
            editedEntourage = (Entourage)args.getSerializable(FeedItem.KEY_FEEDITEM);
            if (editedEntourage != null) {
                entourageMetadata = editedEntourage.getMetadata();
            }
        }
        initializeCategory();
        initializeLocation();
        initializeTitleEditText();
        initializeDescriptionEditText();
        initializeDate();
        initializeHelpHtmlView();
    }

    protected void initializeCategory() {
        if (editedEntourage != null) {
            String entourageType = editedEntourage.getEntourageType();
            String category = editedEntourage.getCategory();
            entourageCategory = EntourageCategoryManager.getInstance().findCategory(entourageType, category);
            if (entourageCategory != null) {
                entourageCategory.setSelected(true);
            }
            groupType = editedEntourage.getGroupType();
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

    protected void initializeLocation() {
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

    protected void initializeTitleEditText() {
        if (editedEntourage != null) {
            titleEditText.setText(editedEntourage.getTitle());
        }
    }

    protected void initializeDescriptionEditText() {
        if (editedEntourage != null) {
            descriptionEditText.setText(editedEntourage.getDescription());
        }
    }

    protected void initializeDate() {
        if (editedEntourage != null) {
            BaseEntourage.Metadata metadata = editedEntourage.getMetadata();
            if (metadata != null) {
                entourageDate.setTime(metadata.getStartDate());
            }
            updateDateTextView();
        }
    }

    private void initializeHelpHtmlView() {
        if (getView() == null) return;
        HtmlTextView helpHtmlTextView = getView().findViewById(R.id.create_entourage_help_link);
        if (helpHtmlTextView != null) {
            if (getActivity() != null && getActivity() instanceof DrawerActivity) {
                String goalLink = ((DrawerActivity) getActivity()).getLink(Constants.GOAL_LINK_ID);
                helpHtmlTextView.setHtmlString(getString(R.string.entourage_create_help_text, goalLink), EntourageLinkMovementMethod.getInstance());
            }
        }
    }

    protected boolean isValid() {
        String title = titleEditText.getText().toString().trim();
        if (title.length() == 0) {
            Toast.makeText(getActivity(), R.string.entourage_create_error_title_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (groupType != null && groupType.equalsIgnoreCase(Entourage.TYPE_OUTING)) {
            String dateString = entourageDateTextView.getText().toString().trim();
            if (dateString.length() == 0) {
                Toast.makeText(getActivity(), R.string.entourage_create_error_date_empty, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            if (entourageCategory == null) {
                Toast.makeText(getActivity(), R.string.entourage_create_error_category_empty, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (location == null) {
            Toast.makeText(getActivity(), R.string.entourage_create_error_location_empty, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            String address = positionTextView.getText().toString().trim();
            if (address.length() == 0) {
                Toast.makeText(getActivity(), R.string.entourage_create_error_location_empty, Toast.LENGTH_SHORT).show();
                return false;
            }
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
            BaseCreateEntourageFragment.this.positionTextView.setText(address);
        }
    }

    // ----------------------------------
    // LocationFragment.OnFragmentInteractionListener
    // ----------------------------------

    public void onEntourageLocationChosen(LatLng location, String address, Place place) {
        if (location != null) {
            this.location = location;
            if (address != null) {
                positionTextView.setText(address);
            }
        }
        if (place != null) {
            if (groupType != null && groupType.equalsIgnoreCase(Entourage.TYPE_OUTING)) {
                if (entourageMetadata == null) entourageMetadata = new BaseEntourage.Metadata();
                entourageMetadata.setPlaceName(place.getName().toString());
                if (place.getAddress() != null) {
                    entourageMetadata.setStreetAddress(place.getAddress().toString());
                }
                entourageMetadata.setGooglePlaceId(place.getId());
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

    // ----------------------------------
    // Date/Time Methods
    // ----------------------------------

    protected void updateDateTextView() {
        if (entourageDate == null) return;
        DateFormat df = new SimpleDateFormat(getString(R.string.entourage_create_date_format), Locale.getDefault());
        entourageDateTextView.setText(df.format(entourageDate.getTime()));
    }

    private void showDatePicker() {
        if (getActivity() == null || getActivity().getFragmentManager() == null) return;
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                entourageDate.get(Calendar.YEAR),
                entourageDate.get(Calendar.MONTH),
                entourageDate.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setCancelText(R.string.cancel);
        dpd.setMinDate(Calendar.getInstance()); // only today and future dates
        dpd.show(getActivity().getFragmentManager(), "DatePickerDialog");
    }

    private void showTimePicker() {
        if (getActivity() == null || getActivity().getFragmentManager() == null) return;
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                BaseCreateEntourageFragment.this,
                true);
        tpd.setInitialSelection(entourageDate.get(Calendar.HOUR_OF_DAY), entourageDate.get(Calendar.MINUTE));
        tpd.setCancelText(R.string.cancel);
        tpd.show(getActivity().getFragmentManager(), "TimePickerDialog");
    }

    @Override
    public void onDateSet(final DatePickerDialog view, final int year, final int monthOfYear, final int dayOfMonth) {
        entourageDate.set(year, monthOfYear, dayOfMonth);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                showTimePicker();
            }
        });
    }

    @Override
    public void onTimeSet(final TimePickerDialog view, final int hourOfDay, final int minute, final int second) {
        entourageDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        entourageDate.set(Calendar.MINUTE, minute);
        entourageDate.set(Calendar.SECOND, second);
        if (entourageMetadata == null) entourageMetadata = new BaseEntourage.Metadata();
        entourageMetadata.setStartDate(entourageDate.getTime());
        updateDateTextView();
    }
}
