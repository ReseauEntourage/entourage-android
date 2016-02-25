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
import android.os.Bundle;
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

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.TourTransportMode;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourUser;

public class TourInformationFragment extends DialogFragment {

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

    @Bind(R.id.tour_info_comment)
    EditText commentEditText;

    int apiRequestsCount;

    Tour tour;
    List<TourUser> tourUserList;

    List<ChatMessage> chatMessageList;

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 2;

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
        apiRequestsCount++;
        presenter.getTourUsers();
        apiRequestsCount++;
        presenter.getTourMessages();
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
            ImageLoader.getInstance().loadImage(avatarURLAsString, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(final String imageUri, final View view, final Bitmap loadedImage) {
                    tourAuthorPhoto.setImageBitmap(loadedImage);
                }
            });
        }

    }

    private void updateDiscussionList() {

        //wait for the API calls to finish
        if (apiRequestsCount > 0) return;

        //add the start time
        TourInformationLocationCardView startCard = new TourInformationLocationCardView(getContext());
        startCard.populate(tour, true);
        discussionLayout.addView(startCard);

        //add the users
        if (tourUserList != null) {
            for (int i = 0; i < tourUserList.size(); i++) {
                TourUser tourUser = tourUserList.get(i);
                //skip the author
                if (tourUser.getUserId() == tour.getAuthor().getUserID()) {
                    continue;
                }
                //skip the rejected user
                if (tourUser.getStatus().equals(Tour.JOIN_STATUS_REJECTED)) {
                    continue;
                }
                //add the separator
                addDiscussionSeparator();
                //add the user card
                addDiscussionTourUserCard(tourUser);
            }
        }

        //add the end time, if tour is closed
        if (tour.isClosed()) {
            addDiscussionSeparator();

            TourInformationLocationCardView endCard = new TourInformationLocationCardView(getContext());
            endCard.populate(tour, false);
            discussionLayout.addView(endCard);
        }
    }

    private void addDiscussionSeparator() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout separatorLayout = (LinearLayout)inflater.inflate(R.layout.tour_information_separator_card, discussionLayout, false);
        View discussionSeparator = separatorLayout.findViewById(R.id.tic_separator);
        separatorLayout.removeView(discussionSeparator);

        discussionLayout.addView(discussionSeparator);
    }

    private void addDiscussionTourUserCard(TourUser tourUser) {
        TourInformationUserCardView userCardView = new TourInformationUserCardView(getContext());
        userCardView.setUsername(tourUser.getFirstName());
        userCardView.setJoinStatus(tourUser.getStatus());

        discussionLayout.addView(userCardView);
    }

    private void hideProgressBar() {
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
        tourUserList = tourUsers;
        if (tourUserList != null && tourUserList.size() > 1) {
            //order the list based on the request date
            Collections.sort(tourUserList, new TourUser.TourUserComparatorOldToNew());
        }

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList();
    }

    protected void onTourMessagesReceived(List<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;

        //hide the progress bar
        hideProgressBar();

        //update the discussion list
        updateDiscussionList();
    }

    protected void onTourMessageSent(ChatMessage chatMessage) {
        if (chatMessage == null) {
            Toast.makeText(getContext(), R.string.tour_info_error_chat_message, Toast.LENGTH_SHORT).show();
            return;
        }
        commentEditText.setText("");
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public interface OnTourInformationFragmentFinish {
        void closeTourInformationFragment(TourInformationFragment fragment);
    }
}
