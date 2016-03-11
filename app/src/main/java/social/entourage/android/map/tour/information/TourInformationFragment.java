package social.entourage.android.map.tour.information;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourTimestamp;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.map.tour.TourService;
import social.entourage.android.map.tour.information.discussion.DiscussionAdapter;

public class TourInformationFragment extends DialogFragment implements TourService.TourServiceListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 2;
    private static final int SCROLL_DELTA_Y_THRESHOLD = 20;

    private static final int REQUEST_NONE = 0;
    private static final int REQUEST_QUIT_TOUR = 1;
    private static final int REQUEST_JOIN_TOUR = 2;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    TourInformationPresenter presenter;

    TourService tourService;
    private ServiceConnection connection = new ServiceConnection();
    private boolean isBound = false;

    @Bind(R.id.tour_info_organization)
    TextView tourOrganization;

    @Bind(R.id.tour_info_author_photo)
    ImageView tourAuthorPhoto;

    @Bind(R.id.tour_info_type)
    TextView tourType;

    @Bind(R.id.tour_info_author_name)
    TextView tourAuthorName;

    @Bind(R.id.tour_info_location)
    TextView tourLocation;

    @Bind(R.id.tour_info_people_count)
    TextView tourPeopleCount;

    @Bind(R.id.tour_info_discussion_view)
    RecyclerView discussionView;

    DiscussionAdapter discussionAdapter;

    @Bind(R.id.tour_info_progress_bar)
    ProgressBar progressBar;

    @Bind(R.id.tour_info_comment_layout)
    LinearLayout commentLayout;

    @Bind(R.id.tour_info_comment)
    EditText commentEditText;

    @Bind(R.id.tour_info_options)
    LinearLayout optionsLayout;

    @Bind(R.id.tour_info_public_section)
    RelativeLayout publicSection;

    @Bind(R.id.tour_info_private_section)
    RelativeLayout privateSection;

    @Bind(R.id.tour_info_share_button)
    AppCompatImageButton shareButton;

    @Bind(R.id.tour_info_more_button)
    AppCompatImageButton moreButton;

    @Bind(R.id.tour_info_join_button)
    Button joinButton;

    int apiRequestsCount;

    Tour tour;

    Date oldestChatMessageDate = null;
    boolean needsMoreChatMessaged = true;
    boolean scrollToLastCard = true;
    private OnScrollListener discussionScrollListener = new OnScrollListener();
    private int scrollDeltaY = 0;

    private SupportMapFragment mapFragment;

    OnTourInformationFragmentFinish mListener;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public static TourInformationFragment newInstance(Tour tour) {
        TourInformationFragment fragment = new TourInformationFragment();
        Bundle args = new Bundle();
        args.putSerializable(Tour.KEY_TOUR, tour);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreateView(inflater, container, savedInstanceState);
        View toReturn = inflater.inflate(R.layout.fragment_tour_information, container, false);
        ButterKnife.bind(this, toReturn);
        initializeView();
        return toReturn;
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());

        if (tour.isPrivate()) {
            loadPrivateCards();
        }
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerTourInformationComponent.builder()
                .entourageComponent(entourageComponent)
                .tourInformationModule(new TourInformationModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof  OnTourInformationFragmentFinish)) {
            throw new ClassCastException(activity.toString() + " must implement OnTourInformationFragmentFinish");
        }
        mListener = (OnTourInformationFragmentFinish)activity;
        doBindService();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        doUnbindService();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                List<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (!textMatchList.isEmpty()) {
                    if (commentEditText.getText().equals("")) {
                        commentEditText.setText(textMatchList.get(0));
                    } else {
                        commentEditText.setText(commentEditText.getText() + " " + textMatchList.get(0));
                    }
                    commentEditText.setSelection(commentEditText.getText().length());
                    FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_OK);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //setup scroll listener
        discussionView.addOnScrollListener(discussionScrollListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        discussionView.removeOnScrollListener(discussionScrollListener);
    }

    // ----------------------------------
    // Button Handling
    // ----------------------------------

    @OnClick(R.id.tour_info_close)
    protected void onCloseButton() {
        this.dismiss();
    }

    @OnClick(R.id.tour_info_comment_add_button)
    protected void onAddCommentButton() {
        presenter.sendTourMessage(commentEditText.getText().toString());
    }

    @OnClick(R.id.tour_info_comment_record_button)
    public void onRecord() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.encounter_leave_voice_message));
        try {
            FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_STARTED);
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), getString(R.string.encounter_voice_message_not_supported), Toast.LENGTH_SHORT).show();
            FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_NOT_SUPPORTED);
        }
    }

    @OnClick(R.id.tour_info_more_button)
    public void onMoreButton() {
        Animation bottomUp = AnimationUtils.loadAnimation(getContext(),
                R.anim.bottom_up);

        optionsLayout.startAnimation(bottomUp);
        optionsLayout.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.tour_info_button_close, R.id.tour_info_options})
    public void onCloseOptionsButton() {
        Animation bottomDown = AnimationUtils.loadAnimation(getContext(),
                R.anim.bottom_down);

        optionsLayout.startAnimation(bottomDown);
        optionsLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.tour_info_button_stop_tour)
    public void onStopTourButton() {
        if (tour.getTourStatus().equals(Tour.TOUR_ON_GOING)) {
            //compute distance
            float distance = 0.0f;
            List<TourPoint> tourPointsList = tour.getTourPoints();
            TourPoint startPoint = tourPointsList.get(0);
            for (int i = 1; i < tourPointsList.size(); i++) {
                TourPoint p = tourPointsList.get(i);
                distance += p.distanceTo(startPoint);
                startPoint = p;
            }
            tour.setDistance(distance);

            //duration
            Date now = new Date();
            Date duration = new Date(now.getTime() - tour.getStartTime().getTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            tour.setDuration(dateFormat.format(duration));

            //hide the options
            optionsLayout.setVisibility(View.GONE);

            //show stop tour activity
            if (mListener != null) {
                mListener.showStopTourActivity(tour);
            }
        }
        else if (tour.getTourStatus().equals(Tour.TOUR_CLOSED)) {
            if (tourService != null) {
                tourService.freezeTour(tour);
            }
        }
    }

    @OnClick(R.id.tour_info_button_quit_tour)
    public void onQuitTourButton() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.tour_info_quit_tour_title)
                .setMessage(R.string.tour_info_quit_tour_description)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (tourService == null) {
                            Toast.makeText(getActivity(), R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            showProgressBar();
                            User me = EntourageApplication.me(getActivity());
                            if (me == null) {
                                Toast.makeText(getActivity(), R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                tourService.removeUserFromTour(tour, me.getId());
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.no, null);
        builder.create().show();
    }

    @OnClick({R.id.tour_info_join_button, R.id.tour_info_share_button})
    public void onJoinTourButton() {
        if (tourService != null) {
            showProgressBar();
            tourService.requestToJoinTour(tour);
        }
        else {
            Toast.makeText(getActivity(), R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show();
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeView() {

        tour = (Tour) getArguments().getSerializable(Tour.KEY_TOUR);

        apiRequestsCount = 0;

        tourOrganization.setText(tour.getOrganizationName());

        String type = tour.getTourType();
        if (type != null) {
            if (type.equals(TourType.MEDICAL.getName())) {
                tourType.setText(getString(R.string.tour_info_text_type_title, getString(R.string.tour_type_medical)));
            } else if (type.equals(TourType.ALIMENTARY.getName())) {
                tourType.setText(getString(R.string.tour_info_text_type_title, getString(R.string.tour_type_alimentary)));
            } else if (type.equals(TourType.BARE_HANDS.getName())) {
                tourType.setText(getString(R.string.tour_info_text_type_title, getString(R.string.tour_type_bare_hands)));
            }
        } else {
            tourType.setText(getString(R.string.tour_info_text_type_title, getString(R.string.tour_info_unknown)));
        }

        tourAuthorName.setText(tour.getAuthor().getUserName());

        String avatarURLAsString = tour.getAuthor().getAvatarURLAsString();
        if (avatarURLAsString != null) {
            Picasso.with(getContext()).load(Uri.parse(avatarURLAsString))
                    .transform(new CropCircleTransformation())
                    .into(tourAuthorPhoto);
        }

        String location = "";
        Address tourAddress = tour.getStartAddress();
        if (tourAddress != null) {
            location = tourAddress.getAddressLine(0);
            if (tourLocation == null) {
                location = "";
            }
        }
        tourLocation.setText(String.format(getResources().getString(R.string.tour_cell_location), Tour.getHoursDiffToNow(tour.getStartTime()), "h", location));

        tourPeopleCount.setText("" + tour.getNumberOfPeople());

        if (tour.isPrivate()) {
            switchToPrivateSection();
        }
        else {
            switchToPublicSection();
        }
    }

    private void initializeOptionsView() {
        User me = EntourageApplication.me(getActivity());
        Button stopTourButton = (Button)optionsLayout.findViewById(R.id.tour_info_button_stop_tour);
        Button quitTourButton = (Button)optionsLayout.findViewById(R.id.tour_info_button_quit_tour);
        stopTourButton.setVisibility(View.GONE);
        quitTourButton.setVisibility(View.GONE);
        if (me != null) {
            int myId = me.getId();
            if (tour.getAuthor().getUserID() != myId) {
                quitTourButton.setVisibility(View.VISIBLE);
            }
            else {
                stopTourButton.setVisibility(tour.getTourStatus().equals(Tour.TOUR_FREEZED) ? View.GONE : View.VISIBLE);
            }
        }
    }

    private void updateHeaderButtons() {
        boolean isTourPrivate = tour.isPrivate();
        shareButton.setVisibility(isTourPrivate ? View.GONE : (tour.getJoinStatus().equals(Tour.JOIN_STATUS_PENDING) ? View.GONE : View.VISIBLE));
        moreButton.setVisibility(isTourPrivate ? View.VISIBLE : View.GONE);
    }

    private void initializeDiscussionList() {

        Date now = new Date();

        //add the start time
        addDiscussionTourStartCard(now);

        //check if we need to add the Tour closed card
        if (tour.isClosed()) {
            addDiscussionTourEndCard();
        }

        //init the recycler view
        discussionView.setLayoutManager(new LinearLayoutManager(getContext()));
        discussionAdapter = new DiscussionAdapter();
        discussionView.setAdapter(discussionAdapter);

        //add the cards
        List<TimestampedObject> cachedCardInfoList = tour.getCachedCardInfoList();
        if (cachedCardInfoList != null) {
            discussionAdapter.addItems(cachedCardInfoList);
        }

        //clear the added cards info
        tour.clearAddedCardInfoList();

        //scroll to last card
        scrollToLastCard();

        //find the oldest chat message received
        initOldestChatMessageDate();
    }

    private void initOldestChatMessageDate() {
        List<TimestampedObject> cachedCardInfoList = tour.getCachedCardInfoList();
        Iterator<TimestampedObject> iterator = cachedCardInfoList.iterator();
        while (iterator.hasNext()) {
            TimestampedObject timestampedObject = iterator.next();
            if (timestampedObject.getClass() != ChatMessage.class) continue;
            ChatMessage chatMessage = (ChatMessage) timestampedObject;
            Date chatCreationDate = chatMessage.getCreationDate();
            if (chatCreationDate == null) continue;
            if (oldestChatMessageDate == null) {
                oldestChatMessageDate = chatCreationDate;
            }
            else {
                if (chatCreationDate.before(oldestChatMessageDate)) {
                    oldestChatMessageDate = chatCreationDate;
                }
            }
        }
    }

    private void initializeMap() {
        if (mapFragment == null) {
            GoogleMapOptions googleMapOptions = new GoogleMapOptions();
            googleMapOptions.zOrderOnTop(true);
            mapFragment = SupportMapFragment.newInstance(googleMapOptions);
        }
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.tour_info_map_layout, mapFragment).commit();

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);

                List<TourPoint> tourPoints = tour.getTourPoints();
                if (tourPoints != null && tourPoints.size() > 0) {
                    //setup the camera position to starting point
                    TourPoint startPoint = tourPoints.get(0);
                    CameraPosition cameraPosition = new CameraPosition(new LatLng(startPoint.getLatitude(), startPoint.getLongitude()), EntourageLocation.INITIAL_CAMERA_FACTOR, 0, 0);
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(startPoint.getLatitude(), startPoint.getLongitude()));
                    googleMap.addMarker(markerOptions);

                    //add the tour points
                    PolylineOptions line = new PolylineOptions();
                    int color = getTrackColor(tour.getTourType(), tour.getStartTime());
                    line.zIndex(2f);
                    line.width(15);
                    line.color(color);
                    for (TourPoint tourPoint : tourPoints) {
                        line.add(tourPoint.getLocation());
                    }
                    googleMap.addPolyline(line);
                }
            }
        });
    }

    private int getTrackColor(String type, Date date) {
        int color = Color.GRAY;
        if (TourType.MEDICAL.getName().equals(type)) {
            color = Color.RED;
        }
        else if (TourType.ALIMENTARY.getName().equals(type)) {
            color = Color.GREEN;
        }
        else if (TourType.BARE_HANDS.getName().equals(type)) {
            color = Color.BLUE;
        }
        if (!MapEntourageFragment.isToday(date)) {
            return MapEntourageFragment.getTransparentColor(color);
        }

        return Color.argb(0, Color.red(color), Color.green(color), Color.blue(color));
    }

    private void switchToPublicSection() {
        publicSection.setVisibility(View.VISIBLE);
        privateSection.setVisibility(View.GONE);

        updateHeaderButtons();
        updateJoinStatus();

        initializeMap();
    }

    private void switchToPrivateSection() {
        publicSection.setVisibility(View.GONE);
        privateSection.setVisibility(View.VISIBLE);

        updateHeaderButtons();
        initializeOptionsView();

        //hide the comment section if the user is not accepted or tour is freezed
        if (!tour.getJoinStatus().equals(Tour.JOIN_STATUS_ACCEPTED) || tour.getTourStatus().equals(Tour.TOUR_FREEZED)) {
            commentLayout.setVisibility(View.GONE);
        }

        initializeDiscussionList();
    }

    private void loadPrivateCards() {
        if (presenter != null) {
            presenter.getTourUsers();
            presenter.getTourMessages();
            presenter.getTourEncounters();
        }
    }

    private void updateJoinStatus() {
        String joinStatus = tour.getJoinStatus();
        if (joinStatus.equals(Tour.JOIN_STATUS_PENDING)) {
            joinButton.setEnabled(false);
            joinButton.setText(R.string.tour_cell_button_pending);
            joinButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.button_act_pending), null, null);
        } else if (joinStatus.equals(Tour.JOIN_STATUS_ACCEPTED)) {
            joinButton.setEnabled(false);
            joinButton.setText(R.string.tour_cell_button_accepted);
            joinButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.button_act_accepted), null, null);
        } else if (joinStatus.equals(Tour.JOIN_STATUS_REJECTED)) {
            joinButton.setEnabled(false);
            joinButton.setText(R.string.tour_cell_button_rejected);
            joinButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.button_act_rejected), null, null);
        } else {
            joinButton.setEnabled(true);
            joinButton.setText(R.string.tour_cell_button_join);
            joinButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.button_act_join), null, null);
        }
    }

    private void updateDiscussionList() {
        updateDiscussionList(true);
    }

    private void updateDiscussionList(boolean scrollToLastCard) {

        List<TimestampedObject> addedCardInfoList = tour.getAddedCardInfoList();
        if (addedCardInfoList == null || addedCardInfoList.size() == 0) {
            return;
        }
        for (int i = 0; i < addedCardInfoList.size(); i++) {

            TimestampedObject cardInfo = addedCardInfoList.get(i);
            discussionAdapter.addCardInfoBeforeTimestamp(cardInfo);

        }

        //clear the added cards info
        tour.clearAddedCardInfoList();

        if (scrollToLastCard) {
            //scroll to last card
            scrollToLastCard();
        }
    }

    private void addDiscussionTourStartCard(Date now) {
        long duration = 0;
        float distance = 0;
        if (tour.getStartTime() != null && !tour.isClosed()) {
            duration = now.getTime() - tour.getStartTime().getTime();
        }
        Date timestamp;
        if (tour.getCachedCardInfoList().size() == 0) {
            timestamp = tour.getStartTime();
        }
        else {
            timestamp = duration == 0 ? tour.getStartTime() : now;
        }
        TourTimestamp tourTimestamp = new TourTimestamp(
                tour.getStartTime(),
                timestamp,
                Tour.TOUR_ON_GOING,
                duration,
                distance
        );
        tour.addCardInfo(tourTimestamp);
    }

    private void addDiscussionTourEndCard() {
        long duration = 0;
        float distance = 0;
        if (tour.getStartTime() != null && tour.getEndTime() != null) {
            duration = tour.getEndTime().getTime() - tour.getStartTime().getTime();
        }
        List<TourPoint> tourPointsList = tour.getTourPoints();
        if (tourPointsList.size() > 1) {
            TourPoint startPoint = tourPointsList.get(0);
            for (int i = 1; i < tourPointsList.size(); i++) {
                TourPoint p = tourPointsList.get(i);
                distance += p.distanceTo(startPoint);
                startPoint = p;
            }
        }
        TourTimestamp tourTimestamp = new TourTimestamp(
                tour.getEndTime(),
                tour.getEndTime(),
                Tour.TOUR_CLOSED,
                duration,
                distance
        );
        tour.addCardInfo(tourTimestamp);
    }

    private void scrollToLastCard() {
        discussionView.scrollToPosition(discussionAdapter.getItemCount()-1);
    }

    protected void showProgressBar() {
        apiRequestsCount++;
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar() {
        apiRequestsCount--;
        if (apiRequestsCount <= 0) {
            progressBar.setVisibility(View.GONE);
            apiRequestsCount = 0;
        }
    }

    private OnTourInformationFragmentFinish getOnTourInformationFragmentFinish() {
        final Activity activity = getActivity();
        return activity != null ? (OnTourInformationFragmentFinish) activity : null;
    }

    // ----------------------------------
    // API callbacks
    // ----------------------------------

    protected void onTourUsersReceived(List<TourUser> tourUsers) {
        if (tourUsers != null) {
            List<TimestampedObject> timestampedObjectList = new ArrayList<>();
            Iterator<TourUser> iterator = tourUsers.iterator();
            while (iterator.hasNext()) {
                TourUser tourUser =  iterator.next();
                //skip the author
                if (tourUser.getUserId() == tour.getAuthor().getUserID()) {
                    continue;
                }
                //skip the rejected user
                if (tourUser.getStatus().equals(Tour.JOIN_STATUS_REJECTED)) {
                    continue;
                }
                timestampedObjectList.add(tourUser);
            }
            tour.addCardInfoList(timestampedObjectList);
        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList();
    }

    protected void onTourMessagesReceived(List<ChatMessage> chatMessageList) {
        if (chatMessageList != null) {
            if (chatMessageList.size() > 0) {
                //check who sent the message
                AuthenticationController authenticationController = EntourageApplication.get(getActivity()).getEntourageComponent().getAuthenticationController();
                if (authenticationController.isAuthenticated()) {
                    int me = authenticationController.getUser().getId();
                    Iterator chatMessageIterator = chatMessageList.iterator();
                    while (chatMessageIterator.hasNext()) {
                        ChatMessage chatMessage = (ChatMessage) chatMessageIterator.next();
                        chatMessage.setIsMe(chatMessage.getUserId() == me);
                    }
                }

                List<TimestampedObject> timestampedObjectList = new ArrayList<>();
                timestampedObjectList.addAll(chatMessageList);
                if (tour.addCardInfoList(timestampedObjectList) > 0) {
                    //remember the last chat message
                    ChatMessage chatMessage = (ChatMessage)tour.getAddedCardInfoList().get(0);
                    oldestChatMessageDate = chatMessage.getCreationDate();
                }
            }
            else {
                //no need to ask for more messages
                needsMoreChatMessaged = false;
            }
        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList(scrollToLastCard);
        scrollToLastCard = false;
    }

    protected void onTourMessageSent(ChatMessage chatMessage) {
        hideProgressBar();

        if (chatMessage == null) {
            Toast.makeText(getContext(), R.string.tour_info_error_chat_message, Toast.LENGTH_SHORT).show();
            return;
        }
        commentEditText.setText("");

        //hide the keyboard
        if (commentEditText.hasFocus()) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
        }

        //add the message to the list
        chatMessage.setIsMe(true);
        tour.addCardInfo(chatMessage);

        updateDiscussionList();
    }

    protected void onTourEncountersReceived(List<Encounter> encounterList) {
        if (encounterList != null) {
            List<TimestampedObject> timestampedObjectList = new ArrayList<>();
            timestampedObjectList.addAll(encounterList);
            tour.addCardInfoList(timestampedObjectList);
        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList();
    }

    protected void onTourQuited(String status) {
        hideProgressBar();
        if (status == null) {
            Toast.makeText(getActivity(), R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    // ----------------------------------
    // SERVICE BINDING METHODS
    // ----------------------------------

    void doBindService() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), TourService.class);
            getActivity().startService(intent);
            getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    void doUnbindService() {
        if (getActivity() != null && isBound) {
            getActivity().unbindService(connection);
            isBound = false;
        }
    }

    // ----------------------------------
    // Tour Service listener implementation
    // ----------------------------------

    @Override
    public void onTourCreated(final boolean created, final long tourId) {

    }

    @Override
    public void onTourUpdated(final LatLng newPoint) {

    }

    @Override
    public void onTourResumed(final List<TourPoint> pointsToDraw, final String tourType, final Date startDate) {

    }

    @Override
    public void onLocationUpdated(final LatLng location) {

    }

    @Override
    public void onRetrieveToursNearby(final List<Tour> tours) {

    }

    @Override
    public void onRetrieveToursByUserId(final List<Tour> tours) {

    }

    @Override
    public void onUserToursFound(final Map<Long, Tour> tours) {

    }

    @Override
    public void onToursFound(final Map<Long, Tour> tours) {

    }

    @Override
    public void onTourClosed(final boolean closed, final Tour tour) {
        //ignore requests that are not related to our tour
        if (tour.getId() != this.tour.getId()) return;

        if (closed) {
            this.tour.setTourStatus(tour.getTourStatus());
            this.tour.setEndTime(tour.getEndTime());
            if (tour.getTourStatus().equals(Tour.TOUR_CLOSED)) {
                addDiscussionTourEndCard();
                updateDiscussionList();
            }
            else if (tour.getTourStatus().equals(Tour.TOUR_FREEZED)){
                commentLayout.setVisibility(View.GONE);
            }
            optionsLayout.setVisibility(View.GONE);
        }
        else {
            Toast.makeText(getActivity(), R.string.tour_close_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGpsStatusChanged(final boolean active) {

    }

    @Override
    public void onUserStatusChanged(final TourUser user, final Tour tour) {
        //ignore requests that are not related to our tour
        if (tour.getId() != this.tour.getId()) return;

        hideProgressBar();

        //check for errors
        if (user == null) {
            Toast.makeText(getActivity(), R.string.tour_info_request_error, Toast.LENGTH_SHORT).show();
            return;
        }

        //update the local tour info
        boolean oldPrivateStatus = tour.isPrivate();
        tour.setJoinStatus(user.getStatus());
        boolean currentPrivateStatus = tour.isPrivate();
        //update UI
        if (oldPrivateStatus != currentPrivateStatus) {
            if (tour.isPrivate()) {
                switchToPrivateSection();
                loadPrivateCards();
            }
            else {
                switchToPublicSection();
            }
        }
        else {
            updateHeaderButtons();
            updateJoinStatus();
        }
    }

    // ----------------------------------
    // RecyclerView.OnScrollListener
    // ----------------------------------

    private class OnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
            if (!needsMoreChatMessaged) return;
            scrollDeltaY += dy;
            //check if user is scrolling up and pass the threshold
            if (dy < 0 && Math.abs(scrollDeltaY) >= SCROLL_DELTA_Y_THRESHOLD) {
                LinearLayoutManager layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                int adapterPosition = recyclerView.findViewHolderForLayoutPosition(firstVisibleItemPosition).getAdapterPosition();
                TimestampedObject timestampedObject = discussionAdapter.getCardAt(adapterPosition);
                Date timestamp = timestampedObject.getTimestamp();
                if (timestamp != null && timestamp.before(oldestChatMessageDate)) {
                    presenter.getTourMessages(oldestChatMessageDate);
                }
                scrollDeltaY = 0;
            }
        }

        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
        }
    }


    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public interface OnTourInformationFragmentFinish {
        void showStopTourActivity(Tour tour);
        void closeTourInformationFragment(TourInformationFragment fragment);
    }

    private class ServiceConnection implements android.content.ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (getActivity() != null) {
                tourService = ((TourService.LocalBinder) service).getService();
                tourService.register(TourInformationFragment.this);
                isBound = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tourService.unregister(TourInformationFragment.this);
            tourService = null;
            isBound = false;
        }
    }
}
