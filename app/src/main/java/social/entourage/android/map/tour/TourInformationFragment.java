package social.entourage.android.map.tour;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.TourTransportMode;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.authentication.AuthenticationController;

public class TourInformationFragment extends DialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 2;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    TourInformationPresenter presenter;

    @Bind(R.id.tour_info_organization)
    TextView tourOrganization;

    @Bind(R.id.tour_info_author_photo)
    ImageView tourAuthorPhoto;

    @Bind(R.id.tour_info_type)
    TextView tourType;

    @Bind(R.id.tour_info_author_name)
    TextView tourAuthorName;

    @Bind(R.id.tour_info_discussion_layout)
    LinearLayout discussionLayout;

    @Bind(R.id.tour_info_progress_bar)
    ProgressBar progressBar;

    @Bind(R.id.tour_info_comment_layout)
    LinearLayout commentLayout;

    @Bind(R.id.tour_info_comment)
    EditText commentEditText;

    int apiRequestsCount;

    Tour tour;

    List<TourUser> tourUserList;
    List<ChatMessage> chatMessageList;
    List<TimestampedObject> cardInfoList;
    Date lastChatMessageDate = null;

    boolean isDiscussionListInitialised = false;

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

        presenter.getTourUsers();
        presenter.getTourMessages();
        presenter.getTourEncounters();
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

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeView() {

        tour = (Tour) getArguments().getSerializable(Tour.KEY_TOUR);

        apiRequestsCount = 0;
        tourUserList = new ArrayList<>();
        chatMessageList = new ArrayList<>();
        cardInfoList = new ArrayList<>();

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

        //hide the comment section if the user is not accepted
        if (!tour.getJoinStatus().equals(Tour.JOIN_STATUS_ACCEPTED)) {
            commentLayout.setVisibility(View.GONE);
        }

    }

    private void initialiseDiscussionList() {
        //wait for the API calls to finish
        if (apiRequestsCount > 0) return;

        //sort the cards
        Collections.sort(cardInfoList, new TimestampedObject.TimestampedObjectComparatorOldToNew());

        //add the start time
        addDiscussionTourStartCard();

        boolean isTourClosed = tour.isClosed();
        boolean isTourClosedCardAdded = false;

        //add the cards
        if (cardInfoList != null) {
            for (int i = 0; i < cardInfoList.size(); i++) {
                TimestampedObject cardInfo = cardInfoList.get(i);

                //check if we need to add the Tour closed card
                if (isTourClosed && !isTourClosedCardAdded) {
                    Date cardTimestamp = cardInfo.getTimestamp();
                    if (cardTimestamp != null && tour.getEndTime() != null) {
                        if (cardTimestamp.after(tour.getEndTime())) {
                            addDiscussionSeparator();
                            addDiscussionTourEndCard();

                            isTourClosedCardAdded = true;
                        }
                    }
                }

                //add the card
                addDiscussionCard(cardInfo, discussionLayout.getChildCount());
            }
        }

        //add the end time, if tour is closed and card not displayed
        if (tour.isClosed() && !isTourClosedCardAdded) {
            addDiscussionSeparator();
            addDiscussionTourEndCard();
        }

        isDiscussionListInitialised = true;
    }

    private void updateDiscussionList() {
        if (!isDiscussionListInitialised) {
            initialiseDiscussionList();
            return;
        }
        if (cardInfoList == null) {
            return;
        }
        for (int i = 0; i < cardInfoList.size(); i++) {
            TimestampedObject cardInfo = cardInfoList.get(i);

            int cardIndex = findCardIndexOlderThan(cardInfo.getTimestamp());

            //add the card
            addDiscussionCard(cardInfo, cardIndex);
        }
    }

    private void addDiscussionSeparator() {
        addDiscussionSeparator(discussionLayout.getChildCount());
    }

    private void addDiscussionSeparator(int atIndex) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout separatorLayout = (LinearLayout)inflater.inflate(R.layout.tour_information_separator_card, discussionLayout, false);
        View discussionSeparator = separatorLayout.findViewById(R.id.tic_separator);
        separatorLayout.removeView(discussionSeparator);

        discussionLayout.addView(discussionSeparator, atIndex);
    }

    private void addDiscussionCard(TimestampedObject cardInfo, int afterIndex) {
        View card = null;
        if (cardInfo.getClass() == TourUser.class) {
            TourUser tourUser = (TourUser)cardInfo;
            //skip the author
            if (tourUser.getUserId() == tour.getAuthor().getUserID()) {
                return;
            }
            //skip the rejected user
            if (tourUser.getStatus().equals(Tour.JOIN_STATUS_REJECTED)) {
                return;
            }
            //get the user card
            card = getDiscussionTourUserCard(tourUser);
        }
        else if (cardInfo.getClass() == ChatMessage.class) {
            ChatMessage chatMessage = (ChatMessage)cardInfo;
            //get the chat card
            card = getDiscussionChatMessageCard(chatMessage);
        }
        else if (cardInfo.getClass() == Encounter.class) {
            Encounter encounter = (Encounter)cardInfo;
            //get the encounter card
            card = getDiscussionEncounterCard(encounter);
        }

        //add the card
        if (card != null) {
            addDiscussionSeparator(afterIndex);
            discussionLayout.addView(card, afterIndex+1);  //+1 because we added the separator
        }
    }

    private void addDiscussionTourStartCard() {
        TourInformationLocationCardView startCard = new TourInformationLocationCardView(getContext());
        startCard.populate(tour, true);
        startCard.setTag(tour.getStartTime());

        discussionLayout.addView(startCard);
    }

    private void addDiscussionTourEndCard() {
        TourInformationLocationCardView endCard = new TourInformationLocationCardView(getContext());
        endCard.populate(tour, false);
        endCard.setTag(tour.getEndTime());

        discussionLayout.addView(endCard);
    }

    private View getDiscussionTourUserCard(TourUser tourUser) {
        TourInformationUserCardView userCardView = new TourInformationUserCardView(getContext());
        userCardView.populate(tourUser);
        userCardView.setTag(tourUser.getTimestamp());

        return userCardView;
    }

    private View getDiscussionChatMessageCard(ChatMessage chatMessage) {
        TourInformationChatMessageCardView chatMessageCardView = new TourInformationChatMessageCardView(getContext(), chatMessage);
        chatMessageCardView.setTag(chatMessage.getTimestamp());

        return chatMessageCardView;
    }

    private View getDiscussionEncounterCard(Encounter encounter) {
        TourInformationEncounterCardView encounterCardView = new TourInformationEncounterCardView(getContext());
        encounterCardView.populate(encounter);
        encounterCardView.setTag(encounter.getTimestamp());

        return encounterCardView;
    }

    private int findCardIndexOlderThan(Date referenceDate) {
        int discussionViewCount = discussionLayout.getChildCount();
        if (referenceDate == null) {
            return discussionViewCount;
        }
        for (int i = 0; i < discussionViewCount-1; i++) {
            View v = discussionLayout.getChildAt(i);
            Object tag = v.getTag();
            if (tag == null || tag.getClass() != Date.class) {
                continue;
            }
            Date viewDate = (Date)tag;
            if (viewDate.after(referenceDate)) {
                if (i == 0) return 0;
                return i-1;
            }
        }
        return discussionViewCount;
    }

    protected void showProgressBar() {
        apiRequestsCount++;
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar() {
        apiRequestsCount--;
        if (apiRequestsCount <= 0) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private OnTourInformationFragmentFinish getOnTourInformationFragmentFinish() {
        final Activity activity = getActivity();
        return activity != null ? (OnTourInformationFragmentFinish) activity : null;
    }

    // ----------------------------------
    // Server callbacks
    // ----------------------------------

    protected void onTourUsersReceived(List<TourUser> tourUsers) {
        if (tourUserList != null) {
            tourUserList.addAll(tourUsers);
            cardInfoList.addAll(tourUsers);
        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList();
    }

    protected void onTourMessagesReceived(List<ChatMessage> chatMessageList) {
        if (chatMessageList != null) {
            this.chatMessageList.addAll(chatMessageList);
            cardInfoList.addAll(chatMessageList);

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
                //remember the last chat message
                ChatMessage chatMessage = chatMessageList.get(chatMessageList.size() - 1);
                lastChatMessageDate = chatMessage.getCreationDate();
            }
        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList();
    }

    protected void onTourMessageSent(ChatMessage chatMessage) {
        hideProgressBar();

        if (chatMessage == null) {
            Toast.makeText(getContext(), R.string.tour_info_error_chat_message, Toast.LENGTH_SHORT).show();
            return;
        }
        commentEditText.setText("");

        //add the message to the list
        chatMessage.setIsMe(true);
        chatMessageList.add(chatMessage);
        cardInfoList.add(chatMessage);

        //add the separator
        addDiscussionSeparator();

        //add the message card
        addDiscussionCard(chatMessage, discussionLayout.getChildCount());
    }

    protected void onTourEncountersReceived(List<Encounter> encounterList) {
        if (encounterList != null) {
            cardInfoList.addAll(encounterList);
        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList();
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public interface OnTourInformationFragmentFinish {
        void closeTourInformationFragment(TourInformationFragment fragment);
    }
}
