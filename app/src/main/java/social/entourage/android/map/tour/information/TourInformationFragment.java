package social.entourage.android.map.tour.information;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import butterknife.Optional;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageError;
import social.entourage.android.EntourageEvents;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.BaseEntourage;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourAuthor;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourTimestamp;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.Events;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.carousel.CarouselFragment;
import social.entourage.android.configuration.Configuration;
import social.entourage.android.deeplinks.DeepLinksManager;
import social.entourage.android.invite.InviteFriendsListener;
import social.entourage.android.invite.contacts.InviteContactsFragment;
import social.entourage.android.invite.phonenumber.InviteByPhoneNumberFragment;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.map.entourage.create.CreateEntourageFragment;
import social.entourage.android.map.entourage.EntourageCloseFragment;
import social.entourage.android.map.tour.TourService;
import social.entourage.android.map.tour.information.discussion.DiscussionAdapter;
import social.entourage.android.map.tour.information.members.MembersAdapter;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.tools.Utils;

public class TourInformationFragment extends EntourageDialogFragment implements TourService.TourServiceListener, InviteFriendsListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "fragment_tour_information";

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 2;
    private static final int READ_CONTACTS_PERMISSION_CODE = 3;

    private static final int SCROLL_DELTA_Y_THRESHOLD = 20;

    private static final int MAP_SNAPSHOT_ZOOM = 15;

    private static final String KEY_INVITATION_ID = "social.entourage.android_KEY_INVITATION_ID";
    private static final String KEY_FEED_POSITION = "social.entourage.android.KEY_FEED_POSITION";
    private static final String KEY_FEED_SHARE_URL = "social.entourage.android.KEY_FEED_SHARE_URL";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    TourInformationPresenter presenter;

    TourService tourService;
    private ServiceConnection connection = new ServiceConnection();
    private boolean isBound = false;

    @BindView(R.id.tour_info_icon)
    ImageView tourIcon;

    @Nullable
    @BindView(R.id.tour_info_title)
    TextView fragmentTitle;

    @BindView(R.id.tour_info_description)
    TextView tourDescription;

    @BindView(R.id.tour_info_discussion_view)
    RecyclerView discussionView;

    DiscussionAdapter discussionAdapter;

    @BindView(R.id.tour_info_loading_view)
    View loadingView;

    @BindView(R.id.tour_info_progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.tour_info_comment_layout)
    LinearLayout commentLayout;

    @BindView(R.id.tour_info_comment)
    EditText commentEditText;

    @BindView(R.id.tour_info_comment_send_button)
    Button commentSendButton;

    @BindView(R.id.tour_info_comment_record_button)
    AppCompatImageButton commentRecordButton;

    @BindView(R.id.tour_info_options)
    LinearLayout optionsLayout;

    @BindView(R.id.tour_info_public_section)
    RelativeLayout publicSection;

    @BindView(R.id.tour_info_private_section)
    RelativeLayout privateSection;

    @Nullable
    @BindView(R.id.tour_info_description_button)
    AppCompatImageButton descriptionButton;

    @Nullable
    @BindView(R.id.tour_info_more_button)
    AppCompatImageButton moreButton;

    @BindView(R.id.tour_info_act_layout)
    RelativeLayout actLayout;

    @BindView(R.id.tour_info_act_button)
    Button actButton;

    @BindView(R.id.tour_info_act_divider_left)
    View actDividerLeft;

    @BindView(R.id.tour_info_act_divider_right)
    View actDividerRight;

    @BindView(R.id.tour_info_invite_source_layout)
    RelativeLayout inviteSourceLayout;

    @BindView(R.id.tour_info_invite_success_layout)
    RelativeLayout inviteSuccessLayout;

    @BindView(R.id.tour_info_members_layout)
    LinearLayout membersLayout;

    @BindView(R.id.tour_info_member_count)
    TextView membersCountTextView;

    @BindView(R.id.tour_info_members)
    RecyclerView membersView;

    @BindView(R.id.tour_info_request_join_layout)
    LinearLayout requestJoinLayout;

    @BindView(R.id.tour_info_request_join_title)
    TextView requestJoinTitle;

    @BindView(R.id.tour_info_request_join_button)
    Button requestJoinButton;

    MembersAdapter membersAdapter;
    List<TimestampedObject> membersList = new ArrayList<>();

    // Invitation Layout
    @BindView(R.id.tour_info_invited_layout)
    View invitedLayout;

    int apiRequestsCount;

    FeedItem feedItem;
    String requestedFeedItemUUID;
    int requestedFeedItemType;
    String requestedFeedItemShareURL;
    long invitationId;
    boolean acceptInvitationSilently = false;
    boolean showInfoButton = true;

    Date oldestChatMessageDate = null;
    boolean needsMoreChatMessaged = true;
    boolean scrollToLastCard = true;
    private OnScrollListener discussionScrollListener = new OnScrollListener();
    private int scrollDeltaY = 0;

    private SupportMapFragment mapFragment;

    private SupportMapFragment hiddenMapFragment;
    private GoogleMap hiddenGoogleMap;
    private boolean isTakingSnapshot = false;
    private Bitmap mapSnapshot;
    private boolean takeSnapshotOnCameraMove = false;
    List<TourTimestamp> tourTimestampList = new ArrayList<>();

    OnTourInformationFragmentFinish mListener;

    // Handler to hide invite success layout
    private Handler inviteSuccessHandler = new Handler();
    private Runnable inviteSuccessRunnable = new Runnable() {
        @Override
        public void run() {
            inviteSuccessLayout.setVisibility(View.GONE);
        }
    };

    private boolean startedTypingMessage = false;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public static TourInformationFragment newInstance(FeedItem feedItem, long invitationId, int feedRank) {
        TourInformationFragment fragment = new TourInformationFragment();
        Bundle args = new Bundle();
        args.putSerializable(FeedItem.KEY_FEEDITEM, feedItem);
        args.putLong(KEY_INVITATION_ID, invitationId);
        args.putInt(KEY_FEED_POSITION, feedRank);
        fragment.setArguments(args);
        return fragment;
    }

    public static TourInformationFragment newInstance(String feedItemUUID, int feedItemType, long invitationId) {
        TourInformationFragment fragment = new TourInformationFragment();
        Bundle args = new Bundle();
        args.putString(FeedItem.KEY_FEEDITEM_UUID, feedItemUUID);
        args.putInt(FeedItem.KEY_FEEDITEM_TYPE, feedItemType);
        args.putLong(KEY_INVITATION_ID, invitationId);
        fragment.setArguments(args);
        return fragment;
    }

    public static TourInformationFragment newInstance(String shareURL, int feedItemType) {
        TourInformationFragment fragment = new TourInformationFragment();
        Bundle args = new Bundle();
        args.putString(KEY_FEED_SHARE_URL, shareURL);
        args.putInt(FeedItem.KEY_FEEDITEM_TYPE, feedItemType);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View toReturn = inflater.inflate(R.layout.fragment_tour_information, container, false);
        ButterKnife.bind(this, toReturn);

        return toReturn;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get().getEntourageComponent());

        Bundle args = getArguments();
        if (args != null) {
            feedItem = (FeedItem) args.getSerializable(FeedItem.KEY_FEEDITEM);
            invitationId = args.getLong(KEY_INVITATION_ID);
            if (feedItem != null) {
                if (feedItem.isPrivate()) {
                    initializeView();
                } else {
                    // public entourage
                    // we need to retrieve the whole entourage again, just to send the distance and feed position
                    int feedRank = args.getInt(KEY_FEED_POSITION);
                    int distance = 0;
                    TourPoint startPoint = feedItem.getStartPoint();
                    if (startPoint != null) {
                        Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
                        if (currentLocation != null) {
                            distance = (int) Math.ceil(startPoint.distanceTo(new TourPoint(currentLocation.getLatitude(), currentLocation.getLongitude())) / 1000); // in kilometers
                        }
                    }
                    presenter.getFeedItem(feedItem.getUUID(), feedItem.getType(), feedRank, distance);
                    feedItem = null;
                }
            } else {
                requestedFeedItemShareURL = args.getString(KEY_FEED_SHARE_URL);
                requestedFeedItemUUID = args.getString(FeedItem.KEY_FEEDITEM_UUID);
                requestedFeedItemType = args.getInt(FeedItem.KEY_FEEDITEM_TYPE);
                if (requestedFeedItemType == TimestampedObject.TOUR_CARD || requestedFeedItemType == TimestampedObject.ENTOURAGE_CARD) {
                    if (requestedFeedItemShareURL != null && requestedFeedItemShareURL.length() > 0) {
                        presenter.getFeedItem(requestedFeedItemShareURL, requestedFeedItemType);
                    } else {
                        presenter.getFeedItem(requestedFeedItemUUID, requestedFeedItemType, 0, 0);
                    }
                }
            }
        }
        if (feedItem != null && feedItem.isPrivate()) {
            loadPrivateCards();
        }

        initializeCommentEditText();
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

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (isBound) {
            tourService.unregisterTourServiceListener(this);
        }
        doUnbindService();

        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                List<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (!textMatchList.isEmpty()) {
                    if (commentEditText.getText().toString().equals("")) {
                        commentEditText.setText(textMatchList.get(0));
                    } else {
                        commentEditText.setText(commentEditText.getText() + " " + textMatchList.get(0));
                    }
                    commentEditText.setSelection(commentEditText.getText().length());
                    EntourageEvents.logEvent(Constants.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_OK);
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (requestCode == READ_CONTACTS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onInviteContactsClicked();
                    }
                });
            } else {
                Toast.makeText(getActivity(), R.string.invite_contacts_permission_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        //setup scroll listener
        discussionView.addOnScrollListener(discussionScrollListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        discussionView.removeOnScrollListener(discussionScrollListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        inviteSuccessRunnable.run();
        inviteSuccessHandler.removeCallbacks(inviteSuccessRunnable);
    }

    @Override
    protected ColorDrawable getBackgroundDrawable() {
        return new ColorDrawable(getResources().getColor(R.color.background));
    }

    public String getFeedItemId() {
        if (feedItem != null) {
            return feedItem.getUUID();
        }
        return requestedFeedItemUUID;
    }

    public long getFeedItemType() {
        if (feedItem != null) {
            return feedItem.getType();
        }
        return requestedFeedItemType;
    }

    public void setShowInfoButton(final boolean showInfoButton) {
        this.showInfoButton = showInfoButton;
    }

    // ----------------------------------
    // Button Handling
    // ----------------------------------

    @OnClick(R.id.tour_info_close)
    protected void onCloseButton() {
        // If we are showing the public section and the feed item is private
        // switch to the private section
        // otherwise just close the view
        boolean isPublicSectionVisible = (publicSection.getVisibility() == View.VISIBLE);
        if (isPublicSectionVisible && (feedItem != null && feedItem.isPrivate())) {
            onSwitchSections();
            return;
        }
        this.dismiss();
    }

    @Optional
    @OnClick({R.id.tour_info_icon, R.id.tour_info_title, R.id.tour_card_arrow, R.id.tour_info_description_button})
    protected void onSwitchSections() {
        // Ignore if the entourage is not loaded or is public
        if (feedItem == null || !feedItem.isPrivate())
        {
            return;
        }

        // Hide the keyboard
        Activity activity = getActivity();
        if (activity != null) {
            View view = getDialog().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        // For conversation, open the author profile
        if (FeedItem.ENTOURAGE_CARD == feedItem.getType() && Entourage.TYPE_CONVERSATION.equalsIgnoreCase(((Entourage)feedItem).getGroupType())) {
            if (showInfoButton) {
                // only if this screen wasn't shown from the profile page
                BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(feedItem.getAuthor().getUserID()));
            }
            return;
        }

        // Switch sections
        boolean isPublicSectionVisible = (publicSection.getVisibility() == View.VISIBLE);
        publicSection.setVisibility(isPublicSectionVisible ? View.GONE : View.VISIBLE);
        privateSection.setVisibility(isPublicSectionVisible ?  View.VISIBLE : View.GONE);

        updateHeaderButtons();

        if (!isPublicSectionVisible) {
            EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_SWITCH_PUBLIC);
            EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_PUBLIC_VIEW_MEMBER);
        } else {
            EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_DISCUSSION_VIEW);
        }

    }

    @OnClick(R.id.tour_info_comment_send_button)
    protected void onAddCommentButton() {
        if (presenter != null) {
            commentEditText.setEnabled(false);
            commentSendButton.setEnabled(false);

            presenter.sendFeedItemMessage(commentEditText.getText().toString());
        }
    }

    @Optional
    @OnClick({R.id.tour_card_author, R.id.tour_card_photo})
    protected void onAuthorClicked() {
        BusProvider.getInstance().post(new Events.OnUserViewRequestedEvent(feedItem.getAuthor().getUserID()));
    }

    @OnClick(R.id.tour_info_comment_record_button)
    public void onRecord() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.encounter_leave_voice_message));
        try {
            EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_SPEECH);
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), getString(R.string.encounter_voice_message_not_supported), Toast.LENGTH_SHORT).show();
            EntourageEvents.logEvent(Constants.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_NOT_SUPPORTED);
        }
    }

    @Optional
    @OnClick({R.id.invite_source_share_button, R.id.feeditem_option_share})
    public void onShareButton() {
        // close the invite source view
        inviteSourceLayout.setVisibility(View.GONE);

        // check if the user is the author
        boolean isMyEntourage = false;
        User me = EntourageApplication.me(getContext());
        if (me != null && feedItem != null) {
            TourAuthor author = feedItem.getAuthor();
            if (author != null) {
                isMyEntourage = me.getId() == author.getUserID();
            }
        }

        // build the share text
        String shareLink = feedItem != null ? feedItem.getShareURL() : null;
        if (shareLink == null) {
            shareLink = getString(R.string.entourage_share_link);
        }
        int entourageShareText = isMyEntourage ? R.string.entourage_share_text_author : (feedItem.isPrivate() ? R.string.entourage_share_text_member : R.string.entourage_share_text_non_member);
        String shareText = getString(entourageShareText, shareLink);

        // start the share intent
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.entourage_share_intent_title)));

        EntourageEvents.logEvent((feedItem != null && feedItem.isPrivate()) ? Constants.EVENT_ENTOURAGE_SHARE_MEMBER : Constants.EVENT_ENTOURAGE_SHARE_NONMEMBER);
    }

    @Optional
    @OnClick(R.id.tour_info_more_button)
    public void onMoreButton() {

        if (feedItem == null) return;

        Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                R.anim.bottom_up);

        optionsLayout.startAnimation(bottomUp);
        optionsLayout.setVisibility(View.VISIBLE);

        EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_OPTIONS_OVERLAY);
    }

    @OnClick({R.id.feeditem_option_cancel, R.id.tour_info_options})
    public void onCloseOptionsButton() {
        Animation bottomDown = AnimationUtils.loadAnimation(getActivity(),
                R.anim.bottom_down);

        optionsLayout.startAnimation(bottomDown);
        optionsLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.feeditem_option_stop)
    public void onStopTourButton() {
        if (feedItem.getStatus().equals(FeedItem.STATUS_ON_GOING) || feedItem.getStatus().equals(FeedItem.STATUS_OPEN)) {
            if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
                Tour tour = (Tour)feedItem;
                //compute distance
                float distance = 0.0f;
                List<TourPoint> tourPointsList = tour.getTourPoints();
                if(tourPointsList.size()>1) {
                    TourPoint startPoint = tourPointsList.get(0);
                    for (int i = 1; i < tourPointsList.size(); i++) {
                        TourPoint p = tourPointsList.get(i);
                        distance += p.distanceTo(startPoint);
                        startPoint = p;
                    }
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
                    EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE);
                    mListener.showStopTourActivity(tour);
                }
            }
            else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
                EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE);
                //tourService.stopFeedItem(feedItem);
                //hide the options
                optionsLayout.setVisibility(View.GONE);
                //show close fragment
                if (getActivity() != null) {
                    FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();
                    EntourageCloseFragment entourageCloseFragment = EntourageCloseFragment.newInstance(feedItem);
                    entourageCloseFragment.show(fragmentManager, EntourageCloseFragment.TAG, getContext());
                }
            }
        }
        else if (feedItem.getType() == TimestampedObject.TOUR_CARD && feedItem.getStatus().equals(FeedItem.STATUS_CLOSED)) {
            if (tourService != null) {
                EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE);
                tourService.freezeTour((Tour)feedItem);
            }
        }
    }

    @OnClick(R.id.feeditem_option_quit)
    public void onQuitTourButton() {
        quitTour();
        /*
        if (getActivity() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int titleId = feedItem.getQuitDialogTitle();
        int messageId = feedItem.getQuitDialogMessage();
        builder.setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        quitTour();
                    }
                })
                .setNegativeButton(R.string.no, null);
        builder.create().show();
        */
    }

    private void quitTour() {
        if (tourService == null) {
            Toast.makeText(getActivity(), R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show();
        }
        else {
            User me = EntourageApplication.me(getActivity());
            if (me == null) {
                Toast.makeText(getActivity(), R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show();
            }
            else {
                EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_OPTIONS_QUIT);
                showProgressBar();
                tourService.removeUserFromFeedItem(feedItem, me.getId());
            }
        }
    }

    @OnClick({R.id.feeditem_option_join, R.id.feeditem_option_contact, R.id.tour_info_request_join_button})
    public void onJoinTourButton() {
        if (tourService != null) {
            showProgressBar();
            if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
                EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_ASK_JOIN);
                tourService.requestToJoinTour((Tour)feedItem);
            }
            else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
                EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_ASK_JOIN);
                tourService.requestToJoinEntourage((Entourage) feedItem);
            }
            else {
                hideProgressBar();
            }
        }
        else {
            Toast.makeText(getActivity(), R.string.tour_join_request_message_error, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.tour_info_act_button)
    public void onActButton() {
        if (feedItem == null) {
            return;
        }
        String joinStatus = feedItem.getJoinStatus();
        if (joinStatus.equals(Tour.JOIN_STATUS_PENDING)) {
            EntourageEvents.logEvent(Constants.EVENT_FEED_PENDING_OVERLAY);
        }
    }

    @OnClick(R.id.feeditem_option_edit)
    protected void onEditEntourageButton() {
        if (feedItem == null || getActivity() == null) return;

        if (feedItem.showEditEntourageView()) {
            if (getFragmentManager() != null) {
                CreateEntourageFragment fragment = CreateEntourageFragment.newInstance((Entourage) feedItem);
                fragment.show(getFragmentManager(), CreateEntourageFragment.TAG);
            }
            //hide the options
            optionsLayout.setVisibility(View.GONE);
        } else {
            // just send an email
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            // Set the email to
            String[] addresses = {getString(R.string.edit_action_email)};
            intent.putExtra(Intent.EXTRA_EMAIL, addresses);
            // Set the subject
            String title = feedItem.getTitle();
            if (title == null) title = "";
            String emailSubject = getString(R.string.edit_entourage_email_title, title);
            intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
            String description = feedItem.getDescription();
            if (description == null) description = "";
            String emailBody = getString(R.string.edit_entourage_email_body, description);
            intent.putExtra(Intent.EXTRA_TEXT, emailBody);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                //hide the options
                optionsLayout.setVisibility(View.GONE);
                // Start the intent
                startActivity(intent);
            } else {
                // No Email clients
                Toast.makeText(getContext(), R.string.error_no_email, Toast.LENGTH_SHORT).show();
            }
        }

        EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_OPTIONS_EDIT);
    }

    @OnClick(R.id.feeditem_option_report)
    protected void onReportEntourageButton() {
        if (feedItem == null || getActivity() == null) return;
        // Build the email intent
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        // Set the email to
        String[] addresses = {getString(R.string.contact_email)};
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        // Set the subject
        String title = feedItem.getTitle();
        if (title == null) title = "";
        String emailSubject = getString(R.string.report_entourage_email_title, title);
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            //hide the options
            optionsLayout.setVisibility(View.GONE);
            // Start the intent
            startActivity(intent);
        } else {
            // No Email clients
            Toast.makeText(getContext(), R.string.error_no_email, Toast.LENGTH_SHORT).show();
        }
    }

    @Optional
    @OnClick(R.id.feeditem_option_promote)
    protected void onPromoteEntourageButton() {
        if (feedItem == null || getActivity() == null) return;
        // Build the email intent
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        // Set the email to
        String[] addresses = {getString(R.string.contact_email)};
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        // Set the subject
        String title = feedItem.getTitle();
        if (title == null) title = "";
        String emailSubject = getString(R.string.promote_entourage_email_title, title);
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        // Set the body
        String emailBody = getString(R.string.promote_entourage_email_body, title);
        intent.putExtra(Intent.EXTRA_TEXT, emailBody);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            //hide the options
            optionsLayout.setVisibility(View.GONE);
            // Start the intent
            startActivity(intent);
        } else {
            // No Email clients
            Toast.makeText(getContext(), R.string.error_no_email, Toast.LENGTH_SHORT).show();
        }
    }

    protected void onUserAddClicked() {
        if (feedItem != null && feedItem.isSuspended()) {
            Toast.makeText(getContext(), R.string.tour_info_members_add_not_allowed, Toast.LENGTH_SHORT).show();
            return;
        }
        EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_INVITE_FRIENDS);
        showInviteSource();
    }

    @Optional
    @OnClick(R.id.tour_info_member_add)
    protected void onMembersAddClicked() {
        if (feedItem == null) return;
        if (feedItem.isPrivate() && Configuration.getInstance().showInviteView()) {
            // For members show the invite screen
            onUserAddClicked();
        }
        else {
            // For non-members, show the share screen
            onShareButton();
        }
    }

    private void showInviteSource() {
        inviteSourceLayout.setVisibility(View.VISIBLE);
        TextView inviteDescription = inviteSourceLayout.findViewById(R.id.invite_source_description);
        if (inviteDescription != null) {
            if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
                inviteDescription.setText(Entourage.TYPE_OUTING.equalsIgnoreCase(((Entourage)feedItem).getGroupType()) ? R.string.invite_source_description_outing : R.string.invite_source_description);
            } else {
                inviteDescription.setText(R.string.invite_source_description);
            }
        }
    }

    @OnClick({R.id.invite_source_close_button, R.id.invite_source_close_bottom_button})
    protected void onCloseInviteSourceClicked() {
        EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_INVITE_CLOSE);
        inviteSourceLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.invite_source_contacts_button)
    protected void onInviteContactsClicked() {
        if (getActivity() == null) return;
        EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_INVITE_CONTACTS);
        // check the permissions
        if (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_CODE);
            return;
        }
        // close the invite source view
        inviteSourceLayout.setVisibility(View.GONE);
        // open the contacts fragment
        if (getFragmentManager() != null) {
            InviteContactsFragment fragment = InviteContactsFragment.newInstance(feedItem.getUUID(), feedItem.getType());
            fragment.show(getFragmentManager(), InviteContactsFragment.TAG);
            // set the listener
            fragment.setInviteFriendsListener(this);
        }
    }

    @OnClick(R.id.invite_source_number_button)
    protected void onInvitePhoneNumberClicked() {
        if (getFragmentManager() == null) return;
        EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_INVITE_PHONE);
        // close the invite source view
        inviteSourceLayout.setVisibility(View.GONE);
        // open the contacts fragment
        InviteByPhoneNumberFragment fragment = InviteByPhoneNumberFragment.newInstance(feedItem.getUUID(), feedItem.getType());
        fragment.show(getFragmentManager(), InviteByPhoneNumberFragment.TAG);
        // set the listener
        fragment.setInviteFriendsListener(this);
    }

    @OnClick(R.id.tour_info_invited_accept_button)
    protected void onAcceptInvitationClicked(View view) {
        if (presenter != null) {
            view.setEnabled(false);
            presenter.acceptInvitation(invitationId);
        }
    }

    @OnClick(R.id.tour_info_invited_reject_button)
    protected void onRejectInvitationClicked(View view) {
        if (presenter != null) {
            view.setEnabled(false);
            presenter.rejectInvitation(invitationId);
        }
    }

    // ----------------------------------
    // Chat push notification
    // ----------------------------------

    public boolean onPushNotificationChatMessageReceived(Message message) {
        //we received a chat notification
        //check if it is referring to this feed item
        PushNotificationContent content = message.getContent();
        if (content == null || feedItem == null) {
            return false;
        }
        if (content.isTourRelated() && feedItem.getType() == FeedItem.ENTOURAGE_CARD) {
            return false;
        }
        if (content.isEntourageRelated() && feedItem.getType() == FeedItem.TOUR_CARD) {
            return false;
        }
        if (content.getJoinableId() != feedItem.getId()) {
            return false;
        }
        //retrieve the last messages from server
        if (presenter != null) {
            scrollToLastCard = true;
            presenter.getFeedItemMessages();
        }
        return true;
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeView() {

        apiRequestsCount = 0;

        // Hide the loading view
        loadingView.setVisibility(View.GONE);

        updateFeedItemInfo();

        if (invitationId > 0) {
            // already a member
            // send a silent accept
            if (presenter != null) {
                acceptInvitationSilently = true;
                presenter.acceptInvitation(invitationId);
            }
            // ignore the invitation
            invitationId = 0;
        }

        // switch to appropiate section
        if (feedItem.isPrivate()) {
            updateJoinStatus();
            switchToPrivateSection();
        }
        else {
            switchToPublicSection();
        }

        // check if we are opening an invitation
        invitedLayout.setVisibility(invitationId == 0 ? View.GONE : View.VISIBLE);

        // update the scroll list layout
        updatePublicScrollViewLayout();

        // for newly created entourages, open the invite friends screen automatically if the feed item is not suspended
        if (feedItem.isNewlyCreated() && feedItem.showInviteViewAfterCreation() && !feedItem.isSuspended()) {
            showInviteSource();
        }

        // check if we need to display the carousel
        User me = EntourageApplication.me(getContext());
        if (me != null && me.isOnboardingUser()) {
            showCarousel();
            me.setOnboardingUser(false);
            if (getActivity() != null) {
                EntourageComponent entourageComponent = EntourageApplication.get(getActivity()).getEntourageComponent();
                if (entourageComponent != null && entourageComponent.getAuthenticationController() != null) {
                    entourageComponent.getAuthenticationController().saveUser(me);
                }
            }
        }

    }

    private void initializeOptionsView() {
        User me = EntourageApplication.me(getActivity());

        Button stopTourButton = optionsLayout.findViewById(R.id.feeditem_option_stop);
        Button quitTourButton = optionsLayout.findViewById(R.id.feeditem_option_quit);
        Button editEntourageButton = optionsLayout.findViewById(R.id.feeditem_option_edit);
        Button shareEntourageButton = optionsLayout.findViewById(R.id.feeditem_option_share);
        Button reportEntourageButton = optionsLayout.findViewById(R.id.feeditem_option_report);
        Button joinEntourageButton = optionsLayout.findViewById(R.id.feeditem_option_join);
        Button contactTourButton = optionsLayout.findViewById(R.id.feeditem_option_contact);
        Button promoteEntourageButton = optionsLayout.findViewById(R.id.feeditem_option_promote);
        stopTourButton.setVisibility(View.GONE);
        quitTourButton.setVisibility(View.GONE);
        editEntourageButton.setVisibility(View.GONE);
        shareEntourageButton.setVisibility(View.GONE);
        reportEntourageButton.setVisibility(View.GONE);
        joinEntourageButton.setVisibility(View.GONE);
        contactTourButton.setVisibility(View.GONE);
        if (promoteEntourageButton != null) promoteEntourageButton.setVisibility(View.GONE);

        if (feedItem != null) {
            boolean hideJoinButton = feedItem.isPrivate() || FeedItem.JOIN_STATUS_PENDING.equals(feedItem.getJoinStatus()) || feedItem.isFreezed();
            if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
                joinEntourageButton.setVisibility(View.GONE);
                contactTourButton.setVisibility(hideJoinButton ? View.GONE : View.VISIBLE);
            } else {
                contactTourButton.setVisibility(View.GONE);
                joinEntourageButton.setVisibility(hideJoinButton ? View.GONE : View.VISIBLE);
            }
            if (me != null && feedItem.getAuthor() != null) {
                int myId = me.getId();
                if (feedItem.getAuthor().getUserID() != myId) {
                    if ((FeedItem.JOIN_STATUS_PENDING.equals(feedItem.getJoinStatus()) || FeedItem.JOIN_STATUS_ACCEPTED.equals(feedItem.getJoinStatus())) && !feedItem.isFreezed()) {
                        quitTourButton.setVisibility(View.VISIBLE);
                        quitTourButton.setText(FeedItem.JOIN_STATUS_PENDING.equals(feedItem.getJoinStatus()) ? R.string.tour_info_options_cancel_request : R.string.tour_info_options_quit_tour);
                    }
                    if (feedItem.getType() == FeedItem.ENTOURAGE_CARD) {
                        reportEntourageButton.setVisibility(View.VISIBLE);
                        // Share button available only for entourages and non-members
                        shareEntourageButton.setVisibility( feedItem.isPrivate() || feedItem.isSuspended() ? View.GONE : View.VISIBLE );
                    }
                } else {
                    stopTourButton.setVisibility(feedItem.isFreezed() || !feedItem.canBeClosed() ? View.GONE : View.VISIBLE);
                    if (feedItem.isClosed()) {
                        stopTourButton.setText(R.string.tour_info_options_freeze_tour);
                    } else {
                        stopTourButton.setText(feedItem.getType() == FeedItem.ENTOURAGE_CARD ? R.string.tour_info_options_freeze_tour : R.string.tour_info_options_stop_tour);
                    }
                    if (feedItem.getType() == FeedItem.ENTOURAGE_CARD && FeedItem.STATUS_OPEN.equals(feedItem.getStatus())) {
                        editEntourageButton.setVisibility(View.VISIBLE);
                    }
                }
                if (promoteEntourageButton != null && membersList != null && feedItem.getType() == FeedItem.ENTOURAGE_CARD) {
                    for (TimestampedObject member : membersList) {
                        if (!(member instanceof TourUser)) continue;
                        TourUser tourUser = (TourUser) member;
                        if (tourUser.getUserId() != myId) continue;
                        String role = tourUser.getRole();
                        if (role != null && role.equalsIgnoreCase("organizer")) {
                            promoteEntourageButton.setVisibility(View.VISIBLE);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void updateHeaderButtons() {
        if (feedItem == null) {
            return;
        }
        boolean isPublicSectionVisible = (publicSection.getVisibility() == View.VISIBLE);

        if (moreButton != null) moreButton.setVisibility(isPublicSectionVisible ? View.VISIBLE : View.GONE);

        if (descriptionButton != null) descriptionButton.setVisibility(isPublicSectionVisible || !showInfoButton ? View.GONE : View.VISIBLE);

        if (invitationId > 0) {
            if (moreButton != null) moreButton.setVisibility(View.GONE);
        }
    }

    private void updatePublicScrollViewLayout() {
        if (getView() == null) return;
        ScrollView scrollView = getView().findViewById(R.id.tour_info_public_scrollview);
        if (scrollView == null) return;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)scrollView.getLayoutParams();
        int oldRule = lp.getRules()[RelativeLayout.ABOVE];
        int newRule = actLayout.getId();
        if (invitedLayout.getVisibility() == View.VISIBLE) {
            newRule = invitedLayout.getId();
        }
        else if (requestJoinLayout.getVisibility() == View.VISIBLE) {
            newRule = requestJoinLayout.getId();
        }
        if (oldRule != newRule) {
            lp.addRule(RelativeLayout.ABOVE, newRule);
            scrollView.setLayoutParams(lp);
            scrollView.forceLayout();
        }
    }

    private void initializeDiscussionList() {

        //init the recycler view
        discussionView.setLayoutManager(new LinearLayoutManager(getContext()));
        discussionAdapter = new DiscussionAdapter();
        discussionView.setAdapter(discussionAdapter);

        //add the cards
        List<TimestampedObject> cachedCardInfoList = feedItem.getCachedCardInfoList();
        if (cachedCardInfoList != null) {
            discussionAdapter.addItems(cachedCardInfoList);
        }

        //clear the added cards info
        feedItem.clearAddedCardInfoList();

        if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
            Date now = new Date();
            //add the start time
            if (FeedItem.STATUS_ON_GOING.equals(feedItem.getStatus())) {
                addDiscussionTourStartCard(now);
            }

            //check if we need to add the Tour closed card
            if (feedItem.isClosed()) {
                addDiscussionTourEndCard(now);
            }
        }

        //scroll to last card
        scrollToLastCard();

        //find the oldest chat message received
        initOldestChatMessageDate();
    }

    private void initOldestChatMessageDate() {
        List<TimestampedObject> cachedCardInfoList = feedItem.getCachedCardInfoList();
        for (final TimestampedObject timestampedObject : cachedCardInfoList) {
            if (timestampedObject.getClass() != ChatMessage.class) continue;
            ChatMessage chatMessage = (ChatMessage) timestampedObject;
            Date chatCreationDate = chatMessage.getCreationDate();
            if (chatCreationDate == null) continue;
            if (oldestChatMessageDate == null) {
                oldestChatMessageDate = chatCreationDate;
            } else {
                if (chatCreationDate.before(oldestChatMessageDate)) {
                    oldestChatMessageDate = chatCreationDate;
                }
            }
        }
    }

    private void initializeMap() {
        if (!isAdded()) return;
        if (mapFragment == null) {
            GoogleMapOptions googleMapOptions = new GoogleMapOptions();
            googleMapOptions.zOrderOnTop(true);
            mapFragment = SupportMapFragment.newInstance(googleMapOptions);
        }
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.tour_info_map_layout, mapFragment).commitAllowingStateLoss();

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);

                if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
                    Tour tour = (Tour)feedItem;
                    List<TourPoint> tourPoints = tour.getTourPoints();
                    if (tourPoints != null && tourPoints.size() > 0) {
                        //setup the camera position to starting point
                        TourPoint startPoint = tourPoints.get(0);
                        CameraPosition cameraPosition = new CameraPosition(new LatLng(startPoint.getLatitude(), startPoint.getLongitude()), EntourageLocation.INITIAL_CAMERA_FACTOR_ENTOURAGE_VIEW, 0, 0);
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
                else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
                    TourPoint startPoint = feedItem.getStartPoint();
                    if (startPoint != null) {
                        LatLng position = startPoint.getLocation();

                        // move camera
                        CameraPosition cameraPosition = new CameraPosition(new LatLng(startPoint.getLatitude(), startPoint.getLongitude()), EntourageLocation.INITIAL_CAMERA_FACTOR_ENTOURAGE_VIEW, 0, 0);
                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        if (feedItem.showHeatmapAsOverlay()) {
                            // add heatmap
                            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(feedItem.getHeatmapResourceId());
                            GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions()
                                    .image(icon)
                                    .position(position, Entourage.HEATMAP_SIZE, Entourage.HEATMAP_SIZE)
                                    .clickable(true)
                                    .anchor(0.5f, 0.5f);

                            googleMap.addGroundOverlay(groundOverlayOptions);
                        } else {
                            // add marker
                            Drawable drawable = getResources().getDrawable(feedItem.getHeatmapResourceId());
                            BitmapDescriptor icon = Utils.getBitmapDescriptorFromDrawable(drawable, Entourage.getMarkerSize(getContext()), Entourage.getMarkerSize(getContext()));
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .icon(icon)
                                    .position(position)
                                    .draggable(false)
                                    .anchor(0.5f, 0.5f);
                            googleMap.addMarker(markerOptions);
                        }
                    }
                }
            }
        });
    }

    private void initializeHiddenMap() {
        if (!isAdded()) return;
        if (hiddenMapFragment == null) {
            GoogleMapOptions googleMapOptions = new GoogleMapOptions();
            googleMapOptions.zOrderOnTop(true);
            hiddenMapFragment = SupportMapFragment.newInstance(googleMapOptions);
        }
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.tour_info_hidden_map_layout, hiddenMapFragment).commitAllowingStateLoss();
        hiddenMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                if (tourTimestampList.size() > 0) {
                    TourTimestamp tourTimestamp = tourTimestampList.get(0);
                    if (tourTimestamp.getTourPoint() != null) {
                        //put the pin
                        MarkerOptions pin = new MarkerOptions().position(tourTimestamp.getTourPoint().getLocation());
                        googleMap.addMarker(pin);
                        //move the camera
                        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(tourTimestamp.getTourPoint().getLocation(), MAP_SNAPSHOT_ZOOM);
                        googleMap.moveCamera(camera);
                    }
                }
                else {
                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(MAP_SNAPSHOT_ZOOM));
                }

                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        getMapSnapshot();
                    }
                });

                googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(final CameraPosition cameraPosition) {
                        if (takeSnapshotOnCameraMove) {
                            getMapSnapshot();
                            hiddenGoogleMap = null;
                        }
                    }
                });

                hiddenGoogleMap = googleMap;
            }
        });
    }

    private void getMapSnapshot() {
        if (hiddenGoogleMap == null) return;
        if (tourTimestampList.size() == 0) {
            hiddenGoogleMap = null;
            return;
        }
        final TourTimestamp tourTimestamp = tourTimestampList.get(0);
        isTakingSnapshot = true;
        //take the snapshot
        hiddenGoogleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(final Bitmap bitmap) {
                //save the snapshot
                mapSnapshot = bitmap;
                snapshotTaken(tourTimestamp);
                //signal it has finished taking the snapshot
                isTakingSnapshot = false;
                //check if we need more snapshots
                if (tourTimestampList.size() > 1) {
                    TourTimestamp nextTourTimestamp = tourTimestampList.get(1);
                    if (nextTourTimestamp.getTourPoint() != null) {
                        float distance = nextTourTimestamp.getTourPoint().distanceTo(tourTimestamp.getTourPoint());
                        VisibleRegion visibleRegion = hiddenGoogleMap.getProjection().getVisibleRegion();
                        LatLng nearLeft = visibleRegion.nearLeft;
                        LatLng nearRight = visibleRegion.nearRight;
                        float[] result = {0};
                        Location.distanceBetween(nearLeft.latitude, nearLeft.longitude, nearRight.latitude, nearRight.longitude, result);
                        takeSnapshotOnCameraMove = (distance < result[0]);

                        //put the pin
                        hiddenGoogleMap.clear();
                        MarkerOptions pin = new MarkerOptions().position(nextTourTimestamp.getTourPoint().getLocation());
                        hiddenGoogleMap.addMarker(pin);
                        //move the camera
                        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(nextTourTimestamp.getTourPoint().getLocation(), MAP_SNAPSHOT_ZOOM);
                        hiddenGoogleMap.moveCamera(camera);
                    }
                } else {
                    hiddenGoogleMap = null;
                }
                tourTimestampList.remove(tourTimestamp);
            }
        });
    }

    private void snapshotTaken(TourTimestamp tourTimestamp) {
        if (mapSnapshot == null || tourTimestamp == null) return;
        tourTimestamp.setSnapshot(mapSnapshot);
        discussionAdapter.updateCard(tourTimestamp);
    }

    private int getTrackColor(String type, Date date) {
        int color = Color.GRAY;
        if (getContext() == null) return color;
        color = ContextCompat.getColor(getContext(), Tour.getTypeColorRes(type));
        if (!MapEntourageFragment.isToday(date)) {
            return MapEntourageFragment.getTransparentColor(color);
        }

        return color;
    }

    private void initializeCommentEditText() {
        commentEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {}

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() > 0) {
                    commentRecordButton.setVisibility(View.GONE);
                    commentSendButton.setVisibility(View.VISIBLE);
                    if (!startedTypingMessage) {
                        EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_VIEW_WRITE_MESSAGE);
                        startedTypingMessage = true;
                    }
                } else {
                    commentRecordButton.setVisibility(View.VISIBLE);
                    commentSendButton.setVisibility(View.GONE);
                    startedTypingMessage = false;
                }
            }
        });

        commentEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (discussionView != null) {
                    final int lastVisibleItemPosition = ((LinearLayoutManager)discussionView.getLayoutManager()).findLastVisibleItemPosition();
                    discussionView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            discussionView.scrollToPosition(lastVisibleItemPosition);
                        }
                    }, 500);
                }
            }
        });
    }

    private void initializeMembersView() {
        if (membersAdapter == null) {
            // Initialize the recycler view
            membersView.setLayoutManager(new LinearLayoutManager(getContext()));
            membersAdapter = new MembersAdapter();
            membersView.setAdapter(membersAdapter);
        }

        // add the members
        membersAdapter.addItems(membersList);

        // Show the members count
        int membersCount = feedItem != null ? feedItem.getNumberOfPeople() : membersAdapter.getItemCount();
        membersCountTextView.setText(getString(R.string.tour_info_members_count, membersCount));

        // hide the 'invite a friend' for a tour
        if (getView() != null) {
            View memberAddView = getView().findViewById(R.id.tour_info_member_add);
            if (memberAddView != null) {
                memberAddView.setVisibility(feedItem != null && feedItem.getType() != TimestampedObject.TOUR_CARD ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void updateFeedItemInfo() {
        // Update the header
        if (fragmentTitle != null) fragmentTitle.setText(feedItem.getTitle());
        String iconURL = feedItem.getIconURL();
        if (iconURL != null) {
            Picasso.with(getContext()).cancelRequest(tourIcon);
            Picasso.with(getContext())
                    .load(iconURL)
                    .placeholder(R.drawable.ic_user_photo_small)
                    .transform(new CropCircleTransformation())
                    .into(tourIcon);
            tourIcon.setVisibility(View.VISIBLE);
        } else {
            Drawable iconDrawable = feedItem.getIconDrawable(getContext());
            if (iconDrawable == null) {
                tourIcon.setVisibility(View.GONE);
            } else {
                tourIcon.setVisibility(View.VISIBLE);
                tourIcon.setImageDrawable(iconDrawable);
            }
        }

        // update description
        tourDescription.setText(feedItem.getDescription());
        DeepLinksManager.linkify(tourDescription);

        // metadata
        updateMetadataView();
    }

    private void updateMetadataView() {
        View fragmentView = getView();
        if (fragmentView == null) return;
        View metadataView = fragmentView.findViewById(R.id.tour_info_metadata_layout);
        if (metadataView == null) return;
        // show the view only for outing
        boolean metadataVisible = false;
        BaseEntourage.Metadata metadata = null;
        if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
            metadata = ((Entourage)feedItem).getMetadata();
            metadataVisible = Entourage.TYPE_OUTING.equalsIgnoreCase(((Entourage)feedItem).getGroupType()) && (metadata != null);
        }
        metadataView.setVisibility(metadataVisible ? View.VISIBLE : View.GONE);
        if (!metadataVisible) return;
        // populate the data
        TextView organiser = fragmentView.findViewById(R.id.tour_info_metadata_organiser);
        if (organiser != null && feedItem.getAuthor() != null) {
            organiser.setText(getString(R.string.tour_info_metadata_organiser_format, feedItem.getAuthor().getUserName()));
        }
        TextView metadataDate = fragmentView.findViewById(R.id.tour_info_metadata_date);
        if (metadataDate != null) metadataDate.setText(metadata.getStartDateAsString(getContext()));
        TextView metadateTime = fragmentView.findViewById(R.id.tour_info_metadata_time);
        if (metadateTime != null) metadateTime.setText(metadata.getStartTimeAsString(getContext()));
        TextView metadataAddress = fragmentView.findViewById(R.id.tour_info_metadata_address);
        if (metadataAddress != null) metadataAddress.setText(metadata.getDisplayAddress());
    }

    private void switchToPublicSection() {
        actLayout.setVisibility(View.VISIBLE);
        publicSection.setVisibility(View.VISIBLE);
        privateSection.setVisibility(View.GONE);

        updateHeaderButtons();
        initializeOptionsView();
        updateJoinStatus();

        initializeMap();
        initializeMembersView();

        if (feedItem != null && feedItem.isPrivate()) {
            EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_PUBLIC_VIEW_MEMBER);
        } else {
            EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_PUBLIC_VIEW_NONMEMBER);
        }
    }

    private void switchToPrivateSection() {
        actLayout.setVisibility(feedItem.isFreezed() ? View.VISIBLE : View.GONE);
        requestJoinLayout.setVisibility(View.GONE);
        publicSection.setVisibility(View.GONE);
        privateSection.setVisibility(View.VISIBLE);
        if (mapFragment == null) {
            initializeMap();
        }

        if (hiddenMapFragment == null) {
            initializeHiddenMap();
        }

        updateHeaderButtons();
        initializeOptionsView();

        //hide the comment section if the user is not accepted or tour is freezed
        if (!feedItem.getJoinStatus().equals(FeedItem.JOIN_STATUS_ACCEPTED) || feedItem.isFreezed()) {
            commentLayout.setVisibility(View.GONE);
            discussionView.setBackgroundResource(R.color.background_login_grey);
        }
        else {
            commentLayout.setVisibility(View.VISIBLE);
            discussionView.setBackgroundResource(R.color.background);
        }

        initializeDiscussionList();
        initializeMembersView();

        EntourageEvents.logEvent(Constants.EVENT_ENTOURAGE_DISCUSSION_VIEW);
    }

    private void loadPrivateCards() {
        if (presenter != null) {
            presenter.getFeedItemJoinRequests();
            presenter.getFeedItemMembers();
            presenter.getFeedItemMessages();
            if (feedItem != null && feedItem.isMine()) {
                presenter.getFeedItemEncounters();
            }
        }
    }

    private void updateJoinStatus() {
        if (feedItem == null) return;

        int dividerColor = R.color.accent;
        int textColor = R.color.accent;

        requestJoinLayout.setVisibility(View.GONE);
        actLayout.setVisibility(View.VISIBLE);

        actButton.setPadding(0, 0, 0, 0);
        if (Build.VERSION.SDK_INT >= 16) {
            actButton.setPaddingRelative(0, 0, 0, 0);
        }

        if (feedItem.isFreezed()) {
            // MI: Instead of hiding it, display the freezed text
            //actButton.setEnabled(false);
            actButton.setTextColor(getResources().getColor(feedItem.getFreezedCTAColor()));
            actButton.setText(feedItem.getFreezedCTAText());
            actButton.setPadding(getResources().getDimensionPixelOffset(R.dimen.act_button_right_padding), 0, 0, 0);
            if (Build.VERSION.SDK_INT >= 16) {
                actButton.setPaddingRelative(getResources().getDimensionPixelOffset(R.dimen.act_button_right_padding), 0, 0, 0);
            }
            actButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
        else {
            String joinStatus = feedItem.getJoinStatus();
            switch (joinStatus) {
                case Tour.JOIN_STATUS_PENDING:
                    actButton.setEnabled(true);
                    actButton.setText(R.string.tour_cell_button_pending);
                    actButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    break;
                case Tour.JOIN_STATUS_ACCEPTED:
                    actButton.setPadding(0, 0, getResources().getDimensionPixelOffset(R.dimen.act_button_right_padding), 0);
                    if (Build.VERSION.SDK_INT >= 16) {
                        actButton.setPaddingRelative(0, 0, getResources().getDimensionPixelOffset(R.dimen.act_button_right_padding), 0);
                    }
                    actButton.setEnabled(false);
                    actButton.setText(R.string.tour_cell_button_accepted);
                    actButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    break;
                case Tour.JOIN_STATUS_REJECTED:
                    actButton.setEnabled(false);
                    actButton.setText(R.string.tour_cell_button_rejected);
                    actButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    textColor = R.color.tomato;
                    break;
                default:
                    // Different layout for requesting to join
                    actLayout.setVisibility(View.INVISIBLE);
                    requestJoinLayout.setVisibility(View.VISIBLE);
                    requestJoinTitle.setText(feedItem.getJoinRequestTitle());
                    requestJoinButton.setText(feedItem.getJoinRequestButton());
                    updatePublicScrollViewLayout();
                    return;
            }
            actButton.setTextColor(getResources().getColor(textColor));

            actDividerLeft.setBackgroundResource(dividerColor);
            actDividerRight.setBackgroundResource(dividerColor);
        }

        updatePublicScrollViewLayout();
    }

    private void updateDiscussionList() {
        updateDiscussionList(true);
    }

    private void updateDiscussionList(boolean scrollToLastCard) {

        if (discussionAdapter == null || feedItem == null) return;

        List<TimestampedObject> addedCardInfoList = feedItem.getAddedCardInfoList();
        if (addedCardInfoList == null || addedCardInfoList.size() == 0) {
            return;
        }
        for (int i = 0; i < addedCardInfoList.size(); i++) {

            TimestampedObject cardInfo = addedCardInfoList.get(i);
            discussionAdapter.addCardInfoAfterTimestamp(cardInfo);

        }

        //clear the added cards info
        feedItem.clearAddedCardInfoList();

        if (scrollToLastCard) {
            //scroll to last card
            scrollToLastCard();
        }
    }

    private void addDiscussionTourStartCard(Date now) {
        long duration = 0;
        float distance = 0;
        if (feedItem.getStartTime() != null && !feedItem.isClosed()) {
            duration = now.getTime() - feedItem.getStartTime().getTime();
        }

        TourPoint startPoint = feedItem.getStartPoint();
        TourTimestamp tourTimestamp = new TourTimestamp(
                feedItem.getStartTime(),
                now,
                feedItem.getType(),
                FeedItem.STATUS_ON_GOING,
                startPoint,
                duration,
                distance
        );
        discussionAdapter.addCardInfo(tourTimestamp);
    }

    private void addDiscussionTourEndCard(Date now) {
        long duration = 0;
        float distance = 0;
        if (feedItem.getStartTime() != null && feedItem.getEndTime() != null) {
            duration = feedItem.getEndTime().getTime() - feedItem.getStartTime().getTime();
        }
        TourPoint endPoint = feedItem.getEndPoint();
        if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
            Tour tour = (Tour) feedItem;
            List<TourPoint> tourPointsList = tour.getTourPoints();
            if (tourPointsList.size() > 1) {
                TourPoint startPoint = tourPointsList.get(0);
                endPoint = tourPointsList.get(tourPointsList.size() - 1);
                for (int i = 1; i < tourPointsList.size(); i++) {
                    TourPoint p = tourPointsList.get(i);
                    distance += p.distanceTo(startPoint);
                    startPoint = p;
                }
            }

            TourTimestamp tourTimestamp = new TourTimestamp(
                    feedItem.getEndTime(),
                    feedItem.getEndTime() != null ? feedItem.getEndTime() : now,
                    feedItem.getType(),
                    FeedItem.STATUS_CLOSED,
                    endPoint,
                    duration,
                    distance
            );
            discussionAdapter.addCardInfoAfterTimestamp(tourTimestamp);
        } else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
            // Retrieve the latest chat messages, which should contain the close feed message
            if (presenter != null) presenter.getFeedItemMessages(oldestChatMessageDate);
        }
    }

    private void scrollToLastCard() {
        discussionView.scrollToPosition(discussionAdapter.getItemCount()-1);
    }

    private void setEncountersToReadOnly() {
        if (feedItem == null) return;
        List<TimestampedObject> encounters = feedItem.getTypedCardInfoList(TimestampedObject.ENCOUNTER);
        if (encounters == null || encounters.size() == 0) return;
        for (TimestampedObject timestampedObject: encounters) {
            Encounter encounter = (Encounter) timestampedObject;
            encounter.setReadOnly(true);
            discussionAdapter.updateCard(encounter);
        }
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
    // Bus handling
    // ----------------------------------

    @Subscribe
    public void onUserJoinRequestUpdateEvent(Events.OnUserJoinRequestUpdateEvent event) {
        if (presenter != null) {
            presenter.updateUserJoinRequest(event.getUserId(), event.getUpdate(), event.getFeedItem());
        }
    }

    @Subscribe
    public void onEntourageUpdated(Events.OnEntourageUpdated event) {
        Entourage updatedEntourage = event.getEntourage();
        if (updatedEntourage == null) return;
        // Check if it is our displayed entourage
        if (feedItem.getType() != updatedEntourage.getType() || feedItem.getId() != updatedEntourage.getId()) return;
        // Update the UI
        feedItem = updatedEntourage;

        updateFeedItemInfo();
    }

    @Subscribe
    public void onEncounterUpdated(Events.OnEncounterUpdated event) {
        Encounter updatedEncounter = event.getEncounter();
        if (updatedEncounter == null || feedItem == null) return;
        Encounter oldEncounter = (Encounter) discussionAdapter.findCard(TimestampedObject.ENCOUNTER, updatedEncounter.getId());
        if (oldEncounter == null) return;
        feedItem.removeCardInfo(oldEncounter);
        discussionAdapter.updateCard(updatedEncounter);
    }

    // ----------------------------------
    // API callbacks
    // ----------------------------------

    protected void onFeedItemReceived(FeedItem feedItem) {
        if (getActivity() == null || !isAdded()) return;

        hideProgressBar();
        if (feedItem != null) {
            this.feedItem = feedItem;
            initializeView();
            if (feedItem.isPrivate()) {
                loadPrivateCards();
            }
        } else {
            if (getContext() == null) return;
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.tour_info_error_retrieve_entourage);
            builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    dismiss();
                }
            });
            builder.create().show();
        }
    }

    protected synchronized void onFeedItemUsersReceived(List<TourUser> tourUsers, String context) {
        if (getActivity() == null || !isAdded()) return;

        if (tourUsers != null) {
            if (context == null) {
                // members
                onFeedItemMembersReceived(tourUsers);
                initializeOptionsView();
            } else {
                onFeedItemJoinRequestsReceived(tourUsers);
            }

        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList();
    }

    private void onFeedItemMembersReceived(List<TourUser> tourUsers) {
        membersList.clear();
        // iterate over the received users
        for (final TourUser tourUser : tourUsers) {
            if (tourUser == null) {
                continue;
            }
            //show only the accepted users
            if (!tourUser.getStatus().equals(FeedItem.JOIN_STATUS_ACCEPTED)) {
                // Remove the user from the members list, in case the user left the entourage
                membersAdapter.removeCard(tourUser);
                //show the pending and cancelled requests too (by skipping the others)
                if (!(tourUser.getStatus().equals(FeedItem.JOIN_STATUS_PENDING) || tourUser.getStatus().equals(FeedItem.JOIN_STATUS_CANCELLED))) {
                    continue;
                }
            }
            tourUser.setFeedItem(feedItem);
            tourUser.setDisplayedAsMember(true);
            membersList.add(tourUser);
        }

        initializeMembersView();
    }

    private void onFeedItemJoinRequestsReceived(List<TourUser> tourUsers) {
        List<TimestampedObject> timestampedObjectList = new ArrayList<>();
        // iterate over the received users
        for (final TourUser tourUser : tourUsers) {
            if (tourUser == null) {
                continue;
            }
            // skip the author
            if (tourUser.getUserId() == feedItem.getAuthor().getUserID()) {
                continue;
            }
            //show only the accepted users
            if (!tourUser.getStatus().equals(FeedItem.JOIN_STATUS_ACCEPTED)) {
                // Remove the user from the members list, in case the user left the entourage
                membersAdapter.removeCard(tourUser);
                //show the pending and cancelled requests too (by skipping the others)
                if (!(tourUser.getStatus().equals(FeedItem.JOIN_STATUS_PENDING) || tourUser.getStatus().equals(FeedItem.JOIN_STATUS_CANCELLED))) {
                    continue;
                }
            }
            tourUser.setFeedItem(feedItem);

            // check if we already have this user
            TourUser oldUser = (TourUser) discussionAdapter.findCard(tourUser);
            if (oldUser != null && !oldUser.getStatus().equals(tourUser.getStatus())) {
                discussionAdapter.updateCard(tourUser);
            } else {
                timestampedObjectList.add(tourUser);
            }
        }
        feedItem.addCardInfoList(timestampedObjectList);
    }

    protected void onFeedItemMessagesReceived(List<ChatMessage> chatMessageList) {
        if (getActivity() == null || !isAdded()) return;

        if (chatMessageList != null) {
            if (chatMessageList.size() > 0) {
                //check who sent the message
                AuthenticationController authenticationController = EntourageApplication.get(getActivity()).getEntourageComponent().getAuthenticationController();
                if (authenticationController.isAuthenticated()) {
                    int me = authenticationController.getUser().getId();
                    for (final ChatMessage chatMessage : chatMessageList) {
                        chatMessage.setIsMe(chatMessage.getUserId() == me);
                    }
                }

                List<TimestampedObject> timestampedObjectList = new ArrayList<>();
                timestampedObjectList.addAll(chatMessageList);
                if (feedItem.addCardInfoList(timestampedObjectList) > 0) {
                    //remember the last chat message
                    ChatMessage chatMessage = (ChatMessage)feedItem.getAddedCardInfoList().get(0);
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

    protected void onFeedItemMessageSent(ChatMessage chatMessage) {
        hideProgressBar();
        commentEditText.setEnabled(true);
        commentSendButton.setEnabled(true);

        if (chatMessage == null) {
            if(getContext()!=null) {
                Toast.makeText(getContext(), R.string.tour_info_error_chat_message, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        commentEditText.setText("");

        //hide the keyboard
        if (commentEditText.hasFocus() && getActivity()!=null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
        }

        //add the message to the list
        chatMessage.setIsMe(true);
        feedItem.addCardInfo(chatMessage);

        updateDiscussionList();
    }

    protected void onFeedItemEncountersReceived(List<Encounter> encounterList) {
        if (encounterList != null) {
            User me = EntourageApplication.me(getContext());
            if (me != null) {
                for (int i = 0; i < encounterList.size(); i++) {
                    Encounter encounter = encounterList.get(i);
                    encounter.setIsMyEncounter(encounter.getUserId() == me.getId());
                    encounter.setReadOnly(feedItem.isClosed());
                }
            }
            List<TimestampedObject> timestampedObjectList = new ArrayList<>();
            timestampedObjectList.addAll(encounterList);
            feedItem.addCardInfoList(timestampedObjectList);
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
        }
    }

    protected void onUserJoinRequestUpdated(int userId, String status, int error) {
        hideProgressBar();
        if (error == EntourageError.ERROR_NONE) {
            // Updated ok
            int messageId = R.string.tour_join_request_success;
            if (FeedItem.JOIN_STATUS_REJECTED.equals(status)) {
                messageId = R.string.tour_join_request_rejected;
            }
            Toast.makeText(getActivity(), messageId, Toast.LENGTH_SHORT).show();
            // Update the card
            TourUser card = (TourUser) discussionAdapter.findCard(TimestampedObject.TOUR_USER_JOIN, userId);
            if (card != null) {
                if (FeedItem.JOIN_STATUS_ACCEPTED.equals(status)) {
                    card.setStatus(FeedItem.JOIN_STATUS_ACCEPTED);
                    discussionAdapter.updateCard(card);
                    // Add the user to members list too
                    TourUser clone = card.clone();
                    clone.setDisplayedAsMember(true);
                    membersAdapter.addCardInfo(clone);
                }
                else {
                    // remove from the adapter
                    discussionAdapter.removeCard(card);
                    // remove from cached cards
                    feedItem.removeCardInfo(card);
                }
            }
            // Remove the push notification
            if (FeedItem.JOIN_STATUS_ACCEPTED.equals(status)) {
                EntourageApplication application = EntourageApplication.get();
                if (application != null) {
                    application.removePushNotification(feedItem, userId, PushNotificationContent.TYPE_NEW_JOIN_REQUEST);
                }
            }
        }
        else if (error == EntourageError.ERROR_BAD_REQUEST) {
            // Assume that the other user cancelled the request
            // Get the card
            TourUser card = (TourUser) discussionAdapter.findCard(TimestampedObject.TOUR_USER_JOIN, userId);
            if (card != null) {
                if (FeedItem.JOIN_STATUS_ACCEPTED.equals(status)) {
                    // Mark the user as accepted
                    card.setStatus(FeedItem.JOIN_STATUS_ACCEPTED);
                    discussionAdapter.updateCard(card);
                    // Add a quited card
                    TourUser clone = card.clone();
                    clone.setStatus(FeedItem.JOIN_STATUS_QUITED);
                    discussionAdapter.addCardInfoAfterTimestamp(clone);
                    // remove from cached cards
                    feedItem.removeCardInfo(card);
                }
                else if (FeedItem.JOIN_STATUS_REJECTED.equals(status)) {
                    // Mark the user as cancelled
                    card.setStatus(FeedItem.JOIN_STATUS_CANCELLED);
                    // Remove the message
                    card.setMessage("");
                    discussionAdapter.updateCard(card);
                    // remove from cached cards
                    feedItem.removeCardInfo(card);
                }
                else {
                    // remove from the adapter
                    discussionAdapter.removeCard(card);
                    // remove from cached cards
                    feedItem.removeCardInfo(card);
                }
            }
        }
        else if(getActivity()!=null){
            // Error
            Toast.makeText(getActivity(), R.string.tour_join_request_error, Toast.LENGTH_SHORT).show();
        }
    }

    protected void onInvitationStatusUpdated(boolean success, String status) {
        if (getView() == null) {
            return;
        }
        Button acceptInvitationButton = getView().findViewById(R.id.tour_info_invited_accept_button);
        Button rejectInvitationButton = getView().findViewById(R.id.tour_info_invited_reject_button);
        acceptInvitationButton.setEnabled(true);
        rejectInvitationButton.setEnabled(true);
        if (success) {
            invitationId = 0;
            if (acceptInvitationSilently) {
                acceptInvitationSilently = false;
            } else {
                // Update UI
                invitedLayout.setVisibility(View.GONE);
                Toast.makeText(getActivity(), R.string.invited_updated_ok, Toast.LENGTH_SHORT).show();
                if (Invitation.STATUS_ACCEPTED.equals(status)) {
                    // Invitation accepted, refresh the lists and status
                    if (feedItem != null) {
                        feedItem.setJoinStatus(FeedItem.JOIN_STATUS_ACCEPTED);
                        switchToPrivateSection();
                        loadPrivateCards();
                        updateHeaderButtons();
                        //actLayout.setVisibility(View.GONE);
                    }
                }
                updatePublicScrollViewLayout();
            }

            // Post an event
            BusProvider.getInstance().post(new Events.OnInvitationStatusChanged(this.feedItem, status));

        } else {
            if (!acceptInvitationSilently) {
                Toast.makeText(getActivity(), R.string.invited_updated_error, Toast.LENGTH_SHORT).show();
            }
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
    public void onTourCreated(final boolean created, final String tourUUID) {

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
        if (feedItem.getType() != TimestampedObject.TOUR_CARD) return;
        for (Tour receivedTour:tours) {
            if (receivedTour.getId() == this.feedItem.getId()) {
                if(!receivedTour.isSame((Tour)this.feedItem)) {
                    onFeedItemClosed(true, receivedTour);
                }
            }
        }
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
    public void onFeedItemClosed(final boolean closed, final FeedItem feedItem) {
        //ignore requests that are not related to our feed item
        if (this.feedItem.getType() != feedItem.getType()) return;
        if (feedItem.getId() != this.feedItem.getId()) return;

        if (closed) {
            this.feedItem.setStatus(feedItem.getStatus());
            this.feedItem.setEndTime(feedItem.getEndTime());
            if (feedItem.getStatus().equals(FeedItem.STATUS_CLOSED) && feedItem.isPrivate()) {
                addDiscussionTourEndCard(new Date());
                updateDiscussionList();
                setEncountersToReadOnly();
            }
            if (feedItem.isFreezed()){
                commentLayout.setVisibility(View.GONE);
            }
            optionsLayout.setVisibility(View.GONE);
            initializeOptionsView();
            updateHeaderButtons();
            updateJoinStatus();
        }
        else {
            Toast.makeText(getActivity(), R.string.tour_close_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationProviderStatusChanged(final boolean active) {

    }

    @Override
    public void onUserStatusChanged(final TourUser user, final FeedItem feedItem) {
        //ignore requests that are not related to our feed item
        if (feedItem == null || this.feedItem == null) return;
        if (feedItem.getType() != this.feedItem.getType()) return;
        if (feedItem.getId() != this.feedItem.getId()) return;

        hideProgressBar();

        //check for errors
        if (user == null) {
            Toast.makeText(getActivity(), R.string.tour_info_request_error, Toast.LENGTH_SHORT).show();
            return;
        }

        //close the overlay
        onCloseOptionsButton();

        //update the local tour info
        boolean oldPrivateStatus = (privateSection.getVisibility() == View.VISIBLE);
        feedItem.setJoinStatus(user.getStatus());
        boolean currentPrivateStatus = feedItem.isPrivate();
        //update UI
        if (oldPrivateStatus != currentPrivateStatus) {
            if (feedItem.isPrivate()) {
                switchToPrivateSection();
                loadPrivateCards();
            }
            else {
                switchToPublicSection();
            }
        }
        else {
            updateHeaderButtons();
            initializeOptionsView();
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
                if (timestamp != null && oldestChatMessageDate != null && timestamp.before(oldestChatMessageDate)) {
                    presenter.getFeedItemMessages(oldestChatMessageDate);
                }
                scrollDeltaY = 0;
            }
        }

        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
        }
    }

    // ----------------------------------
    // InviteFriendsListener
    // ----------------------------------

    @Override
    public void onInviteSent() {
        // Show the success layout
        inviteSuccessLayout.setVisibility(View.VISIBLE);
        // Start the timer to hide the success layout
        inviteSuccessHandler.postDelayed(inviteSuccessRunnable, Constants.INVITE_SUCCESS_HIDE_DELAY);
    }

    // ----------------------------------
    // CAROUSEL
    // ----------------------------------

    private void showCarousel() {
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if the activity is still running
                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }
                if (!isVisible()) {
                    return;
                }
                if (getFragmentManager() != null) {
                    CarouselFragment carouselFragment = new CarouselFragment();
                    carouselFragment.show(getFragmentManager(), CarouselFragment.TAG);
                }
            }
        }, Constants.CAROUSEL_DELAY_MILLIS);
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
                tourService.registerTourServiceListener(TourInformationFragment.this);
                isBound = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tourService.unregisterTourServiceListener(TourInformationFragment.this);
            tourService = null;
            isBound = false;
        }
    }
}
