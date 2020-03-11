package social.entourage.android.deeplinks

import android.content.Intent
import android.net.Uri
import android.text.util.Linkify
import android.widget.TextView
import social.entourage.android.BuildConfig
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.api.tape.Events.OnFeedItemInfoViewRequestedEvent
import social.entourage.android.tools.BusProvider
import social.entourage.android.user.edit.UserEditFragment
import java.util.*
import java.util.regex.Pattern

/**
 * Handles the deep links received by the app
 * Created by Mihai Ionescu on 31/10/2017.
 */
class DeepLinksManager {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var deepLinkIntent: Intent? = null
    private var deepLinkUri: Uri? = null
    // ----------------------------------
    // DEEP LINK HANDLING
    // ----------------------------------
    /**
     * Saves the receieved deep link intent
     * @param deepLinkIntent
     */
    fun setDeepLinkIntent(deepLinkIntent: Intent?) {
        this.deepLinkIntent = deepLinkIntent
    }

    /**
     * Handles the current deep link, and sets it to null if successfully
     * @param activity
     */
    fun handleCurrentDeepLink(activity: MainActivity) {
        if (deepLinkIntent == null) return
        deepLinkUri = deepLinkIntent!!.data
        if (deepLinkUri == null || deepLinkUri!!.scheme == null) {
            deepLinkIntent = null
            return
        }
        if (deepLinkUri!!.scheme!!.contains(BuildConfig.DEEP_LINKS_SCHEME)) {
            handleEntourageDeepLink(activity)
        } else {
            handleHttpDeepLink(activity)
        }
    }

    /**
     * Handles deeplinks with format "entourage:// *"
     * @param activity
     */
    private fun handleEntourageDeepLink(activity: MainActivity) {
        var host = deepLinkUri!!.host
        if (host == null) {
            deepLinkIntent = null
            return
        }
        handleDeepLink(activity, host.toLowerCase(), deepLinkUri!!.pathSegments)
    }

    /**
     * Handles the deeplinks with format "http(s)://"
     * @param activity
     */
    private fun handleHttpDeepLink(activity: MainActivity) {
        val pathSegments: ArrayList<String> = ArrayList(deepLinkUri!!.pathSegments)
        if (pathSegments.size >= 2) {
            val requestedView = pathSegments[0]
            val key = pathSegments[1]
            if (requestedView.equals(DeepLinksView.ENTOURAGES.view, ignoreCase = true)) {
                BusProvider.getInstance().post(OnFeedItemInfoViewRequestedEvent(FeedItem.ENTOURAGE_CARD, "", key))
            } else if (requestedView.equals(DeepLinksView.DEEPLINK.view, ignoreCase = true)) {
                //Remove the requested view and the key from path segments
                pathSegments.removeAt(0)
                pathSegments.removeAt(0) // zero, because it was shifted when we removed requestedview
                //Handle the deep link
                handleDeepLink(activity, key, pathSegments)
                return  // we don't suppress the intent in this case
            }
        }
        deepLinkIntent = null
    }

    private fun handleDeepLink(activity: MainActivity, key: String, pathSegments: List<String>?) {
        if (key == DeepLinksView.FEED.view) {
            activity.showFeed()
            activity.dismissMapFragmentDialogs()
            if (pathSegments != null && pathSegments.isNotEmpty()) {
                val requestedView = pathSegments[0]
                if (requestedView.equals(DeepLinksView.FILTERS.view, ignoreCase = true)) {
                    activity.showMapFilters()
                }
            }
        } else if (key == DeepLinksView.BADGE.view) {
            val fragmentManager = activity.supportFragmentManager
            val userEditFragment = fragmentManager.findFragmentByTag(UserEditFragment.TAG) as UserEditFragment?
            if (userEditFragment != null) {
                userEditFragment.onAddAssociationClicked()
            } else {
                activity.selectItem(R.id.action_edit_profile)
            }
        } else if (key == DeepLinksView.WEBVIEW.view) {
            try {
                var urlToOpen = deepLinkUri!!.getQueryParameter("url")
                if (urlToOpen != null) {
                    if (!urlToOpen.toLowerCase().startsWith("http")) {
                        urlToOpen = "https://$urlToOpen"
                    }
                    activity.showFeed()
                    activity.dismissMapFragmentDialogs()
                    activity.showWebView(urlToOpen)
                }
            } catch (ignored: Exception) {
            }
        } else if (key == DeepLinksView.PROFILE.view) {
            activity.selectItem(R.id.action_edit_profile)
        } else if (key == DeepLinksView.GUIDE.view) {
            activity.showGuide()
        } else if (key == DeepLinksView.MY_CONVERSATIONS.view) {
            activity.dismissMapFragmentDialogs()
            activity.showMyEntourages()
        } else if (key == DeepLinksView.CREATE_ACTION.view) {
            activity.createEntourage()
        } else if (key == DeepLinksView.ENTOURAGE.view) {
            if (pathSegments != null && pathSegments.isNotEmpty()) {
                BusProvider.getInstance().post(OnFeedItemInfoViewRequestedEvent(FeedItem.ENTOURAGE_CARD, "", pathSegments[0]))
            }
        } else if (key == DeepLinksView.TUTORIAL.view) {
            activity.showTutorial(true)
        }
        deepLinkIntent = null
    }

    /**
     * Enum that contains the keywords our [DeepLinksManager] can manage
     */
    private enum class DeepLinksView(val view: String) {
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

    }

    companion object {
        /**
         * Singleton accessor
         * @return the singleton
         */
        val instance = DeepLinksManager()

        /**
         * Linkify the textview, by adding the app deep link
         * @param textView textview to be linkified
         */
        @JvmStatic
        fun linkify(textView: TextView) {
            val pattern = Pattern.compile(BuildConfig.DEEP_LINKS_SCHEME + "://" + DeepLinksView.ENTOURAGE.view + "/[0-9]+")
            Linkify.addLinks(textView, Linkify.ALL) // to add support for standard URLs, emails, phones a.s.o.
            Linkify.addLinks(textView, pattern, null)
        }
    }
}