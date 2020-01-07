package social.entourage.android.map;

import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.tape.Events;
import social.entourage.android.entourage.EntourageCloseFragment;
import social.entourage.android.newsfeed.FeedItemOptionsFragment;

import com.squareup.otto.Subscribe;

public class MapVoisinageFragment extends MapEntourageFragment {

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

    @Subscribe
    @Override
    public void feedItemCloseRequested(Events.OnFeedItemCloseRequestEvent event) {
        super.feedItemCloseRequested(event);
    }
}
