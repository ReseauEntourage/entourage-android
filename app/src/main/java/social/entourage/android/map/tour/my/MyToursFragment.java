package social.entourage.android.map.tour.my;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.base.EntouragePagination;
import social.entourage.android.newsfeed.NewsfeedAdapter;

public class MyToursFragment extends EntourageDialogFragment implements TabHost.OnTabChangeListener {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.mytours";

    private static final int MAX_SCROLL_DELTA_Y = 20;

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private OnFragmentInteractionListener mListener;

    @Inject
    MyToursPresenter presenter;

    @BindView(R.id.mytours_tabHost)
    TabHost tabHost;

//    @BindView(R.id.mytours_ongoing)
//    RecyclerView ongoingToursRecyclerView;
//
//    ToursAdapter ongoingToursAdapter;

    @BindView(R.id.mytours_active)
    RecyclerView activeFeedsRecyclerView;

    NewsfeedAdapter activeFeedsAdapter;

    @BindView(R.id.mytours_frozen)
    RecyclerView frozenFeedsRecyclerView;

    NewsfeedAdapter frozenFeedsAdapter;

    @BindView(R.id.mytours_progress_bar)
    ProgressBar progressBar;

    private int apiRequestsCount = 0;

//    private EntouragePagination ongoingToursPagination= new EntouragePagination(Constants.ITEMS_PER_PAGE);
    private EntouragePagination activeFeedsPagination = new EntouragePagination(Constants.ITEMS_PER_PAGE);
    private EntouragePagination frozenFeedsPagination = new EntouragePagination(Constants.ITEMS_PER_PAGE);

    private HashMap<String, EntouragePagination> paginationHashMap = new HashMap<>();

