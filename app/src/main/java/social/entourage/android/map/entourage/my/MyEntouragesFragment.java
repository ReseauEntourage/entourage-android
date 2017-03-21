package social.entourage.android.map.entourage.my;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.flurry.android.FlurryAgent;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.squareup.otto.Subscribe;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.base.EntouragePagination;
import social.entourage.android.invite.view.InvitationsAdapter;
import social.entourage.android.map.entourage.my.filter.MyEntouragesFilterFragment;
import social.entourage.android.tools.BusProvider;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyEntouragesFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.my.entourages";

    private static final int MAX_SCROLL_DELTA_Y = 20;
    private static final long REFRESH_INVITATIONS_INTERVAL = 60000; //1 minute in ms

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @Inject
    MyEntouragesPresenter presenter;

    @BindView(R.id.myentourages_fab_menu)
    FloatingActionMenu fabMenu;

    @BindView(R.id.button_start_tour_launcher)
    FloatingActionButton startTourButton;

    @BindView(R.id.myentourages_invitations_view)
    RecyclerView invitationsView;

    InvitationsAdapter invitationsAdapter;

    @BindView(R.id.myentourages_list_view)
    RecyclerView entouragesView;

    MyEntouragesAdapter entouragesAdapter;

    @BindView(R.id.myentourages_progressBar)
    ProgressBar progressBar;

    private int apiRequestsCount = 0;

    private EntouragePagination entouragesPagination = new EntouragePagination(Constants.ITEMS_PER_PAGE);

    private int scrollDeltaY;
    private OnScrollListener scrollListener = new OnScrollListener();

    // Refresh invitations attributes
    Timer refreshInvitationsTimer;
    TimerTask refreshInvitationsTimerTask;
    final Handler refreshInvitationsHandler = new Handler();
    boolean isRefreshingInvitations = false;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public MyEntouragesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);

        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_my_entourages, container, false);
        ButterKnife.bind(this, view);
        initializeView();

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());

        retrieveMyFeeds();
    }

    @Override
    public void onResume() {
        super.onResume();

        timerStart();
        entouragesView.addOnScrollListener(scrollListener);
    }

    @Override
    public void onPause() {
        super.onPause();

        timerStop();
        entouragesView.removeOnScrollListener(scrollListener);
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerMyEntouragesComponent.builder()
                .entourageComponent(entourageComponent)
                .myEntouragesModule(new MyEntouragesModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected int getSlideStyle() {
        return R.style.CustomDialogFragmentFromRight;
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private void initializeView() {
        initializeInvitationsView();
        initializeEntouragesView();
        initializeFabMenu();
    }

    private void initializeInvitationsView() {
        invitationsView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        invitationsAdapter = new InvitationsAdapter();
        invitationsView.setAdapter(invitationsAdapter);
    }

    private void initializeEntouragesView() {
        entouragesView.setLayoutManager(new LinearLayoutManager(getContext()));
        entouragesAdapter = new MyEntouragesAdapter();
        entouragesView.setAdapter(entouragesAdapter);
    }

    private void initializeFabMenu() {
        User me = EntourageApplication.me(getActivity());
        boolean isPro = false;
        if (me != null) {
            isPro = me.isPro();
        }
        startTourButton.setVisibility( isPro ? View.VISIBLE : View.GONE );

        fabMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(final boolean opened) {
                if (opened) {
                    FlurryAgent.logEvent(Constants.EVENT_MYENTOURAGES_PLUS_CLICK);
                }
            }
        });
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

    private void retrieveMyFeeds() {
        if (entouragesPagination != null && !entouragesPagination.isLoading) {
            showProgressBar();
            entouragesPagination.isLoading = true;
            presenter.getMyFeeds(entouragesPagination.page, entouragesPagination.itemsPerPage);
        }
    }

    private void refreshMyFeeds() {
        // remove the current feed
        entouragesAdapter.removeAll();
        entouragesPagination = new EntouragePagination(Constants.ITEMS_PER_PAGE);
        // request a new feed
        retrieveMyFeeds();
    }

    // ----------------------------------
    // BUTTONS HANDLING
    // ----------------------------------

    @OnClick(R.id.myentourages_back_button)
    void onBackClicked() {
        FlurryAgent.logEvent(Constants.EVENT_MYENTOURAGES_BACK_CLICK);
        dismiss();
    }

    @OnClick(R.id.myentourages_filter_button)
    void onFilterClicked() {
        FlurryAgent.logEvent(Constants.EVENT_MYENTOURAGES_FILTER_CLICK);
        MyEntouragesFilterFragment fragment = new MyEntouragesFilterFragment();
        fragment.show(getFragmentManager(), MyEntouragesFilterFragment.TAG);
    }

    @OnClick(R.id.button_create_entourage_demand)
    void onCreateEntourageDemandClicked() {
        if (getActivity() instanceof DrawerActivity) {
            fabMenu.close(false);
            DrawerActivity activity = (DrawerActivity)getActivity();
            activity.onCreateEntouragDemandClicked();
        }
    }

    @OnClick(R.id.button_create_entourage_contribution)
    void onCreateEntourageContributionClicked() {
        if (getActivity() instanceof DrawerActivity) {
            fabMenu.close(false);
            DrawerActivity activity = (DrawerActivity)getActivity();
            activity.onCreateEntourageContributionClicked();
        }
    }

    @OnClick(R.id.button_start_tour_launcher)
    void onStartTourClicked() {
        if (getActivity() instanceof DrawerActivity) {
            fabMenu.close(false);
            DrawerActivity activity = (DrawerActivity)getActivity();
            activity.onStartTourClicked();

            dismiss();
        }
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void onMyEntouragesFilterChanged(Events.OnMyEntouragesFilterChanged event) {
        refreshMyFeeds();
    }

    @Subscribe
    public void onEntourageCreated(Events.OnEntourageCreated event) {
        refreshMyFeeds();
    }

    @Subscribe
    public void onInvitationStatusChanged(Events.OnInvitationStatusChanged event) {
        // Refresh the invitations list
        refreshInvitations();
        // Refresh the entourages list if invitation was accepted
        if (Invitation.STATUS_ACCEPTED.equals(event.getStatus())) {
            refreshMyFeeds();
        }
    }

    // ----------------------------------
    // Push handling
    // ----------------------------------

    public void onPushNotificationReceived(Message message) {
        PushNotificationContent content = message.getContent();
        if (content == null) return;
        String joinableTypeString = content.getType();
        int cardType = 0;
        if (content.isTourRelated()) cardType = TimestampedObject.TOUR_CARD;
        else if (content.isEntourageRelated()) cardType = TimestampedObject.ENTOURAGE_CARD;
        else return;
        long joinableId = content.getJoinableId();

        TimestampedObject card = entouragesAdapter.findCard(cardType, joinableId);
        if (card != null && card instanceof FeedItem) {
            ((FeedItem)card).increaseBadgeCount();
            entouragesAdapter.updateCard(card);
            return;
        }
    }

    public void onPushNotificationConsumedForFeedItem(FeedItem feedItem) {
        if (entouragesAdapter == null) {
            return;
        }
        FeedItem feedItemCard = (FeedItem) entouragesAdapter.findCard(feedItem);
        if (feedItemCard == null) {
            return;
        }
        feedItemCard.setBadgeCount(0);
        entouragesAdapter.updateCard(feedItemCard);
    }

    // ----------------------------------
    // Refresh invitations timer handling
    // ----------------------------------

    private void timerStart() {
        //create the timer
        refreshInvitationsTimer = new Timer();
        //create the task
        refreshInvitationsTimerTask = new TimerTask() {
            @Override
            public void run() {
                refreshInvitationsHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshInvitations();
                    }
                });
            }
        };
        //schedule the timer
        refreshInvitationsTimer.schedule(refreshInvitationsTimerTask, 0, REFRESH_INVITATIONS_INTERVAL);
    }

    private void timerStop() {
        if (refreshInvitationsTimer != null) {
            refreshInvitationsTimer.cancel();
            refreshInvitationsTimer = null;
        }
    }

    private void refreshInvitations() {
        if (presenter != null) {
            if (!isRefreshingInvitations) {
                presenter.getMyPendingInvitations();
                isRefreshingInvitations = true;
            }
        }
    }

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------

    protected void onNewsfeedReceived(List<Newsfeed> newsfeedList) {
        if (!isAdded()) {
            return;
        }
        hideProgressBar();
        //reset the loading indicator
        if (entouragesPagination != null) {
            entouragesPagination.isLoading = false;
        }
        //ignore errors
        if (newsfeedList == null) return;
        //add the feed
        if (newsfeedList.size() > 0) {
            EntourageApplication application = EntourageApplication.get(getContext());

            Iterator<Newsfeed> iterator = newsfeedList.iterator();
            while (iterator.hasNext()) {
                Newsfeed newsfeed = iterator.next();
                Object feedData = newsfeed.getData();
                if (feedData == null || !(feedData instanceof FeedItem)) {
                    continue;
                }
                FeedItem feedItem = (FeedItem)newsfeed.getData();
                if (application != null) {
                    application.updateBadgeCountForFeedItem(feedItem);
                }

                if (entouragesAdapter.findCard(feedItem) == null) {
                    entouragesAdapter.addCardInfoBeforeTimestamp(feedItem);
                }
                else {
                    entouragesAdapter.updateCard(feedItem);
                }
            }

            //increase page and items count
            if (entouragesPagination != null) {
                entouragesPagination.loadedItems(newsfeedList.size());
            }
        }
    }

    protected void onInvitationsReceived(List<Invitation> invitationList) {
        // reset the semaphore
        isRefreshingInvitations = false;
        // check if the fragment is still attached
        if (!isAdded()) {
            return;
        }
        // ignore errors
        if (invitationList == null) {
            return;
        }
        invitationsAdapter.removeAll();
        // add the invitations
        Iterator<Invitation> iterator = invitationList.iterator();
        while (iterator.hasNext()) {
            Invitation invitation = iterator.next();
            invitationsAdapter.addCardInfoBeforeTimestamp(invitation);
        }
    }

    // ----------------------------------
    // PRIVATE CLASSES
    // ----------------------------------

    private class OnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {

            scrollDeltaY += dy;
            if (dy > 0 && scrollDeltaY > MAX_SCROLL_DELTA_Y) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int position = linearLayoutManager.findLastVisibleItemPosition();
                if (position == recyclerView.getAdapter().getItemCount()-1) {
                    retrieveMyFeeds();
                }

                scrollDeltaY = 0;
            }
        }
        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
        }
    }

}
