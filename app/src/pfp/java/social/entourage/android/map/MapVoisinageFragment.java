package social.entourage.android.map;

import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.tape.Events;
import social.entourage.android.entourage.EntourageCloseFragment;
import social.entourage.android.newsfeed.FeedItemOptionsFragment;

import com.squareup.otto.Subscribe;

public class MapVoisinageFragment extends MapFragment {

    // ----------------------------------
    // DISPLAY SCREENS METHODS
    // ----------------------------------

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
    public void onBetterLocation(Events.OnBetterLocationEvent event) {
        super.onBetterLocation(event);
    }

    @Subscribe
    @Override
    public void onEntourageCreated(Events.OnEntourageCreated event) {
        super.onEntourageCreated(event);
    }

    @Subscribe
    @Override
    public void feedItemCloseRequested(Events.OnFeedItemCloseRequestEvent event) {
        super.feedItemCloseRequested(event);
    }

    @Subscribe
    @Override
    public void onEntourageUpdated(Events.OnEntourageUpdated event) {
        super.onEntourageUpdated(event);
    }

    @Subscribe
    @Override
    public void onMapFilterChanged(Events.OnMapFilterChanged event) {
        super.onMapFilterChanged(event);
    }

    @Subscribe
    @Override
    public void onNewsfeedLoadMoreRequested(Events.OnNewsfeedLoadMoreEvent event) {
        super.onNewsfeedLoadMoreRequested(event);
    }

    @Subscribe
    @Override
    public void onLocationPermissionGranted(Events.OnLocationPermissionGranted event) {
        super.onLocationPermissionGranted(event);
    }


    @Subscribe
    public void userActRequested(final Events.OnUserActEvent event) {
        super.userActRequested(event);
    }

    @Subscribe
    public void feedItemViewRequested(Events.OnFeedItemInfoViewRequestedEvent event) {
        super.feedItemViewRequested(event);
    }
}
