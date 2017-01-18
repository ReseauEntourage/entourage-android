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
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourTimestamp;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.Events;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.invite.InviteFriendsListener;
import social.entourage.android.invite.contacts.InviteContactsFragment;
import social.entourage.android.invite.phonenumber.InviteByPhoneNumberFragment;
import social.entourage.android.map.MapEntourageFragment;
import social.entourage.android.map.entourage.CreateEntourageFragment;
import social.entourage.android.map.tour.TourService;
import social.entourage.android.map.tour.information.discussion.DiscussionAdapter;
import social.entourage.android.map.tour.information.members.MembersAdapter;
import social.entourage.android.tools.BusProvider;

public class TourInformationFragment extends DialogFragment implements TourService.TourServiceListener, InviteFriendsListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "fragment_tour_information";

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 2;
    private static final int READ_CONTACTS_PERMISSION_CODE = 3;

    private static final int SCROLL_DELTA_Y_THRESHOLD = 20;

    private static final int REQUEST_NONE = 0;
    private static final int REQUEST_QUIT_TOUR = 1;
    private static final int REQUEST_JOIN_TOUR = 2;

    private static final int MAP_SNAPSHOT_ZOOM = 15;

    private static final String KEY_FEEDITEM = "social.entourage.android.KEY_FEEDITEM";
    private static final String KEY_FEEDITEM_ID = "social.entourage.android.KEY_FEEDITEM_ID";
    private static final String KEY_FEEDITEM_TYPE = "social.entourage.android.KEY_FEEDITEM_TYPE";
    private static final String KEY_INVITATION_ID = "social.entourage.android_KEY_INVITATION_ID";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    TourInformationPresenter presenter;

    TourService tourService;
    private ServiceConnection connection = new ServiceConnection();
    private boolean isBound = false;

    @BindView(R.id.tour_info_title)
    TextView fragmentTitle;

    @BindView(R.id.tour_card_title)
    TextView tourOrganization;

    @BindView(R.id.tour_card_photo)
    ImageView tourAuthorPhoto;

    @BindView(R.id.tour_card_partner_logo)
    ImageView tourAuthorPartnerLogo;

    @BindView(R.id.tour_card_type)
    TextView tourType;

    @BindView(R.id.tour_card_author)
    TextView tourAuthorName;

    @BindView(R.id.tour_card_location)
    TextView tourLocation;

    @BindView(R.id.tour_card_people_count)
    TextView tourPeopleCount;

    @BindView(R.id.tour_card_people_image)
    ImageView tourPeopleImage;

    @BindView(R.id.tour_card_arrow)
    ImageView tourCardArrow;

    @BindView(R.id.tour_card_act_layout)
    RelativeLayout headerActLayout;

    @BindView(R.id.tour_info_description)
    TextView tourDescription;

    @BindView(R.id.tour_info_discussion_view)
    RecyclerView discussionView;

    DiscussionAdapter discussionAdapter;

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

    @BindView(R.id.tour_info_share_button)
    AppCompatImageButton shareButton;

    @BindView(R.id.tour_info_user_add_button)
    AppCompatImageButton addUserButton;

    @BindView(R.id.tour_info_more_button)
    AppCompatImageButton moreButton;

    @BindView(R.id.tour_info_act_layout)
    RelativeLayout actLayout;

    @BindView(R.id.tour_info_join_button)
    Button joinButton;

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

    MembersAdapter membersAdapter;
    List<TimestampedObject> membersList = new ArrayList<>();

    // Invitation Layout
    @BindView(R.id.tour_info_invited_layout)
    View invitedLayout;

    int apiRequestsCount;

    FeedItem feedItem;
    long requestedFeedItemId;
    int requestedFeedItemType;
    long invitationId;

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

    public static TourInformationFragment newInstance(FeedItem feedItem, long invitationId) {
        TourInformationFragment fragment = new TourInformationFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_FEEDITEM, feedItem);
        args.putLong(KEY_INVITATION_ID, invitationId);
        fragment.setArguments(args);
        return fragment;
    }

    public static TourInformationFragment newInstance(long feedId, int feedItemType, long invitationId) {
        TourInformationFragment fragment = new TourInformationFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_FEEDITEM_ID, feedId);
        args.putInt(KEY_FEEDITEM_TYPE, feedItemType);
        args.putLong(KEY_INVITATION_ID, invitationId);
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

        return toReturn;
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());

        feedItem = (FeedItem) getArguments().getSerializable(KEY_FEEDITEM);
        invitationId = getArguments().getLong(KEY_INVITATION_ID);
        if (feedItem != null) {
            initializeView();
        }
        else {
            requestedFeedItemId = getArguments().getLong(KEY_FEEDITEM_ID);
            requestedFeedItemType = getArguments().getInt(KEY_FEEDITEM_TYPE);
            if (requestedFeedItemType == TimestampedObject.TOUR_CARD || requestedFeedItemType == TimestampedObject.ENTOURAGE_CARD) {
                presenter.getFeedItem(requestedFeedItemId, requestedFeedItemType);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
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
                    FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_OK);
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
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.background)));

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

    public long getFeedItemId() {
        if (feedItem != null) {
            return feedItem.getId();
        }
        return requestedFeedItemId;
    }

    public long getFeedItemType() {
        if (feedItem != null) {
            return feedItem.getType();
        }
        return requestedFeedItemType;
    }

    // ----------------------------------
    // Button Handling
    // ----------------------------------

    @OnClick(R.id.tour_info_close)
    protected void onCloseButton() {
        this.dismiss();
    }

    @OnClick({R.id.tour_info_title, R.id.tour_card_arrow})
    protected void onSwitchSections() {
        // Ignore if the entourage is not loaded or is public
        if (feedItem == null || !feedItem.isPrivate())
        {
            return;
        }

        // Switch sections
        boolean isPublicSectionVisible = (publicSection.getVisibility() == View.VISIBLE);
        publicSection.setVisibility(isPublicSectionVisible ? View.GONE : View.VISIBLE);
        privateSection.setVisibility(isPublicSectionVisible ?  View.VISIBLE : View.GONE);

        if (!isPublicSectionVisible) {
            FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_SWITCH_PUBLIC);
        }
    }

    @OnClick(R.id.tour_info_comment_send_button)
    protected void onAddCommentButton() {
        if (presenter != null) {
            presenter.sendFeedItemMessage(commentEditText.getText().toString());
        }
    }

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
            FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_SPEECH);
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), getString(R.string.encounter_voice_message_not_supported), Toast.LENGTH_SHORT).show();
            FlurryAgent.logEvent(Constants.EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_NOT_SUPPORTED);
        }
    }

    @OnClick({R.id.tour_info_more_button, R.id.tour_info_share_button})
    public void onMoreButton() {
        Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                R.anim.bottom_up);

        optionsLayout.startAnimation(bottomUp);
        optionsLayout.setVisibility(View.VISIBLE);

        FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_OPTIONS_OVERLAY);
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
                    FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE);
                    mListener.showStopTourActivity(tour);
                }
            }
            else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
                FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE);
                tourService.stopFeedItem(feedItem);
            }
        }
        else if (feedItem.getType() == TimestampedObject.TOUR_CARD && feedItem.getStatus().equals(FeedItem.STATUS_CLOSED)) {
            if (tourService != null) {
                FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE);
                tourService.freezeTour((Tour)feedItem);
            }
        }
    }

    @OnClick(R.id.feeditem_option_quit)
    public void onQuitTourButton() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int titleId = R.string.tour_info_quit_tour_title;
        int messageId = R.string.tour_info_quit_tour_description;
        if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
            titleId = R.string.entourage_info_quit_entourage_title;
            messageId = R.string.entourage_info_quit_entourage_description;
        }
        builder.setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (tourService == null) {
                            Toast.makeText(getActivity(), R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            User me = EntourageApplication.me(getActivity());
                            if (me == null) {
                                Toast.makeText(getActivity(), R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_OPTIONS_QUIT);
                                showProgressBar();
                                tourService.removeUserFromFeedItem(feedItem, me.getId());
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.no, null);
        builder.create().show();
    }

    @OnClick({R.id.tour_info_join_button, R.id.feeditem_option_join})
    public void onJoinTourButton() {
        if (tourService != null) {
            showProgressBar();
            if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
                FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_ASK_JOIN);
                tourService.requestToJoinTour((Tour)feedItem);
            }
            else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
                FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_ASK_JOIN);
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

    @OnClick(R.id.feeditem_option_edit)
    protected void onEditEntourageButton() {
        CreateEntourageFragment fragment = CreateEntourageFragment.newInstance((Entourage)feedItem);
        fragment.show(getFragmentManager(), CreateEntourageFragment.TAG);

        //hide the options
        optionsLayout.setVisibility(View.GONE);

        FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_OPTIONS_EDIT);
    }

    @OnClick(R.id.feeditem_option_report)
    protected void onReportEntourageButton() {
        if (feedItem == null) return;
        // Build the email intent
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        // Set the email to
        String[] addresses = {Constants.EMAIL_CONTACT};
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        // Set the subject
        String title = feedItem.getTitle();
        if (title == null) title = "";
        String name = "Unknown";
        if (feedItem.getAuthor() != null) {
            name = feedItem.getAuthor().getUserName();
            if (name == null) name = "Unknown";
        }
        String emailSubject = getString(R.string.report_entourage_email_title, title, name);
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

    @OnClick(R.id.tour_info_user_add_button)
    protected void onUserAddClicked() {
        FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_INVITE_FRIENDS);
        inviteSourceLayout.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.invite_source_close_button, R.id.invite_source_close_bottom_button})
    protected void onCloseInviteSourceClicked() {
        FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_INVITE_CLOSE);
        inviteSourceLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.invite_source_contacts_button)
    protected void onInviteContactsClicked() {
        FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_INVITE_CONTACTS);
        // check the permissions
        if (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION_CODE);
            return;
        }
        // close the invite source view
        inviteSourceLayout.setVisibility(View.GONE);
        // open the contacts fragment
        InviteContactsFragment fragment = InviteContactsFragment.newInstance(feedItem.getId(), feedItem.getType());
        fragment.show(getFragmentManager(), InviteContactsFragment.TAG);
        // set the listener
        fragment.setInviteFriendsListener(this);
    }

    @OnClick(R.id.invite_source_number_button)
    protected void onInvitePhoneNumberClicked() {
        FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_INVITE_PHONE);
        // close the invite source view
        inviteSourceLayout.setVisibility(View.GONE);
        // open the contacts fragment
        InviteByPhoneNumberFragment fragment = InviteByPhoneNumberFragment.newInstance(feedItem.getId(), feedItem.getType());
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
        if (content == null) {
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

        // Initialize the header
        if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
            fragmentTitle.setText(R.string.tour_info_title);
        }
        else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
            if (feedItem.getFeedType().equals(Entourage.TYPE_DEMAND)) {
                fragmentTitle.setText(R.string.entourage_type_demand);
            }
            else {
                fragmentTitle.setText(R.string.entourage_type_contribution);
            }
        }

        // Initialize the header
        tourOrganization.setText(feedItem.getTitle());

        String displayType = feedItem.getFeedTypeLong(this.getActivity());
        if (displayType != null) {
            tourType.setText(displayType);
        } else {
            tourType.setText(getString(R.string.tour_info_text_type_title, getString(R.string.tour_info_unknown)));
        }

        if (feedItem.getAuthor() != null) {
            tourAuthorName.setText(feedItem.getAuthor().getUserName());

            String avatarURLAsString = feedItem.getAuthor().getAvatarURLAsString();
            if (avatarURLAsString != null) {
                Picasso.with(getContext()).load(Uri.parse(avatarURLAsString))
                        .placeholder(R.drawable.ic_user_photo_small)
                        .transform(new CropCircleTransformation())
                        .into(tourAuthorPhoto);
            }
            //TODO partner logo
        } else {
            tourAuthorName.setText("--");
        }

        // MI: for v2.1 we display the distance to starting point
        /*
        String location = "";
        if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
            Address tourAddress = feedItem.getStartAddress();
            if (tourAddress != null) {
                location = tourAddress.getAddressLine(0);
                if (tourLocation == null) {
                    location = "";
                }
            }
        }
        else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
            TourPoint entourageLocation = ((Entourage)feedItem).getLocation();
            if (entourageLocation != null) {
                Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
                if (currentLocation != null) {
                    float distance = entourageLocation.distanceTo(new TourPoint(currentLocation.getLatitude(), currentLocation.getLongitude()));
                    location = String.format("%.2f km", distance/1000.0f);
                }
            }
        }
        */

        String distanceAsString = "";
        TourPoint startPoint = null;
        if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
            startPoint = ((Tour)feedItem).getStartPoint();
        }
        else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
            startPoint = ((Entourage)feedItem).getLocation();
        }
        if (startPoint != null) {
            distanceAsString = startPoint.distanceToCurrentLocation();
        }

        tourLocation.setText(String.format(getResources().getString(R.string.tour_cell_location), Tour.getStringDiffToNow(feedItem.getStartTime()), distanceAsString));

        tourPeopleCount.setText("" + feedItem.getNumberOfPeople());

        headerActLayout.setVisibility(View.GONE);

        // update description
        tourDescription.setText(feedItem.getDescription());

        // switch to appropiate section
        if (feedItem.isPrivate()) {
            tourPeopleCount.setVisibility(View.INVISIBLE);
            tourPeopleImage.setVisibility(View.INVISIBLE);
            tourAuthorPhoto.setVisibility(View.INVISIBLE);
            tourAuthorPartnerLogo.setVisibility(View.INVISIBLE);
            tourCardArrow.setVisibility(View.VISIBLE);
            updateJoinStatus();
            switchToPrivateSection();
        }
        else {
            tourPeopleCount.setVisibility(View.VISIBLE);
            tourPeopleImage.setVisibility(View.VISIBLE);
            tourAuthorPhoto.setVisibility(View.VISIBLE);
            tourAuthorPartnerLogo.setVisibility(View.VISIBLE);
            tourCardArrow.setVisibility(View.GONE);
            switchToPublicSection();
        }

        // check if we are opening an invitation
        invitedLayout.setVisibility(invitationId == 0 ? View.GONE : View.VISIBLE);
    }

    private void initializeOptionsView() {
        User me = EntourageApplication.me(getActivity());

        Button stopTourButton = (Button)optionsLayout.findViewById(R.id.feeditem_option_stop);
        Button quitTourButton = (Button)optionsLayout.findViewById(R.id.feeditem_option_quit);
        Button editEntourageButton = (Button)optionsLayout.findViewById(R.id.feeditem_option_edit);
        Button reportEntourageButton = (Button)optionsLayout.findViewById(R.id.feeditem_option_report);
        Button joinEntourageButton = (Button)optionsLayout.findViewById(R.id.feeditem_option_join);
        stopTourButton.setVisibility(View.GONE);
        quitTourButton.setVisibility(View.GONE);
        editEntourageButton.setVisibility(View.GONE);
        reportEntourageButton.setVisibility(View.GONE);
        joinEntourageButton.setVisibility(View.GONE);

        if (feedItem != null) {
            joinEntourageButton.setVisibility(feedItem.isPrivate() ? View.GONE : (FeedItem.JOIN_STATUS_PENDING.equals(feedItem.getJoinStatus()) ? View.GONE : View.VISIBLE) );
            if (me != null && feedItem.getAuthor() != null) {
                int myId = me.getId();
                if (feedItem.getAuthor().getUserID() != myId) {
                    quitTourButton.setVisibility((FeedItem.JOIN_STATUS_PENDING.equals(feedItem.getJoinStatus()) || !feedItem.isPrivate() ? View.GONE : View.VISIBLE));
                    reportEntourageButton.setVisibility(View.VISIBLE);
                } else {
                    stopTourButton.setVisibility(feedItem.isFreezed() ? View.GONE : View.VISIBLE);
                    if (feedItem.isClosed()) {
                        stopTourButton.setText(R.string.tour_info_options_freeze_tour);
                    } else {
                        stopTourButton.setText(R.string.tour_info_options_stop_tour);
                    }
                    if (feedItem.getType() == FeedItem.ENTOURAGE_CARD && FeedItem.STATUS_OPEN.equals(feedItem.getStatus())) {
                        editEntourageButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    private void updateHeaderButtons() {
        boolean isTourPrivate = feedItem.isPrivate();
        User me = EntourageApplication.me(getActivity());
        int myId = 0;
        if (me != null) {
            myId = me.getId();
        }
        shareButton.setVisibility(isTourPrivate ? View.GONE : View.VISIBLE );

        addUserButton.setVisibility(isTourPrivate ? (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD && !feedItem.isClosed() ? View.VISIBLE : View.GONE) : View.GONE);

        moreButton.setVisibility(isTourPrivate ? View.VISIBLE : View.GONE);
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

                        // add heatmap
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.heat_zone);
                        GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions()
                                .image(icon)
                                .position(position, Entourage.HEATMAP_SIZE, Entourage.HEATMAP_SIZE)
                                .clickable(true)
                                .anchor(0.5f, 0.5f);

                        googleMap.addGroundOverlay(groundOverlayOptions);
                    }
                }
            }
        });
    }

    private void initializeHiddenMap() {
        if (hiddenMapFragment == null) {
            GoogleMapOptions googleMapOptions = new GoogleMapOptions();
            googleMapOptions.zOrderOnTop(true);
            hiddenMapFragment = SupportMapFragment.newInstance(googleMapOptions);
        }
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.tour_info_hidden_map_layout, hiddenMapFragment).commit();
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
                        }
                    }
                });

                hiddenGoogleMap = googleMap;
            }
        });
    }

    private boolean getMapSnapshot() {
        if (hiddenGoogleMap == null) return false;
        if (tourTimestampList.size() == 0) return true;
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
                }
                tourTimestampList.remove(tourTimestamp);
            }
        });
        return true;
    }

    private void snapshotTaken(TourTimestamp tourTimestamp) {
        if (mapSnapshot == null || tourTimestamp == null) return;
        tourTimestamp.setSnapshot(mapSnapshot);
        discussionAdapter.updateCard(tourTimestamp);
    }

    private int getTrackColor(String type, Date date) {
        int color = Color.GRAY;
        if (TourType.MEDICAL.getName().equals(type)) {
            //color = Color.RED;
            color = ContextCompat.getColor(getContext(), R.color.tour_type_medical);
        }
        else if (TourType.ALIMENTARY.getName().equals(type)) {
            //color = Color.BLUE;
            color = ContextCompat.getColor(getContext(), R.color.tour_type_distributive);
        }
        else if (TourType.BARE_HANDS.getName().equals(type)) {
            //color = Color.GREEN;
            color = ContextCompat.getColor(getContext(), R.color.tour_type_social);
        }
        if (!MapEntourageFragment.isToday(date)) {
            return MapEntourageFragment.getTransparentColor(color);
        }

        //return Color.argb(0, Color.red(color), Color.green(color), Color.blue(color));
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
                        FlurryAgent.logEvent(Constants.EVENT_ENTOURAGE_VIEW_WRITE_MESSAGE);
                        startedTypingMessage = true;
                    }
                } else {
                    commentRecordButton.setVisibility(View.VISIBLE);
                    commentSendButton.setVisibility(View.GONE);
                    startedTypingMessage = false;
                }
            }
        });
    }

    private void initializeMembersView() {

        // Show the members count
        membersCountTextView.setText(getString(R.string.tour_info_members_count, membersList.size()));

        if (membersAdapter == null) {
            // Initialize the recycler view
            membersView.setLayoutManager(new LinearLayoutManager(getContext()));
            membersAdapter = new MembersAdapter();
            membersView.setAdapter(membersAdapter);
        }

        // add the members
        membersAdapter.addItems(membersList);
    }

    private void switchToPublicSection() {
        actLayout.setVisibility(View.VISIBLE);
        publicSection.setVisibility(View.VISIBLE);
        privateSection.setVisibility(View.GONE);
        membersLayout.setVisibility(View.GONE);

        updateHeaderButtons();
        initializeOptionsView();
        updateJoinStatus();

        initializeMap();
    }

    private void switchToPrivateSection() {
        actLayout.setVisibility(feedItem.isFreezed() ? View.VISIBLE : View.GONE);
        membersLayout.setVisibility(View.VISIBLE);
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
        }

        initializeDiscussionList();
        initializeMembersView();
    }

    private void loadPrivateCards() {
        if (presenter != null) {
            presenter.getFeedItemUsers();
            presenter.getFeedItemMessages();
            presenter.getFeedItemEncounters();
        }
    }

    private void updateJoinStatus() {
        if (feedItem == null) return;
        if (feedItem.isFreezed()) {
            //actLayout.setVisibility(View.GONE);

            // MI: Instead of hiding it, display the freezed text
            actLayout.setVisibility(View.VISIBLE);
            joinButton.setText(R.string.tour_cell_button_freezed);
            joinButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.button_act_freezed), null, null);
        }
        else {
            actLayout.setVisibility(View.VISIBLE);
            String joinStatus = feedItem.getJoinStatus();
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
    }

    private void updateDiscussionList() {
        updateDiscussionList(true);
    }

    private void updateDiscussionList(boolean scrollToLastCard) {

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
        Date timestamp;
        if (feedItem.getCachedCardInfoList().size() == 0) {
            timestamp = feedItem.getStartTime();
        }
        else {
            timestamp = duration == 0 ? feedItem.getStartTime() : now;
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
            Tour tour = (Tour)feedItem;
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
        }
        TourTimestamp tourTimestamp = new TourTimestamp(
                feedItem.getEndTime(),
                now,
                feedItem.getType(),
                FeedItem.STATUS_CLOSED,
                endPoint,
                duration,
                distance
        );
        discussionAdapter.addCardInfo(tourTimestamp);
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

        tourOrganization.setText(feedItem.getTitle());
        tourDescription.setText(feedItem.getDescription());

        String distanceAsString = "";
        TourPoint entourageLocation = ((Entourage)feedItem).getLocation();
        if (entourageLocation != null) {
            distanceAsString = entourageLocation.distanceToCurrentLocation();
        }
        tourLocation.setText(String.format(getResources().getString(R.string.tour_cell_location), Tour.getStringDiffToNow(feedItem.getStartTime()), distanceAsString));
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
        }
    }

    protected void onFeedItemUsersReceived(List<TourUser> tourUsers) {
        if (getActivity() == null || !isAdded()) return;

        if (tourUsers != null) {
            List<TimestampedObject> timestampedObjectList = new ArrayList<>();
            Iterator<TourUser> iterator = tourUsers.iterator();
            // check if this is my entourage
            User me = EntourageApplication.me(getActivity());
            boolean isMyEntourage = false;
            if (me != null) {
                isMyEntourage = me.getId() == feedItem.getAuthor().getUserID();
            }
            // iterate over the received users
            while (iterator.hasNext()) {
                TourUser tourUser =  iterator.next();
                // add the author to members list and skip it
                if (tourUser.getUserId() == feedItem.getAuthor().getUserID()) {
                    TourUser clone = tourUser.clone();
                    clone.setDisplayedAsMember(true);
                    membersList.add(clone);
                    continue;
                }
                //show only the accepted users
                if (!tourUser.getStatus().equals(FeedItem.JOIN_STATUS_ACCEPTED)) {
                    // if it's my entourage, show the pending requests too
                    if (!isMyEntourage || !tourUser.getStatus().equals(FeedItem.JOIN_STATUS_PENDING))
                    continue;
                }
                tourUser.setFeedItem(feedItem);
                timestampedObjectList.add(tourUser);

                if (FeedItem.JOIN_STATUS_ACCEPTED.equals(tourUser.getStatus())) {
                    TourUser clone = tourUser.clone();
                    clone.setDisplayedAsMember(true);
                    membersList.add(clone);
                }
            }
            feedItem.addCardInfoList(timestampedObjectList);

            initializeMembersView();
        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList();
    }

    protected void onFeedItemMessagesReceived(List<ChatMessage> chatMessageList) {
        if (getActivity() == null || !isAdded()) return;

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

    protected void onUserJoinRequestUpdated(int userId, String status, boolean success) {
        hideProgressBar();
        if (success) {
            // Updated ok
            Toast.makeText(getActivity(), R.string.tour_join_request_success, Toast.LENGTH_SHORT).show();
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
        }
        else {
            // Error
            Toast.makeText(getActivity(), R.string.tour_join_request_error, Toast.LENGTH_SHORT).show();
        }
    }

    protected void onInvitationStatusUpdated(boolean success, String status) {
        Button acceptInvitationButton = (Button) this.getView().findViewById(R.id.tour_info_invited_accept_button);
        Button rejectInvitationButton = (Button) this.getView().findViewById(R.id.tour_info_invited_reject_button);
        acceptInvitationButton.setEnabled(true);
        rejectInvitationButton.setEnabled(true);
        if (success) {
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

            // Post an event
            BusProvider.getInstance().post(new Events.OnInvitationStatusChanged(this.feedItem, status));

        } else {
            Toast.makeText(getActivity(), R.string.invited_updated_error, Toast.LENGTH_SHORT).show();
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
    public void onGpsStatusChanged(final boolean active) {

    }

    @Override
    public void onUserStatusChanged(final TourUser user, final FeedItem feedItem) {
        //ignore requests that are not related to our feed item
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
