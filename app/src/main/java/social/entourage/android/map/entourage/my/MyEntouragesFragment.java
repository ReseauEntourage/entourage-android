package social.entourage.android.map.entourage.my;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Subscribe;

import java.util.Date;
import java.util.List;
import java.util.Map;
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
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.base.EntouragePagination;
import social.entourage.android.base.EntourageViewHolderListener;
import social.entourage.android.configuration.Configuration;
import social.entourage.android.map.entourage.my.filter.MyEntouragesFilter;
import social.entourage.android.map.entourage.my.filter.MyEntouragesFilterFactory;
import social.entourage.android.map.tour.TourService;
import social.entourage.android.tools.BusProvider;

/**
 * My Entourages Fragment
 */
public class MyEntouragesFragment extends EntourageDialogFragment implements TourService.TourServiceListener, EntourageViewHolderListener, MyEntouragesAdapter.LoaderCallback {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.my.entourages";

    private static final long REFRESH_INVITATIONS_INTERVAL = 60000; //1 minute in ms

    private static final int FILTER_TAB_INDEX_ALL = 0;
    private static final int FILTER_TAB_INDEX_UNREAD = 1;

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @Inject
    MyEntouragesPresenter presenter;

    TourService tourService;
    private ServiceConnection connection = new ServiceConnection();
    private boolean isBound = false;

    @BindView(R.id.myentourages_fab_menu)
    FloatingActionMenu fabMenu;

    @BindView(R.id.button_start_tour_launcher)
    FloatingActionButton startTourButton;

    @BindView(R.id.button_add_tour_encounter)
    FloatingActionButton addEncounterButton;

    @BindView(R.id.myentourages_list_view)
    RecyclerView entouragesView;

    MyEntouragesAdapter entouragesAdapter;

    @BindView(R.id.myentourages_layout_no_items)
    View noItemsView;

    @BindView(R.id.myentourages_no_items_title)
    TextView noItemsTitleTextView;

    @BindView(R.id.myentourages_no_items_details)
    TextView noItemsDetailsTextView;

    @BindView(R.id.myentourages_progressBar)
    ProgressBar progressBar;

    private @NonNull EntouragePagination entouragesPagination = new EntouragePagination(Constants.ITEMS_PER_PAGE);

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

