package social.entourage.android.map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.TourAuthor;
import social.entourage.android.api.tape.Events;
import social.entourage.android.location.LocationUpdateListener;
import social.entourage.android.tour.TourService;
import timber.log.Timber;

import static social.entourage.android.tour.TourService.KEY_LOCATION_PROVIDER_DISABLED;

public class MapVoisinage extends MapEntourageFragment implements LocationUpdateListener {
    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    //public static final String TAG = "social.entourage.android.fragment_map_voisinage";

    private ServiceConnection connection = new ServiceConnection();
    private boolean isBound;
    private boolean shouldShowGPSDialog = true;

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
        if (isBound && tourService != null) {
            tourService.unregisterLocationUpdateListener(MapVoisinage.this);
            doUnbindService();
        }
        super.onDestroy();
    }

    @Override
    public void onLocationStatusUpdated(boolean active) {
        super.onLocationStatusUpdated(active);
        if(shouldShowGPSDialog && !active &&  tourService!=null && tourService.isRunning()) {
            //We always need GPS to be turned on during tour
            shouldShowGPSDialog = false;
            final Intent newIntent = new Intent(this.getContext(), DrawerActivity.class);
            newIntent.setAction(KEY_LOCATION_PROVIDER_DISABLED);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(newIntent);
        } else if(!shouldShowGPSDialog && active) {
            shouldShowGPSDialog = true;
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
    // BUS LISTENERS
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

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------

    @Subscribe
    @Override
    public void onMapTabChanged(Events.OnMapTabSelected event) {
        super.onMapTabChanged(event);
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
                Intent intent = new Intent(getActivity(), TourService.class);
                getActivity().startService(intent);
                getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
            } catch(IllegalStateException e) {
                Timber.e(e);
            }
        }
    }

    private void doUnbindService() {
        if (getActivity() != null && isBound) {
            getActivity().unbindService(connection);
            isBound = false;
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
            tourService = ((TourService.LocalBinder) service).getService();
            if(tourService==null) {
                Timber.e("Tour service not found");
                return;
            }
            tourService.registerLocationUpdateListener(MapVoisinage.this);
            tourService.registerNewsFeedListener(MapVoisinage.this);

            tourService.updateNewsfeed(pagination, selectedTab);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tourService.unregisterLocationUpdateListener(MapVoisinage.this);
            tourService.unregisterNewsFeedListener(MapVoisinage.this);
            tourService = null;
        }
    }

}