    private int scrollDeltaY;
    private OnScrollListener scrollListener = new OnScrollListener();

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public MyToursFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_tours, container, false);
        ButterKnife.bind(this, view);
        initializeView();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());

        retrieveMyFeeds(tabHost.getCurrentTabTag());
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerMyToursComponent.builder()
                .entourageComponent(entourageComponent)
                .myToursModule(new MyToursModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();

//        ongoingToursRecyclerView.addOnScrollListener(scrollListener);
        activeFeedsRecyclerView.addOnScrollListener(scrollListener);
        frozenFeedsRecyclerView.addOnScrollListener(scrollListener);
    }

    @Override
    public void onStop() {
        super.onStop();

//        ongoingToursRecyclerView.removeOnScrollListener(scrollListener);
        activeFeedsRecyclerView.removeOnScrollListener(scrollListener);
        frozenFeedsRecyclerView.removeOnScrollListener(scrollListener);
    }

    @Override
    protected ColorDrawable getBackgroundDrawable() {
        return new ColorDrawable(getResources().getColor(R.color.background));
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private void initializeView() {
        initializeTabHost();
        initializeRecyclerViews();
    }

    private void initializeTabHost() {
        tabHost.setup();
        //setupTab(R.id.mytours_ongoing_layout, FeedItem.STATUS_ON_GOING, getString(R.string.mytours_ongoing));
        setupTab(R.id.mytours_active_layout, Newsfeed.STATUS_ACTIVE, getString(R.string.mytours_recorded));
        setupTab(R.id.mytours_frozen_layout, Newsfeed.STATUS_CLOSED, getString(R.string.mytours_frozen));

        TabWidget tabWidget = tabHost.getTabWidget();
        tabWidget.getChildTabViewAt(0).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_tabitem_left));
        tabWidget.getChildTabViewAt(tabWidget.getTabCount() - 1).setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_tabitem_right));

        tabHost.setOnTabChangedListener(this);
    }

    private void setupTab(@IdRes int viewId, String tag, String text) {
        View tabView = createTabView(tabHost.getContext(), text);
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(tag);
        tabSpec.setIndicator(tabView);
        tabSpec.setContent(viewId);
        tabHost.addTab(tabSpec);
    }

    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_mytours_tab_item, null);
        TextView tv = (TextView) view.findViewById(R.id.tabitem_textview);
        tv.setText(text);
        return view;
    }

    private void initializeRecyclerViews() {
//        ongoingToursRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        ongoingToursAdapter = new ToursAdapter();
//        ongoingToursRecyclerView.setAdapter(ongoingToursAdapter);
//        paginationHashMap.put(FeedItem.STATUS_ON_GOING, ongoingToursPagination);

        activeFeedsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activeFeedsAdapter = new NewsfeedAdapter();
        activeFeedsRecyclerView.setAdapter(activeFeedsAdapter);
        paginationHashMap.put(Newsfeed.STATUS_ACTIVE, activeFeedsPagination);

        frozenFeedsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        frozenFeedsAdapter = new NewsfeedAdapter();
        frozenFeedsRecyclerView.setAdapter(frozenFeedsAdapter);
        paginationHashMap.put(Newsfeed.STATUS_CLOSED, frozenFeedsPagination);
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

    private void retrieveMyFeeds(String status) {
        EntouragePagination pagination = paginationHashMap.get(status);
        if (pagination != null && !pagination.isLoading) {
            showProgressBar();
            pagination.isLoading = true;
            presenter.getMyFeeds(pagination.page, pagination.itemsPerPage, status);
        }
    }

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------

    protected void onNewsfeedReceived(List<Newsfeed> newsfeedList, String status) {
        hideProgressBar();
        //reset the loading indicator
        EntouragePagination pagination = paginationHashMap.get(status);
        if (pagination != null) {
            pagination.isLoading = false;
        }
        //ignore errors
        if (newsfeedList == null) return;
        //add the tours
        if (newsfeedList.size() > 0) {
            EntourageApplication application = EntourageApplication.get(getContext());

            for (final Newsfeed newsfeed : newsfeedList) {
                Object feedData = newsfeed.getData();
                if (feedData == null || !(feedData instanceof FeedItem)) {
                    continue;
                }
                FeedItem feedItem = (FeedItem) newsfeed.getData();
                if (application != null) {
                    application.updateBadgeCountForFeedItem(feedItem);
                }
//                if (tour.getTourStatus().equals(FeedItem.STATUS_ON_GOING)) {
//                    ongoingToursAdapter.add(tour);
//                }
                if (status.equals(Newsfeed.STATUS_ACTIVE)) {
                    if (activeFeedsAdapter.findCard(feedItem) == null) {
                        activeFeedsAdapter.addCardInfoBeforeTimestamp(feedItem);
                    } else {
                        activeFeedsAdapter.updateCard(feedItem);
                    }
                } else if (status.equals(Newsfeed.STATUS_CLOSED)) {
                    if (frozenFeedsAdapter.findCard(feedItem) == null) {
                        frozenFeedsAdapter.addCardInfoBeforeTimestamp(feedItem);
                    } else {
                        frozenFeedsAdapter.updateCard(feedItem);
                    }
                }
            }
            //increase page and items count
            if (pagination != null) {
                pagination.loadedItems(newsfeedList.size());
            }
        }
    }

    // ----------------------------------
    // BUTTONS HANDLING
    // ----------------------------------

    @OnClick(R.id.mytours_close_button)
    protected void onCloseButton() {
        dismiss();
    }

    // ----------------------------------
    // OnTabChangedListener
    // ----------------------------------

    @Override
    public void onTabChanged(final String tabId) {

        String currentTabTag = tabHost.getCurrentTabTag();
        EntouragePagination pagination = paginationHashMap.get(currentTabTag);
        if (pagination != null) {
            //refresh current page
            if (pagination.page > 1) {
                pagination.page --;
            }
            retrieveMyFeeds(currentTabTag);
        }
        scrollDeltaY = 0;
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

        TimestampedObject card = activeFeedsAdapter.findCard(cardType, joinableId);
        if (card != null && card instanceof FeedItem) {
            ((FeedItem)card).increaseBadgeCount();
            activeFeedsAdapter.updateCard(card);
            return;
        }
        card = frozenFeedsAdapter.findCard(cardType, joinableId);
        if (card != null && card instanceof FeedItem) {
            ((FeedItem)card).increaseBadgeCount();
            frozenFeedsAdapter.updateCard(card);
        }
    }

    public void onPushNotificationConsumedForTour(long tourId) {
        TimestampedObject card = activeFeedsAdapter.findCard(TimestampedObject.TOUR_CARD, tourId);
        if (card != null && card instanceof Tour) {
            ((Tour)card).setBadgeCount(0);
            activeFeedsAdapter.updateCard(card);
            return;
        }
        card = frozenFeedsAdapter.findCard(TimestampedObject.TOUR_CARD, tourId);
        if (card != null && card instanceof Tour) {
            ((Tour)card).setBadgeCount(0);
            frozenFeedsAdapter.updateCard(card);
        }
    }

    // ----------------------------------
    // ACTIVITY INTERFACE
    // ----------------------------------

    public interface OnFragmentInteractionListener {
        void onShowTourInfo(Tour tour);
    }

    // ----------------------------------
    // PRIVATE CLASSES
    // ----------------------------------

    private class OnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {

            scrollDeltaY += dy;
            if (dy > 0 && scrollDeltaY > MAX_SCROLL_DELTA_Y) {
                String currentTabTag = tabHost.getCurrentTabTag();
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int position = linearLayoutManager.findLastVisibleItemPosition();
                if (position == recyclerView.getAdapter().getItemCount()-1) {
                    retrieveMyFeeds(currentTabTag);
                }

                scrollDeltaY = 0;
            }
        }
        @Override
        public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
        }
    }
}
