package social.entourage.android.deeplinks;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import social.entourage.android.DrawerActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.user.edit.UserEditFragment;

import static social.entourage.android.R.id.action_edit_user;

/**
 * Handles the deep links received by the app
 * Created by Mihai Ionescu on 31/10/2017.
 */

public class DeepLinksManager {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final String SCHEME_ENTOURAGE = "entourage";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private Intent deepLinkIntent;

    // ----------------------------------
    // SINGLETON
    // ----------------------------------

    private static final DeepLinksManager ourInstance = new DeepLinksManager();

    /**
     * Singleton accessor
     * @return the singleton
     */
    public static DeepLinksManager getInstance() {
        return ourInstance;
    }

    private DeepLinksManager() {

    }

    // ----------------------------------
    // DEEP LINK HANDLING
    // ----------------------------------

    /**
     * Saves the receieved deep link intent
     * @param deepLinkIntent
     */
    public void setDeepLinkIntent(final Intent deepLinkIntent) {
        this.deepLinkIntent = deepLinkIntent;
    }

    /**
     * Handles the current deep link, and sets it to null if successfully
     * @param activity
     */
    public void handleCurrentDeepLink(Activity activity) {
        if (deepLinkIntent == null || activity == null) return;
        Uri deepLinkUri = deepLinkIntent.getData();
        if (deepLinkUri == null) {
            deepLinkIntent = null;
            return;
        }
        String scheme = deepLinkUri.getScheme();
        if (scheme == null) {
            deepLinkIntent = null;
            return;
        }
        if (SCHEME_ENTOURAGE.equalsIgnoreCase(scheme)) {
            handleEntourageDeepLink(activity, deepLinkUri);
        } else {
            handleHttpDeepLink(activity, deepLinkUri);
        }
    }

    /**
     * Handles deeplinks with format "entourage://*"
     * @param activity
     * @param deepLinkUri
     */
    private void handleEntourageDeepLink(Activity activity, Uri deepLinkUri) {
        String host = deepLinkUri.getHost();
        if (host == null) {
            deepLinkIntent = null;
            return;
        }
        host = host.toLowerCase();
        if (host.equals(DeepLinksView.FEED.getView())) {
            if (activity instanceof DrawerActivity) {
                DrawerActivity drawerActivity = (DrawerActivity)activity;
                drawerActivity.switchToMapFragment();
                drawerActivity.popToMapFragment();
            } else {
                return;
            }
        }
        else if (host.equals(DeepLinksView.BADGE.getView())) {
            if (activity instanceof AppCompatActivity) {
                AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
                FragmentManager fragmentManager = appCompatActivity.getSupportFragmentManager();
                if (fragmentManager != null) {
                    UserEditFragment userEditFragment = (UserEditFragment)fragmentManager.findFragmentByTag(UserEditFragment.TAG);
                    if (userEditFragment != null) {
                        userEditFragment.onAddAssociationClicked();
                        deepLinkIntent = null;
                        return;
                    }
                }
            }
            if (activity instanceof DrawerActivity) {
                ((DrawerActivity)activity).selectItem(action_edit_user);
                return;
            }
        }
        else if (host.equals(DeepLinksView.WEBVIEW.getView())) {
            try {
                String urlToOpen = deepLinkUri.getQueryParameter("url");
                if (urlToOpen != null) {
                    if (!urlToOpen.toLowerCase().startsWith("http")) {
                        urlToOpen = "https://"+urlToOpen;
                    }
                    if (activity instanceof DrawerActivity) {
                        DrawerActivity drawerActivity = (DrawerActivity) activity;
                        drawerActivity.switchToMapFragment();
                        drawerActivity.popToMapFragment();
                        drawerActivity.showWebView(urlToOpen);
                    } else {
                        return;
                    }
                }
            } catch (Exception ex) {}
        }
        deepLinkIntent = null;
    }

    /**
     * Handles the deeplinks with format "http(s)://"
     * @param activity
     * @param deepLinkUri
     */
    private void handleHttpDeepLink(Activity activity, Uri deepLinkUri) {
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

    /**
     * Enum that contains the keywords our {@link DeepLinksManager} can manage
     */
    private enum DeepLinksView {

        ENTOURAGE("entourages"),
        FEED("feed"),
        BADGE("badge"),
        WEBVIEW("webview");

        private final String view;

        private DeepLinksView(final String view) {
            this.view = view;
        }

        public String getView() {
            return view;
        }
    }

}
