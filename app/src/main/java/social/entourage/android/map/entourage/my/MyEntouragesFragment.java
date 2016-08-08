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

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.base.EntourageBaseAdapter;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.R;
import social.entourage.android.base.EntouragePagination;
import social.entourage.android.invite.view.InvitationsAdapter;
import social.entourage.android.newsfeed.NewsfeedAdapter;

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

    @Bind(R.id.myentourages_invitations_view)
    RecyclerView invitationsView;

    InvitationsAdapter invitationsAdapter;

    @Bind(R.id.myentourages_list_view)
    RecyclerView entouragesView;

    NewsfeedAdapter entouragesAdapter;

    @Bind(R.id.myentourages_progressBar)
    ProgressBar progressBar;

    private int apiRequestsCount = 0;

    private EntouragePagination entouragesPagination = new EntouragePagination(Constants.ITEMS_PER_PAGE);

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
        initializeInvitationsView();
        initializeEntouragesView();
    }

    private void initializeInvitationsView() {
        invitationsView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        invitationsAdapter = new InvitationsAdapter();
        invitationsView.setAdapter(invitationsAdapter);
    }

    private void initializeEntouragesView() {
        entouragesView.setLayoutManager(new LinearLayoutManager(getContext()));
        entouragesAdapter = new NewsfeedAdapter();
        entouragesView.setAdapter(entouragesAdapter);
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

    // ----------------------------------
    // BUTTONS HANDLING
    // ----------------------------------

    @OnClick(R.id.myentourages_back_button)
    void onBackClicked() {
        dismiss();
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
                        if (presenter != null) {
                            if (!isRefreshingInvitations) {
                                presenter.getMyInvitations();
                                isRefreshingInvitations = true;
                            }
                        }
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

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------

    protected void onNewsfeedReceived(List<Newsfeed> newsfeedList) {
        hideProgressBar();
        //reset the loading indicator
        if (entouragesPagination != null) {
            entouragesPagination.isLoading = false;
        }
        //ignore errors
        if (newsfeedList == null) return;
        //add the feed
        if (newsfeedList.size() > 0) {
            DrawerActivity activity = null;
            if (getActivity() instanceof DrawerActivity) {
                activity = (DrawerActivity) getActivity();
            }
            Iterator<Newsfeed> iterator = newsfeedList.iterator();
            while (iterator.hasNext()) {
                Newsfeed newsfeed = iterator.next();
                Object feedData = newsfeed.getData();
                if (feedData == null || !(feedData instanceof FeedItem)) {
                    continue;
                }
                FeedItem feedItem = (FeedItem)newsfeed.getData();
                // TODO Badge count for entourages
                if (activity != null && feedItem.getType() == TimestampedObject.TOUR_CARD) {
                    feedItem.setBadgeCount(activity.getPushNotificationsCountForTour(feedItem.getId()));
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
        // ignore errors
        if (invitationList == null) {
            return;
        }
        // add the invitations
        Iterator<Invitation> iterator = invitationList.iterator();
        while (iterator.hasNext()) {
            Invitation invitation = iterator.next();
            if (invitationsAdapter.findCard(invitation) == null) {
                invitationsAdapter.addCardInfoBeforeTimestamp(invitation);
            }
            else {
                invitationsAdapter.updateCard(invitation);
            }
        }
    }

}
