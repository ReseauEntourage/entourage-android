package social.entourage.android.deeplinks;

import android.content.Intent;
import android.net.Uri;

import java.util.List;

import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

/**
 * Handles the deep links received by the app
 * Created by Mihai Ionescu on 31/10/2017.
 */

public class DeepLinksManager {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private Intent deepLinkIntent;

    // ----------------------------------
    // SINGLETON
    // ----------------------------------

    private static final DeepLinksManager ourInstance = new DeepLinksManager();

    public static DeepLinksManager getInstance() {
        return ourInstance;
    }

    private DeepLinksManager() {

    }

    // ----------------------------------
    // DEEP LINK HANDLING
    // ----------------------------------


    public void setDeepLinkIntent(final Intent deepLinkIntent) {
        this.deepLinkIntent = deepLinkIntent;
    }

    public void handleCurrentDeepLink() {
        if (deepLinkIntent == null) return;
        Uri deepLinkUri = deepLinkIntent.getData();
        if (deepLinkUri == null) {
            deepLinkIntent = null;
            return;
        }
        List<String> pathSegments = deepLinkUri.getPathSegments();
        if (pathSegments != null && pathSegments.size() >= 2) {
            String requestedView = pathSegments.get(0);
            String key = pathSegments.get(1);
            if (requestedView.equalsIgnoreCase(DeepLinksView.ENTOURAGE.getView())) {
                BusProvider.getInstance().post(new Events.OnFeedItemInfoViewRequestedEvent(FeedItem.ENTOURAGE_CARD, key));
            }
        }
        deepLinkIntent = null;
    }

    private enum DeepLinksView {

        ENTOURAGE("entourages");

        private final String view;

        private DeepLinksView(final String view) {
            this.view = view;
        }

        public String getView() {
            return view;
        }
    }

}