        doBindService();

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);

        if (isBound) {
            tourService.unregisterTourServiceListener(this);
        }
        doUnbindService();

        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_my_entourages, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        initializeView();

        refreshMyFeeds();

        if (getActivity() != null) {
            ((DrawerActivity)getActivity()).showEditActionZoneFragment();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        timerStart();
    }

    @Override
    public void onPause() {
        super.onPause();

        timerStop();
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerMyEntouragesComponent.builder()
                .entourageComponent(entourageComponent)
                .myEntouragesModule(new MyEntouragesModule(this))
                .build()
                .inject(this);
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private void initializeView() {
        initializeFilterTab();
        initializeEntouragesView();
        initializeFabMenu();
    }

    private void initializeFilterTab() {
        TabLayout tabLayout = getView().findViewById(R.id.myentourages_tab);
        if (tabLayout == null) return;
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                MyEntouragesFilter filter = MyEntouragesFilterFactory.getMyEntouragesFilter(getContext());
                switch (tab.getPosition()) {
                    case FILTER_TAB_INDEX_ALL:
                        filter.setShowUnreadOnly(false);
                        refreshMyFeeds();
                        break;
                    case FILTER_TAB_INDEX_UNREAD:
                        filter.setShowUnreadOnly(true);
                        EntourageEvents.logEvent(EntourageEvents.EVENT_MYENTOURAGES_FILTER_UNREAD);
                        refreshMyFeeds();
                        break;
                }
            }

            @Override
            public void onTabUnselected(final TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(final TabLayout.Tab tab) {

            }
        });

        // The tab is initialised ALL
        // So we need to reset the filter flag and no item view in case the user switched to another screen
        // while the UNREAD tab was active
        MyEntouragesFilter filter = MyEntouragesFilterFactory.getMyEntouragesFilter(getContext());
        filter.setShowUnreadOnly(false);

        noItemsView.setVisibility(View.GONE);
    }

    private void initializeEntouragesView() {
        if (entouragesAdapter == null) {
            entouragesAdapter = new MyEntouragesAdapter();
        }
        entouragesView.setLayoutManager(new LinearLayoutManager(getContext()));
        entouragesAdapter.setViewHolderListener(this);
        entouragesAdapter.setLoaderCallback(this);
        entouragesView.setAdapter(entouragesAdapter);
    }

    private void initializeFabMenu() {
        fabMenu.setVisibility(Configuration.getInstance().showMyMessagesFAB() ? View.VISIBLE : View.GONE);
        updateFabMenu();
        fabMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(final boolean opened) {
                if (opened) {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_MYENTOURAGES_PLUS_CLICK);
                }
            }
        });
    }

    private void updateFabMenu() {
        User me = EntourageApplication.me(getActivity());
        boolean isPro = false;
        if (me != null) {
            isPro = me.isPro();
        }
        boolean isTourRunning = isBound && tourService.isRunning();
        if (startTourButton != null) startTourButton.setVisibility( isPro ? (isTourRunning ? View.GONE : View.VISIBLE) : View.GONE );
        if (addEncounterButton != null) addEncounterButton.setVisibility(isTourRunning ? View.VISIBLE : View.GONE);
    }

    private void retrieveMyFeeds() {
        if (!entouragesPagination.isLoading) {
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

    /* Removed in 5.0
    @OnClick(R.id.myentourages_filter_button)
    void onFilterClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MYENTOURAGES_FILTER_CLICK);
        MyEntouragesFilterFragment fragment = new MyEntouragesFilterFragment();
        fragment.show(getFragmentManager(), MyEntouragesFilterFragment.TAG);
    }
    */

    @OnClick(R.id.button_create_entourage)
    void onCreateEntourageClicked() {
        if (getActivity() instanceof DrawerActivity) {
            fabMenu.close(false);
            DrawerActivity activity = (DrawerActivity)getActivity();
            activity.onCreateEntourageClicked();
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
    public void onEntourageUpdated(Events.OnEntourageUpdated event) {
        if (event == null || event.getEntourage() == null) return;
        Entourage entourage = event.getEntourage();
        entouragesAdapter.updateCard(entourage);
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
        int cardType;
        if (content.isTourRelated()) cardType = TimestampedObject.TOUR_CARD;
        else if (content.isEntourageRelated()) cardType = TimestampedObject.ENTOURAGE_CARD;
        else return;
        long joinableId = content.getJoinableId();

        TimestampedObject card = entouragesAdapter.findCard(cardType, joinableId);
        if (card instanceof FeedItem) {
            ((FeedItem)card).increaseBadgeCount();
            entouragesAdapter.updateCard(card);

            // if (presenter != null) presenter.getFeedItem(String.valueOf(joinableId), cardType);
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
        //reset the loading indicator
        entouragesPagination.isLoading = false;

        if (!isAdded()) {
            return;
        }
        //ignore errors
        if (newsfeedList == null) return;
        //add the feed
        MyEntouragesFilter filter = MyEntouragesFilterFactory.getMyEntouragesFilter(this.getContext());
        boolean showUnreadOnly = filter.isShowUnreadOnly();

        entouragesAdapter.removeLoader();

        if (newsfeedList.size() > 0) {
            EntourageApplication application = EntourageApplication.get(getContext());

            for (final Newsfeed newsfeed : newsfeedList) {
                Object feedData = newsfeed.getData();
                if (!(feedData instanceof FeedItem)) {
                    continue;
                }
                FeedItem feedItem = (FeedItem) newsfeed.getData();
                if (application != null) {
                    application.updateBadgeCountForFeedItem(feedItem);
                }

                // show only the unread ones if filter is set
                if (showUnreadOnly && feedItem.getBadgeCount() <= 0) {
                    continue;
                }

                if (entouragesAdapter.findCard(feedItem) == null) {
                    entouragesAdapter.addCardInfo(feedItem);
                } else {
                    entouragesAdapter.updateCard(feedItem);
                }
            }

            //increase page and items count
            entouragesPagination.loadedItems(newsfeedList.size());
            if(entouragesPagination.nextPageAvailable) {
                entouragesAdapter.addLoader();
            }
        }
        if (entouragesAdapter.getDataItemCount() == 0) {
            noItemsView.setVisibility(View.VISIBLE);
            noItemsTitleTextView.setVisibility(showUnreadOnly ? View.GONE : View.VISIBLE);
            noItemsDetailsTextView.setText(showUnreadOnly ? R.string.myentourages_no_unread_items_details : R.string.myentourages_no_items_details);
        } else {
            noItemsView.setVisibility(View.GONE);
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
        entouragesAdapter.setInvitations(invitationList);
    }

    protected void onFeedItemReceived(FeedItem feedItem) {
        if (getActivity() == null || !isAdded()) return;

        if (feedItem != null) {
            TimestampedObject card = entouragesAdapter.findCard(feedItem);
            if (card instanceof FeedItem) {
                ((FeedItem)card).increaseBadgeCount();
                entouragesAdapter.updateCard(feedItem);
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
        if (created) {
            updateFabMenu();
        }
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
    public void onFeedItemClosed(final boolean closed, final FeedItem feedItem) {
        if (closed) {
            updateFabMenu();
        }
    }

    @Override
    public void onLocationProviderStatusChanged(final boolean active) {

    }

    @Override
    public void onUserStatusChanged(final TourUser user, final FeedItem feedItem) {
        if (feedItem == null || user == null) return;
        // if the user was rejected or canceled the request
        if ( FeedItem.JOIN_STATUS_REJECTED.equals(user.getStatus()) || FeedItem.JOIN_STATUS_CANCELLED.equals(user.getStatus()) ) {
            // remove the feed item
            entouragesAdapter.removeCard(feedItem);
        }
    }

    // ----------------------------------
    // EntourageViewHolderListener
    // ----------------------------------

    @Override
    public void onViewHolderDetailsClicked(final int detailType) {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MYENTOURAGES_MESSAGE_OPEN);
    }

    @Override
    public void loadMoreItems() {
        retrieveMyFeeds();
    }

    // ----------------------------------
    // PRIVATE CLASSES
    // ----------------------------------

    private class ServiceConnection implements android.content.ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (getActivity() != null) {
                tourService = ((TourService.LocalBinder) service).getService();
                tourService.registerTourServiceListener(MyEntouragesFragment.this);
                isBound = true;
                updateFabMenu();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (tourService != null) tourService.unregisterTourServiceListener(MyEntouragesFragment.this);
            tourService = null;
            isBound = false;
        }
    }

}
