package social.entourage.android.newsfeed;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.map.entourage.create.CreateEntourageFragment;
import social.entourage.android.map.entourage.EntourageCloseFragment;
import social.entourage.android.tools.BusProvider;


public class FeedItemOptionsFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.FeedItemOptions";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private FeedItem feedItem;

    @BindView(R.id.feeditem_options)
    View optionsView;

    @BindView(R.id.feeditem_option_stop)
    Button stopButton;

    @BindView(R.id.feeditem_option_quit)
    Button quitButton;

    @BindView(R.id.feeditem_option_edit)
    Button editButton;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public FeedItemOptionsFragment() {
        // Required empty public constructor
    }

    public static FeedItemOptionsFragment newInstance(FeedItem feedItem) {
        FeedItemOptionsFragment fragment = new FeedItemOptionsFragment();
        Bundle args = new Bundle();
        args.putSerializable(FeedItem.KEY_FEEDITEM, feedItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            feedItem = (FeedItem)getArguments().getSerializable(FeedItem.KEY_FEEDITEM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_feeditem_options, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeView();
    }

    private void initializeView() {
        if (feedItem == null) return;
        User me = EntourageApplication.me(getActivity());
        if (me == null || feedItem.getAuthor() == null) return;

        int myId = me.getId();
        if (feedItem.getAuthor().getUserID() != myId) {
            quitButton.setVisibility(View.VISIBLE);
            quitButton.setText(FeedItem.JOIN_STATUS_PENDING.equals(feedItem.getJoinStatus()) ? R.string.tour_info_options_cancel_request : R.string.tour_info_options_quit_tour);
        }
        else {
            stopButton.setVisibility(feedItem.isFreezed() || !feedItem.canBeClosed() ? View.GONE : View.VISIBLE);
            if (feedItem.isClosed() && feedItem.getType() == FeedItem.TOUR_CARD) {
                stopButton.setText(R.string.tour_info_options_freeze_tour);
            } else {
                stopButton.setText(feedItem.getType() == FeedItem.TOUR_CARD ? R.string.tour_info_options_stop_tour : R.string.tour_info_options_freeze_tour);
            }
            if (feedItem.getType() == FeedItem.ENTOURAGE_CARD && FeedItem.STATUS_OPEN.equals(feedItem.getStatus())) {
                editButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected int getSlideStyle() {
        return R.style.CustomDialogFragmentSlide;
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------

    @OnClick({R.id.feeditem_option_cancel, R.id.feeditem_options})
    protected void onCancelClicked() {
        dismiss();
    }

    @OnClick(R.id.feeditem_option_stop)
    protected void onStopClicked() {
        if (feedItem.getStatus().equals(FeedItem.STATUS_ON_GOING) || feedItem.getStatus().equals(FeedItem.STATUS_OPEN)) {
            if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
                Tour tour = (Tour)feedItem;
                //compute distance
                float distance = 0.0f;
                List<TourPoint> tourPointsList = tour.getTourPoints();
                if (tourPointsList.size() > 0) {
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

                //show stop tour activity
                Activity activity = getActivity();
                if (activity instanceof DrawerActivity) {
                    ((DrawerActivity)activity).showStopTourActivity(tour);
                }

                //hide the options
                dismiss();
            }
            else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
                //BusProvider.getInstance().post(new Events.OnFeedItemCloseRequestEvent(feedItem, false));
                FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager();
                EntourageCloseFragment entourageCloseFragment = EntourageCloseFragment.newInstance(feedItem);
                entourageCloseFragment.show(fragmentManager, EntourageCloseFragment.TAG, getContext());
                dismiss();
            }
        }
        else if (feedItem.getType() == TimestampedObject.TOUR_CARD && feedItem.getStatus().equals(FeedItem.STATUS_CLOSED)) {
            BusProvider.getInstance().post(new Events.OnFeedItemCloseRequestEvent(feedItem, false, true));
            dismiss();
        }
    }

    @OnClick(R.id.feeditem_option_quit)
    protected void onQuitClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_QUIT_ENTOURAGE);
        BusProvider.getInstance().post(new Events.OnUserActEvent(Events.OnUserActEvent.ACT_QUIT, feedItem));
        dismiss();
    }

    @OnClick(R.id.feeditem_option_edit)
    protected void onEditClicked() {
        if (feedItem.getType() == FeedItem.ENTOURAGE_CARD) {
            if (feedItem.showEditEntourageView()) {
                if (getFragmentManager() != null) {
                    CreateEntourageFragment fragment = CreateEntourageFragment.newInstance((Entourage) feedItem);
                    fragment.show(getFragmentManager(), CreateEntourageFragment.TAG);
                }
            } else {
                if (getActivity() == null) return;
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
                    // Start the intent
                    startActivity(intent);
                } else {
                    // No Email clients
                    Toast.makeText(getContext(), R.string.error_no_email, Toast.LENGTH_SHORT).show();
                }
            }

            dismiss();
        }
    }

}
