package social.entourage.android.map;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;

import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageEvents;
import social.entourage.android.PlusFragment;
import social.entourage.android.R;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourAuthor;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.Events;
import social.entourage.android.entourage.EntourageCloseFragment;
import social.entourage.android.message.push.PushNotificationManager;
import social.entourage.android.newsfeed.FeedItemOptionsFragment;
import social.entourage.android.service.EntourageService;
import social.entourage.android.service.EntourageServiceListener;
import social.entourage.android.tour.join.TourJoinRequestFragment;
import social.entourage.android.view.EntourageSnackbar;
import timber.log.Timber;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapVoisinageFragment extends MapFragment implements EntourageServiceListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private ServiceConnection connection = new ServiceConnection();
    private boolean isBound;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isBound) {
            doBindService();
        }
    }

    @Override
    public void onDestroy() {
        if (isBound && entourageService != null) {
            entourageService.unregisterServiceListener(this);
            doUnbindService();
        }
        super.onDestroy();
    }

    private void checkAction(@NonNull String action) {
        if (getActivity() != null && isBound) {
            // 1 : Check if should Resume tour
            if (PlusFragment.KEY_CREATE_OUTING.equals(action)) {
                createAction(null, Entourage.TYPE_OUTING);
            }
        }
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void onUserInfoUpdated(Events.OnUserInfoUpdatedEvent event) {
        User me = EntourageApplication.me(getContext());
        if (me == null || newsfeedAdapter == null) return;
        TourAuthor meAsAuthor = me.asTourAuthor();
        List<TimestampedObject> dirtyList = new ArrayList<>();
        // See which cards needs updating
        for (TimestampedObject timestampedObject : newsfeedAdapter.getItems()) {
            if (!(timestampedObject instanceof FeedItem)) continue;
            FeedItem feedItem = (FeedItem)timestampedObject;
            TourAuthor author = feedItem.getAuthor();
            // Skip null author
            if (author == null) continue;
            // Skip not same author id
            if (author.getUserID() != meAsAuthor.getUserID()) continue;
            // Skip if nothing changed
            if (author.isSame(meAsAuthor)) continue;
            // Update the tour author
            meAsAuthor.setUserName(author.getUserName());
            feedItem.setAuthor(meAsAuthor);
            // Mark as dirty
            dirtyList.add(feedItem);
        }
        // Update the dirty cards
        for (TimestampedObject dirty : dirtyList) {
            newsfeedAdapter.updateCard(dirty);
        }
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------

    // ----------------------------------
    // BUS LISTENERS : needs to be in final class (not in parent class
    // ----------------------------------
    @Subscribe
    @Override
    public void onMyEntouragesForceRefresh(Events.OnMyEntouragesForceRefresh event) {
        super.onMyEntouragesForceRefresh(event);
    }

    @Subscribe
    @Override
    public void onEntourageCreated(Events.OnEntourageCreated event) {
        super.onEntourageCreated(event);
    }

    @Subscribe
    @Override
    public void onEntourageUpdated(Events.OnEntourageUpdated event) {
        super.onEntourageUpdated(event);
    }

    @Subscribe
    @Override
    public void onNewsfeedLoadMoreRequested(Events.OnNewsfeedLoadMoreEvent event) {
        super.onNewsfeedLoadMoreRequested(event);
    }

    @Subscribe
    @Override
    public void onMapFilterChanged(Events.OnMapFilterChanged event) {
        super.onMapFilterChanged(event);
    }

    @Subscribe
    @Override
    public void onBetterLocation(Events.OnBetterLocationEvent event) {
        super.onBetterLocation(event);
    }

    @Subscribe
    public void feedItemViewRequested(Events.OnFeedItemInfoViewRequestedEvent event) {
        super.feedItemViewRequested(event);
    }

    @Subscribe
    public void userActRequested(final Events.OnUserActEvent event) {
        super.userActRequested(event);
    }

    @Override
    public void onFeedItemClosed(boolean closed, @NonNull FeedItem feedItem) {
        if (closed) {
            refreshFeed();
            if(layoutMain!=null){
                EntourageSnackbar.INSTANCE.make(layoutMain, feedItem.getClosedToastMessage(), Snackbar.LENGTH_SHORT).show();
            }
        }

        if (loaderStop != null) {
            loaderStop.dismiss();
            loaderStop = null;
        }
    }

    @Override
    public void onUserStatusChanged(@NonNull TourUser user, @NonNull FeedItem feedItem) {
        if (getActivity() == null || getActivity().isFinishing()) return;
        if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
            feedItem.setJoinStatus(user.getStatus());
            if (user.getStatus().equals(Tour.JOIN_STATUS_PENDING)) {
                try {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                    JoinRequestOkFragment joinRequestOkFragment = JoinRequestOkFragment.newInstance(feedItem);
//                    joinRequestOkFragment.show(fragmentManager, JoinRequestOkFragment.TAG);
                    TourJoinRequestFragment tourJoinRequestFragment = TourJoinRequestFragment.newInstance(feedItem);
                    tourJoinRequestFragment.show(fragmentManager, TourJoinRequestFragment.TAG);
                } catch (IllegalStateException e) {
                    Timber.w(e);
                }
            }
        }
        updateNewsfeedJoinStatus(feedItem);
        isRequestingToJoin--;
    }

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------

    @Override
    protected void redrawWholeNewsfeed(@NotNull List<? extends Newsfeed> newsFeeds) {
        if (map != null && newsFeeds.size() > 0 && newsfeedAdapter!=null) {
            //redraw the whole newsfeed
            for (TimestampedObject timestampedObject : newsfeedAdapter.getItems()) {
                if (timestampedObject.getType() == TimestampedObject.ENTOURAGE_CARD) {
                    drawNearbyEntourage((Entourage) timestampedObject);
                }
            }
            mapClusterManager.cluster();
        }
    }

    @Subscribe
    @Override
    public void onLocationPermissionGranted(Events.OnLocationPermissionGranted event) {
        super.onLocationPermissionGranted(event);
    }

    private void updateNewsfeedJoinStatus(TimestampedObject timestampedObject) {
        if(newsfeedAdapter!=null) {
            newsfeedAdapter.updateCard(timestampedObject);
        }
    }

    // ----------------------------------
    // SERVICE BINDING METHODS
    // ----------------------------------

    private void doBindService() {
        if (getActivity() != null) {
            User me = EntourageApplication.me(getActivity());
            if (me == null) {
                // Don't start the service
                return;
            }
            try{
                Intent intent = new Intent(getActivity(), EntourageService.class);
                getActivity().startService(intent);
                getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
            } catch(IllegalStateException e) {
                Timber.w(e);
            }
        }
    }

    private void doUnbindService() {
        if (getActivity() != null && isBound) {
            getActivity().unbindService(connection);
            isBound = false;
        }
    }

    @Override
    protected void displayFeedItemOptions(final FeedItem feedItem) {
        if (getActivity() != null ) {
            if (!feedItem.isMine(getContext()) || feedItem.isFreezed() || !feedItem.canBeClosed()) {
                FeedItemOptionsFragment feedItemOptionsFragment = FeedItemOptionsFragment.newInstance(feedItem);
                feedItemOptionsFragment.show(getActivity().getSupportFragmentManager(), FeedItemOptionsFragment.TAG);
                return;
            }
            EntourageCloseFragment entourageCloseFragment = EntourageCloseFragment.newInstance(feedItem);
            entourageCloseFragment.show(getActivity().getSupportFragmentManager(), EntourageCloseFragment.TAG, getContext());
        }
    }

    @Subscribe
    @Override
    public void feedItemCloseRequested(Events.OnFeedItemCloseRequestEvent event) {
        super.feedItemCloseRequested(event);
    }

    @Subscribe
    public void checkIntentAction(Events.OnCheckIntentActionEvent event) {
        if(getActivity()==null) {
            Timber.w("No activity found");
            return;
        }
        Intent intent = getActivity().getIntent();

        if(intent.getAction()!=null) {
            checkAction(intent.getAction());
        }

        Message message = null;
        if (intent.getExtras() != null) {
            message = (Message) intent.getExtras().getSerializable(PushNotificationManager.PUSH_MESSAGE);
        }
        if (message != null) {
            PushNotificationContent content = message.getContent();
            if (content != null) {
                PushNotificationContent.Extra extra = content.extra;
                switch(intent.getAction()) {
                    case PushNotificationContent.TYPE_NEW_CHAT_MESSAGE:
                    case PushNotificationContent.TYPE_NEW_JOIN_REQUEST:
                    case PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED:
                        if (content.isTourRelated()) {
                            displayChosenFeedItem(content.getJoinableUUID(), TimestampedObject.TOUR_CARD);
                        } else if (content.isEntourageRelated()) {
                            displayChosenFeedItem(content.getJoinableUUID(), TimestampedObject.ENTOURAGE_CARD);
                        }
                        break;
                    case PushNotificationContent.TYPE_ENTOURAGE_INVITATION:
                        if (extra != null) {
                            displayChosenFeedItem(String.valueOf(extra.entourageId), TimestampedObject.ENTOURAGE_CARD, extra.invitationId);
                        }
                        break;
                    case PushNotificationContent.TYPE_INVITATION_STATUS:
                        if (extra != null && (content.isEntourageRelated() || content.isTourRelated())) {
                            displayChosenFeedItem(content.getJoinableUUID(), content.isTourRelated() ? TimestampedObject.TOUR_CARD : TimestampedObject.ENTOURAGE_CARD);
                        }
                        break;
                }
            }
        }
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private class ServiceConnection implements android.content.ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (getActivity() == null) {
                Timber.e("No activity for service");
                return;
            }
            entourageService = ((EntourageService.LocalBinder) service).getService();
            if(entourageService ==null) {
                Timber.e("Service not found");
                return;
            }
            entourageService.registerServiceListener(MapVoisinageFragment.this);
            entourageService.registerApiListener(MapVoisinageFragment.this);

            if (entourageService.isRunning()) {
                updateFloatingMenuOptions();
            }

            entourageService.updateNewsfeed(pagination, selectedTab);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            entourageService.unregisterServiceListener(MapVoisinageFragment.this);
            entourageService.unregisterApiListener(MapVoisinageFragment.this);
            entourageService = null;
        }
    }

}
