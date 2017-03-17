package social.entourage.android.newsfeed;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.flurry.android.FlurryAgent;

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
import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.map.entourage.CreateEntourageFragment;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_feeditem_options, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
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
            stopButton.setVisibility(feedItem.isFreezed() ? View.GONE : View.VISIBLE);
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
                entourageCloseFragment.show(fragmentManager, EntourageCloseFragment.TAG);
                dismiss();
            }
        }
        else if (feedItem.getType() == TimestampedObject.TOUR_CARD && feedItem.getStatus().equals(FeedItem.STATUS_CLOSED)) {
            BusProvider.getInstance().post(new Events.OnFeedItemCloseRequestEvent(feedItem, false));
            dismiss();
        }
    }

    @OnClick(R.id.feeditem_option_quit)
    protected void onQuitClicked() {
        FlurryAgent.logEvent(Constants.EVENT_FEED_QUIT_ENTOURAGE);
        BusProvider.getInstance().post(new Events.OnUserActEvent(Events.OnUserActEvent.ACT_QUIT, feedItem));
        dismiss();
    }

    @OnClick(R.id.feeditem_option_edit)
    protected void onEditClicked() {
        if (feedItem.getType() == FeedItem.ENTOURAGE_CARD) {
            CreateEntourageFragment fragment = CreateEntourageFragment.newInstance((Entourage) feedItem);
            fragment.show(getFragmentManager(), CreateEntourageFragment.TAG);

            dismiss();
        }
    }

}
