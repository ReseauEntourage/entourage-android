package social.entourage.android.entourage.my;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;
import com.squareup.otto.Subscribe;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import social.entourage.android.Constants;
import social.entourage.android.MainActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.base.EntouragePagination;
import social.entourage.android.base.EntourageViewHolderListener;
import social.entourage.android.entourage.my.filter.MyEntouragesFilter;
import social.entourage.android.entourage.my.filter.MyEntouragesFilterFactory;
import social.entourage.android.tools.BusProvider;
import timber.log.Timber;

/**
 * My Entourages Fragment
 */
public class MyEntouragesFragment extends EntourageDialogFragment implements EntourageViewHolderListener, MyEntouragesAdapter.LoaderCallback {

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

    @BindView(R.id.myentourages_list_view)
    RecyclerView entouragesView;

    @BindView(R.id.myentourages_swipeRefreshLayout)
    SwipeRefreshLayout entouragesRefresh;

    private MyEntouragesAdapter entouragesAdapter;

    @BindView(R.id.myentourages_layout_no_items)
    View noItemsView;

    @BindView(R.id.myentourages_no_items_title)
    TextView noItemsTitleTextView;

    @BindView(R.id.myentourages_no_items_details)
    TextView noItemsDetailsTextView;

    private @NonNull EntouragePagination entouragesPagination = new EntouragePagination(Constants.ITEMS_PER_PAGE);

    // Refresh invitations attributes
    private Timer refreshInvitationsTimer;
    private final Handler refreshInvitationsHandler = new Handler();
    private boolean isRefreshingInvitations = false;

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

        if (getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).showEditActionZoneFragment();
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
                        break;
                    case FILTER_TAB_INDEX_UNREAD:
                        filter.setShowUnreadOnly(true);
                        EntourageEvents.logEvent(EntourageEvents.EVENT_MYENTOURAGES_FILTER_UNREAD);
                        break;
                }
                refreshMyFeeds();
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
        if(filter!=null)
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
        entouragesRefresh.setOnRefreshListener(this::refreshMyFeeds);
    }

    private void retrieveMyFeeds() {
        if (!entouragesPagination.isLoading) {
            entouragesPagination.isLoading = true;
            presenter.getMyFeeds(entouragesPagination.page, entouragesPagination.itemsPerPage);
        }
    }

    private void refreshMyFeeds() {
        entouragesRefresh.setRefreshing(true);
        presenter.clear();
        // remove the current feed
        entouragesAdapter.removeAll();
        entouragesPagination = new EntouragePagination(Constants.ITEMS_PER_PAGE);
        // request a new feed
        refreshInvitations();
        retrieveMyFeeds();
        entouragesRefresh.setRefreshing(false);
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void onMyEntouragesForceRefresh(Events.OnMyEntouragesForceRefresh event) {
        FeedItem item = event.getFeedItem();
        if(item==null) {
            refreshMyFeeds();
        } else {
            entouragesAdapter.updateCard(item);
        }
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



    @Subscribe
    public void feedItemViewRequested(Events.OnFeedItemInfoViewRequestedEvent event) {
        if(event != null && event.getFeedItem() != null) {
            onPushNotificationConsumedForFeedItem(event.getFeedItem());
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
        boolean isChatMesssage = PushNotificationContent.TYPE_NEW_CHAT_MESSAGE.equals(content.getType());

        TimestampedObject card = entouragesAdapter.findCard(cardType, joinableId);
        if (card instanceof FeedItem) {
            ((FeedItem)card).increaseBadgeCount(isChatMesssage);
            ((FeedItem)card).setLastMessage(content.message, message.getAuthor());
            //approximate message time with Now //TODO get proper time
            ((FeedItem)card).setUpdatedTime(new Date());
            entouragesAdapter.updateCard(card);
        } else {
            refreshMyFeeds();
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
        feedItemCard.decreaseBadgeCount();
        entouragesAdapter.updateCard(feedItemCard);
    }

    // ----------------------------------
    // Refresh invitations timer handling
    // ----------------------------------

    private void timerStart() {
        //create the timer
        refreshInvitationsTimer = new Timer();
        //create the task
        TimerTask refreshInvitationsTimerTask = new TimerTask() {
            @Override
            public void run() {
                refreshInvitationsHandler.post(MyEntouragesFragment.this::refreshInvitations);
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

    void onNewsfeedReceived(List<Newsfeed> newsfeedList) {
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

                // show only the unread ones if filter is set
                if (!showUnreadOnly || feedItem.getBadgeCount() > 0) {
                    if (entouragesAdapter.findCard(feedItem) == null) {
                        entouragesAdapter.addCardInfo(feedItem);
                    }
                }
                BusProvider.getInstance().post(new Events.OnMyEntouragesForceRefresh(feedItem));
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

    void onInvitationsReceived(List<Invitation> invitationList) {
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

    void onFeedItemReceived(FeedItem feedItem) {
        if (getActivity() == null || !isAdded()) return;

        if (feedItem != null) {
            TimestampedObject card = entouragesAdapter.findCard(feedItem);
            if (card instanceof FeedItem) {
                entouragesAdapter.updateCard(feedItem);
            }
        } else {
            //TODO force refresh really?
            Timber.e("shoud we refreshMyFeeds();");
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
}
