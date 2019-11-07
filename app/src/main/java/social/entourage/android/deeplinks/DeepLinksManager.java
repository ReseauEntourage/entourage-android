package social.entourage.android.deeplinks;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.text.util.Linkify;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import social.entourage.android.BuildConfig;
import social.entourage.android.DrawerActivity;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.user.edit.UserEditFragment;

import static social.entourage.android.R.id.action_edit_profile;

/**
 * Handles the deep links received by the app
 * Created by Mihai Ionescu on 31/10/2017.
 */

public class DeepLinksManager {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private Intent deepLinkIntent;
    private Uri deepLinkUri;

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
        deepLinkUri = deepLinkIntent.getData();
        if (deepLinkUri == null) {
            deepLinkIntent = null;
            return;
        }
        String scheme = deepLinkUri.getScheme();
        if (scheme == null) {
            deepLinkIntent = null;
            return;
        }
        if (scheme.contains(BuildConfig.DEEP_LINKS_SCHEME)) {
            handleEntourageDeepLink(activity);
        } else {
            handleHttpDeepLink(activity);
        }
    }

    /**
     * Handles deeplinks with format "entourage://*"
     * @param activity
     */
    private void handleEntourageDeepLink(Activity activity) {
        String host = deepLinkUri.getHost();
        if (host == null) {
            deepLinkIntent = null;
            return;
        }
        host = host.toLowerCase();
        handleDeepLink(activity, host, deepLinkUri.getPathSegments());
    }

    /**
     * Handles the deeplinks with format "http(s)://"
     * @param activity
     */
    private void handleHttpDeepLink(Activity activity) {
        List<String> pathSegments = new ArrayList<>(deepLinkUri.getPathSegments());
        if (pathSegments != null && pathSegments.size() >= 2) {
            String requestedView = pathSegments.get(0);
            String key = pathSegments.get(1);
            if (requestedView.equalsIgnoreCase(DeepLinksView.ENTOURAGES.getView())) {
                BusProvider.getInstance().post(new Events.OnFeedItemInfoViewRequestedEvent(FeedItem.ENTOURAGE_CARD, "", key));
            }
            else if (requestedView.equalsIgnoreCase(DeepLinksView.DEEPLINK.getView())) {
                //Remove the requested view and the key from path segments
                pathSegments.remove(0);
                pathSegments.remove(0); // zero, because it was shifted when we removed requestedview
                //Handle the deep link
                handleDeepLink(activity, key, pathSegments);
                return; // we don't suppress the intent in this case
            }
        }
        deepLinkIntent = null;
    }

    private void handleDeepLink(Activity activity, String key, List<String> pathSegments) {
        if (key.equals(DeepLinksView.FEED.getView())) {
            if (activity instanceof DrawerActivity) {
                DrawerActivity drawerActivity = (DrawerActivity)activity;
                drawerActivity.showFeed(false);
                drawerActivity.popToMapFragment();
                if (pathSegments != null && pathSegments.size() >= 1) {
                    String requestedView = pathSegments.get(0);
                    if (requestedView.equalsIgnoreCase(DeepLinksView.FILTERS.getView())) {
                        drawerActivity.showMapFilters();
                    }
                }
            } else {
                return;
            }
        }
        else if (key.equals(DeepLinksView.BADGE.getView())) {
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
                ((DrawerActivity)activity).selectItem(action_edit_profile);
                return;
            }
        }
        else if (key.equals(DeepLinksView.WEBVIEW.getView())) {
            try {
                String urlToOpen = deepLinkUri.getQueryParameter("url");
                if (urlToOpen != null) {
                    if (!urlToOpen.toLowerCase().startsWith("http")) {
                        urlToOpen = "https://"+urlToOpen;
                    }
                    if (activity instanceof DrawerActivity) {
                        DrawerActivity drawerActivity = (DrawerActivity) activity;
                        drawerActivity.showFeed(false);
                        drawerActivity.popToMapFragment();
                        drawerActivity.showWebView(urlToOpen);
                    } else {
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }
        else if (key.equals(DeepLinksView.PROFILE.getView())) {
            if (activity instanceof DrawerActivity) {
                ((DrawerActivity)activity).selectItem(action_edit_profile);
            } else {
                return;
            }
        }
        else if (key.equals(DeepLinksView.GUIDE.getView())) {
            if (activity instanceof DrawerActivity) {
                DrawerActivity drawerActivity = (DrawerActivity)activity;
                if (!drawerActivity.isGuideShown()) {
                    drawerActivity.showGuide();
                }
            } else {
                return;
            }
        }
        else if (key.equals(DeepLinksView.MY_CONVERSATIONS.getView())) {
            if (activity instanceof DrawerActivity) {
                DrawerActivity drawerActivity = (DrawerActivity)activity;
                drawerActivity.popToMapFragment();
                drawerActivity.showMyEntourages();
            } else {
                return;
            }
        }
        else if (key.equals(DeepLinksView.CREATE_ACTION.getView())) {
            if (activity instanceof DrawerActivity) {
                DrawerActivity drawerActivity = (DrawerActivity)activity;
                drawerActivity.showFeed(false);
                drawerActivity.popToMapFragment();
                drawerActivity.onCreateEntourageDeepLink();
            } else {
                return;
            }
        }
        else if (key.equals(DeepLinksView.ENTOURAGE.getView())) {
            if (activity instanceof DrawerActivity) {
                if (pathSegments != null && pathSegments.size() >= 1) {
                    String entourage = pathSegments.get(0);
                    BusProvider.getInstance().post(new Events.OnFeedItemInfoViewRequestedEvent(FeedItem.ENTOURAGE_CARD, "", entourage));
                }
            } else {
                return;
            }
        }
        else if (key.equals(DeepLinksView.TUTORIAL.getView())) {
            if (activity instanceof DrawerActivity) {
                DrawerActivity drawerActivity = (DrawerActivity)activity;
                drawerActivity.showTutorial(true);
            } else {
                return;
            }
        }
        deepLinkIntent = null;
    }

    /**
     * Linkify the textview, by adding the app deep link
     * @param textView textview to be linkified
     */
    public static void linkify(TextView textView) {
        Pattern pattern = Pattern.compile(BuildConfig.DEEP_LINKS_SCHEME + "://" + DeepLinksView.ENTOURAGE.view + "/[0-9]+");
        Linkify.addLinks(textView, Linkify.ALL); // to add support for standard URLs, emails, phones a.s.o.
        Linkify.addLinks(textView, pattern, null);
    }

    /**
     * Enum that contains the keywords our {@link DeepLinksManager} can manage
     */
    private enum DeepLinksView {

        DEEPLINK("deeplink"),
        ENTOURAGE("entourage"),
        ENTOURAGES("entourages"),
        FEED("feed"),
        BADGE("badge"),
        WEBVIEW("webview"),
        PROFILE("profile"),
        FILTERS("filters"),
        GUIDE("guide"),
        MY_CONVERSATIONS("messages"),
        CREATE_ACTION("create-action"),
        TUTORIAL("tutorial");

        private final String view;

        DeepLinksView(final String view) {
            this.view = view;
        }

        public String getView() {
            return view;
        }
    }

}
