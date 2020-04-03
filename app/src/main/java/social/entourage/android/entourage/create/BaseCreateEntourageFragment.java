package social.entourage.android.entourage.create;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.libraries.places.compat.Place;
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
import social.entourage.android.MainActivity;
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
import social.entourage.android.entourage.category.EntourageCategory;
import social.entourage.android.entourage.category.EntourageCategoryFragment;
import social.entourage.android.entourage.category.EntourageCategoryManager;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.view.EntourageTitleView;
import social.entourage.android.view.HtmlTextView;
import timber.log.Timber;

import static social.entourage.android.entourage.category.EntourageCategoryFragment.KEY_ENTOURAGE_CATEGORY;

/**
 * Base fragment for creating and editing an action/entourage
 */
public class BaseCreateEntourageFragment extends EntourageDialogFragment implements LocationFragment.OnFragmentInteractionListener, CreateEntourageListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.createentourage";

    protected static final String KEY_ENTOURAGE_LOCATION = "social.entourage.android.KEY_ENTOURAGE_LOCATION";
    protected static final String KEY_ENTOURAGE_GROUP_TYPE = "social.entourage.android.KEY_ENTOURAGE_GROUP_TYPE";

    private static final int VOICE_RECOGNITION_TITLE_CODE = 1;
    private static final int VOICE_RECOGNITION_DESCRIPTION_CODE = 2;

    private static final int ADD_HOURS_TO_END_DATE = 3;

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @Inject
    CreateEntouragePresenter presenter;

    @BindView(R.id.create_entourage_category_layout)
    View categoryLayout;

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

    @BindView(R.id.create_entourage_date_start_layout)
    View entourageDateStartLayout;

    @BindView(R.id.create_entourage_date_start)
    TextView entourageDateStartTextView;

    @BindView(R.id.create_entourage_date_end_layout)
    View entourageDateEndLayout;

    @BindView(R.id.create_entourage_date_end)
    TextView entourageDateEndTextView;

    @BindView(R.id.create_entourage_privacy_layout)
    View privacyLayout;

    @BindView(R.id.create_entourage_privacy_label)
    TextView privacyLabel;

    @BindView(R.id.create_entourage_privacy_switch)
    Switch privacySwitch;

    @BindView(R.id.create_entourage_privacy_description)
    TextView privacyDescription;

    protected EntourageCategory entourageCategory;
    protected LatLng location;
    protected String groupType;
    protected Calendar entourageDateStart ;//= Calendar.getInstance();
    protected Calendar entourageDateEnd ;//= Calendar.getInstance();
    protected BaseEntourage.Metadata entourageMetadata;
    protected boolean recipientConsentObtained = true;
    protected boolean joinRequestTypePublic = true;

    protected boolean isSaving = false;

    protected Entourage editedEntourage;

    protected Boolean isStartDateEdited = true;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public BaseCreateEntourageFragment() {
        // Required empty public constructor
    }

    public static CreateEntourageFragment newInstance(LatLng location, @NonNull String groupType, @Nullable EntourageCategory category) {
        CreateEntourageFragment fragment = new CreateEntourageFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_ENTOURAGE_LOCATION, location);
        args.putString(KEY_ENTOURAGE_GROUP_TYPE, groupType);
        if(category!=null) {
            args.putSerializable(KEY_ENTOURAGE_CATEGORY, category);
        }
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
                joinRequestTypePublic = privacySwitch.isChecked();
                if (editedEntourage != null) {
                    saveEditedEntourage();
                } else {
                    createEntourage();
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
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_CREATE_CHANGE_LOCATION);
        LocationFragment fragment = LocationFragment.newInstance(
                location,
                positionTextView.getText().toString(),
                Entourage.TYPE_OUTING.equalsIgnoreCase(groupType),
                this);
        fragment.show(getFragmentManager(), LocationFragment.TAG);
    }

    @OnClick(R.id.create_entourage_title_layout)
    protected void onEditTitleClicked() {
        if (getFragmentManager() == null) return;
        CreateEntourageTitleFragment entourageTitleFragment = CreateEntourageTitleFragment.newInstance(titleEditText.getText().toString(), entourageCategory, groupType);
        entourageTitleFragment.setListener(this);
        entourageTitleFragment.show(getFragmentManager(), CreateEntourageTitleFragment.TAG);
    }

    @OnClick(R.id.create_entourage_description_layout)
    protected void onEditDescriptionClicked() {
        if (getFragmentManager() == null) return;
        CreateEntourageDescriptionFragment descriptionFragment = CreateEntourageDescriptionFragment.newInstance(descriptionEditText.getText().toString(), entourageCategory, groupType);
        descriptionFragment.setListener(this);
        descriptionFragment.show(getFragmentManager(), CreateEntourageDescriptionFragment.TAG);
    }

    @OnClick(R.id.create_entourage_date_start_layout)
    protected void onEditDateStartClicked() {
        isStartDateEdited = true;
        showDatePicker();
    }

    @OnClick(R.id.create_entourage_date_end_layout)
    protected void onEditDateEndClicked() {
        if (entourageDateStart == null) return;
        isStartDateEdited = false;
        showDatePicker();
    }

    @OnClick(R.id.create_entourage_privacy_switch)
    protected void onPrivacySwitchClicked() {
        if (privacySwitch == null) return;
        // adjust the labels accordingly
        if (privacySwitch.isChecked()) {
            if (privacyLabel != null) {
                privacyLabel.setText(R.string.entourage_create_privacy_public);
                privacyLabel.setTypeface(null, Typeface.BOLD);
                privacyLabel.setTextColor(ResourcesCompat.getColor(getResources(), R.color.create_entourage_privacy_public, null));
            }
            if (privacyDescription != null) {
                privacyDescription.setText(R.string.entourage_create_privacy_description_public);
                privacyDescription.requestLayout();
            }
        } else {
            if (privacyLabel != null) {
                privacyLabel.setText(R.string.entourage_create_privacy_private);
                privacyLabel.setTypeface(null, Typeface.NORMAL);
                privacyLabel.setTextColor(ResourcesCompat.getColor(getResources(), R.color.create_entourage_privacy_private, null));
            }
            if (privacyDescription != null) {
                privacyDescription.setText(R.string.entourage_create_privacy_description_private);
                privacyDescription.requestLayout();
            }
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
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_SPEECH);
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
                Toast.makeText(
                        getActivity(),
                        Entourage.TYPE_OUTING.equalsIgnoreCase(groupType) ? R.string.outing_create_error : R.string.entourage_create_error,
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                postEntourageCreated(entourage);
            }
        }
    }

    protected void onEntourageEdited(Entourage entourage) {
        isSaving = false;
        if (getActivity() != null) {
            if (entourage == null) {
                Toast.makeText(
                        getActivity(),
                        Entourage.TYPE_OUTING.equalsIgnoreCase(groupType) ? R.string.outing_save_error : R.string.entourage_save_error,
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                postEntourageSaved(entourage);
            }
        }
    }

    // ----------------------------------
    // Entourage create/edit methods
    // ----------------------------------

    protected void createEntourage() {
        if (isSaving) return;
        isSaving = true;
        TourPoint entourageLocation = new TourPoint(0, 0);
        if (location != null) {
            entourageLocation.setLatitude(location.latitude);
            entourageLocation.setLongitude(location.longitude);
        }
        presenter.createEntourage(
                entourageCategory != null ? entourageCategory.getEntourageType() : null,
                entourageCategory != null ? entourageCategory.getCategory() : null,
                titleEditText.getText().toString(),
                descriptionEditText.getText().toString(),
                entourageLocation,
                recipientConsentObtained,
                groupType,
                entourageMetadata,
                joinRequestTypePublic);
    }

    protected void postEntourageCreated(Entourage entourage) {
        Toast.makeText(
                getActivity(),
                Entourage.TYPE_OUTING.equalsIgnoreCase(groupType) ? R.string.outing_create_ok : R.string.entourage_create_ok,
                Toast.LENGTH_SHORT
        ).show();
        try {
            dismiss();
        } catch(IllegalStateException e) {
            Timber.w(e);
        }
        BusProvider.getInstance().post(new Events.OnFeedItemInfoViewRequestedEvent(entourage));
    }

    protected void saveEditedEntourage() {
        isSaving = true;
        TourPoint entourageLocation = new TourPoint(0, 0);
        if (location != null) {
            entourageLocation.setLatitude(location.latitude);
            entourageLocation.setLongitude(location.longitude);
        }
        editedEntourage.setTitle(titleEditText.getText().toString());
        editedEntourage.setDescription(descriptionEditText.getText().toString());
        editedEntourage.setLocation(entourageLocation);
        if (entourageCategory != null) {
            editedEntourage.setEntourageType(entourageCategory.getEntourageType());
            editedEntourage.setCategory(entourageCategory.getCategory());
        }
        editedEntourage.setGroupType(groupType);
        editedEntourage.setMetadata(entourageMetadata);
        editedEntourage.setJoinRequestPublic(joinRequestTypePublic);
        presenter.editEntourage(editedEntourage);
    }

    protected void postEntourageSaved(Entourage entourage) {
        Toast.makeText(
                getActivity(),
                Entourage.TYPE_OUTING.equalsIgnoreCase(groupType) ? R.string.outing_save_ok : R.string.entourage_save_ok,
                Toast.LENGTH_SHORT
        ).show();
        dismiss();
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
            groupType = args.getString(KEY_ENTOURAGE_GROUP_TYPE, null);
            entourageCategory = (EntourageCategory)args.getSerializable(KEY_ENTOURAGE_CATEGORY);
        }
        initializeCategory();
        initializeLocation();
        initializeTitleEditText();
        initializeDescriptionEditText();
        initializeDate();
        initializeJoinRequestType();
        initializeHelpHtmlView();
    }

    protected void initializeCategory() {
        if (editedEntourage != null) {
            entourageCategory = EntourageCategoryManager.getInstance().findCategory(editedEntourage.getEntourageType(), editedEntourage.getCategory());
            groupType = editedEntourage.getGroupType();
        }
        if(entourageCategory==null) {
            if(groupType != null){
                entourageCategory = EntourageCategoryManager.getInstance().getDefaultCategory(groupType);
            } else {
                entourageCategory = EntourageCategoryManager.getInstance().getDefaultCategory();
            }
        }
        if (entourageCategory != null) {
            entourageCategory.setSelected(true);
        }
        updateFragmentTitle();
        updateCategoryTextView();
    }

    protected void updateCategoryTextView() {
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
        categoryLayout.setVisibility((groupType != null && groupType.equalsIgnoreCase(Entourage.TYPE_OUTING)) ? View.GONE : View.VISIBLE);
    }

    protected void updateFragmentTitle() {
        if (Entourage.TYPE_OUTING.equalsIgnoreCase(groupType)) {
            if (getView() != null) {
                EntourageTitleView fragmentTitleView = getView().findViewById(R.id.create_entourage_fragment_title);
                if (fragmentTitleView != null) {
                    fragmentTitleView.setTitle(getString(R.string.entourage_create_outing_title));
                }
            }
        }
    }

    protected void initializeLocation() {
        Bundle args = getArguments();
        if (args != null) {
            if (editedEntourage != null) {
                location = editedEntourage.getLocation().getLocation();
                if (Entourage.TYPE_OUTING.equalsIgnoreCase(groupType)) {
                    BaseEntourage.Metadata metadata = editedEntourage.getMetadata();
                    if (metadata != null) {
                        positionTextView.setText(metadata.getDisplayAddress());
                    }
                }
            } else {
                location = args.getParcelable(KEY_ENTOURAGE_LOCATION);
            }
            if (location != null && !Entourage.TYPE_OUTING.equalsIgnoreCase(groupType)) {
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
            if (metadata != null && metadata.getStartDate() != null) {
                entourageDateStart = Calendar.getInstance();
                entourageDateStart.setTime(metadata.getStartDate());
            }
            if (metadata != null && metadata.getEndDate() != null) {
                entourageDateEnd = Calendar.getInstance();
                entourageDateEnd.setTime(metadata.getEndDate());
            }
            updateDateStartTextView();
            updateDateEndTextView();
        }
        entourageDateStartLayout.setVisibility((groupType != null && groupType.equalsIgnoreCase(Entourage.TYPE_OUTING)) ? View.VISIBLE : View.GONE);
        entourageDateEndLayout.setVisibility((groupType != null && groupType.equalsIgnoreCase(Entourage.TYPE_OUTING)) ? View.VISIBLE : View.GONE);
    }

    protected void initializeJoinRequestType() {
        privacyLayout.setVisibility(Entourage.TYPE_OUTING.equalsIgnoreCase(groupType) ? View.VISIBLE : View.GONE);
        if (editedEntourage != null) {
            privacySwitch.setChecked(editedEntourage.isJoinRequestPublic());
            onPrivacySwitchClicked();
        }
    }

    private void initializeHelpHtmlView() {
        if (getView() == null) return;
        HtmlTextView helpHtmlTextView = getView().findViewById(R.id.create_entourage_help_link);
        if (helpHtmlTextView != null) {
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                String goalLink = ((MainActivity) getActivity()).getLink(Constants.GOAL_LINK_ID);
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
        if (Entourage.TYPE_OUTING.equalsIgnoreCase(groupType)) {
            String dateString = entourageDateStartTextView.getText().toString().trim();
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
        if (Entourage.TYPE_OUTING.equalsIgnoreCase(groupType)) {
            if (entourageMetadata == null || entourageMetadata.getGooglePlaceId() == null || entourageMetadata.getGooglePlaceId().length() == 0) {
                Toast.makeText(getActivity(), R.string.entourage_create_error_location_empty, Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
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
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    if (address.getMaxAddressLineIndex() >= 0) {
                        return addresses.get(0).getAddressLine(0);
                    }
                }
            }
            catch (IOException | NullPointerException | IllegalStateException ignored) {

            }
            return "";
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
        if (Entourage.TYPE_OUTING.equalsIgnoreCase(groupType)) {
            if (place != null) {
                if (entourageMetadata == null) entourageMetadata = new BaseEntourage.Metadata();
                entourageMetadata.setPlaceName(place.getName().toString());
                if (place.getAddress() != null) {
                    entourageMetadata.setStreetAddress(place.getAddress().toString());
                    positionTextView.setText(place.getAddress().toString());
                }
                entourageMetadata.setGooglePlaceId(place.getId());
                this.location = place.getLatLng();
            } else {
                positionTextView.setText("");
            }
        } else {
            if (location != null) {
                this.location = location;
                if (address != null) {
                    positionTextView.setText(address);
                }
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

    protected void updateDateStartTextView() {
        if (entourageDateStart == null) return;
        DateFormat df = new SimpleDateFormat(getString(R.string.entourage_create_date_format), Locale.getDefault());
        entourageDateStartTextView.setText(df.format(entourageDateStart.getTime()));
    }

    protected void updateDateEndTextView() {
        if (entourageDateEnd == null) return;
        DateFormat df = new SimpleDateFormat(getString(R.string.entourage_create_date_format), Locale.getDefault());
        entourageDateEndTextView.setText(df.format(entourageDateEnd.getTime()));
    }

    private void showDatePicker() {
        if (getActivity() == null || getActivity().getFragmentManager() == null) return;
        DatePickerDialog dpd;
        if (isStartDateEdited) {
            entourageDateStart = Calendar.getInstance();
            dpd = DatePickerDialog.newInstance(
                    this,
                    entourageDateStart.get(Calendar.YEAR),
                    entourageDateStart.get(Calendar.MONTH),
                    entourageDateStart.get(Calendar.DAY_OF_MONTH)
            );
            dpd.setMinDate(Calendar.getInstance()); // only today and future dates
        }
        else {
            dpd = DatePickerDialog.newInstance(
                    this,
                    entourageDateEnd.get(Calendar.YEAR),
                    entourageDateEnd.get(Calendar.MONTH),
                    entourageDateEnd.get(Calendar.DAY_OF_MONTH)
            );
            dpd.setMinDate(entourageDateStart); // only after start date
        }

        dpd.setCancelText(R.string.cancel);
        dpd.show(getActivity().getSupportFragmentManager(), "DatePickerDialog");
    }

    private void showTimePicker() {
        if (getActivity() == null || getActivity().getFragmentManager() == null) return;
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                BaseCreateEntourageFragment.this,
                true);
        if (isStartDateEdited) {
            tpd.setInitialSelection(entourageDateStart.get(Calendar.HOUR_OF_DAY), entourageDateStart.get(Calendar.MINUTE));
        }
        else {
            tpd.setInitialSelection(entourageDateEnd.get(Calendar.HOUR_OF_DAY), entourageDateEnd.get(Calendar.MINUTE));
            tpd.setMinTime(entourageDateStart.get(Calendar.HOUR_OF_DAY),
                    entourageDateStart.get(Calendar.MINUTE),
                    entourageDateStart.get(Calendar.SECOND)); //Only after time from start date
        }

        tpd.setCancelText(R.string.cancel);
        tpd.show(getActivity().getSupportFragmentManager(), "TimePickerDialog");
    }

    @Override
    public void onDateSet(final DatePickerDialog view, final int year, final int monthOfYear, final int dayOfMonth) {
        if (isStartDateEdited) {
            entourageDateStart.set(year, monthOfYear, dayOfMonth);
            if (entourageDateEnd == null || entourageDateStart.after(entourageDateEnd)) {
                entourageDateEnd = Calendar.getInstance();
                entourageDateEnd.setTime(entourageDateStart.getTime());
                entourageDateEnd.set(Calendar.HOUR,entourageDateEnd.get(Calendar.HOUR) + ADD_HOURS_TO_END_DATE);
            }
        }
        else {
            entourageDateEnd.set(year, monthOfYear, dayOfMonth);
        }
        new Handler().post(this::showTimePicker);
    }

    @Override
    public void onTimeSet(final TimePickerDialog view, final int hourOfDay, final int minute, final int second) {
        if (isStartDateEdited) {
            entourageDateStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
            entourageDateStart.set(Calendar.MINUTE, minute);
            entourageDateStart.set(Calendar.SECOND, second);

            if (entourageDateStart.after(entourageDateEnd)) {
                entourageDateEnd.setTime(entourageDateStart.getTime());
                entourageDateEnd.set(Calendar.HOUR,entourageDateEnd.get(Calendar.HOUR) + ADD_HOURS_TO_END_DATE);
            }
        }
        else {
            entourageDateEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
            entourageDateEnd.set(Calendar.MINUTE, minute);
            entourageDateEnd.set(Calendar.SECOND, second);
        }
        if (entourageMetadata == null) entourageMetadata = new BaseEntourage.Metadata();
        entourageMetadata.setEndDate(entourageDateEnd.getTime());
        entourageMetadata.setStartDate(entourageDateStart.getTime());
        updateDateStartTextView();
        updateDateEndTextView();
    }
}
