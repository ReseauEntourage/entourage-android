package social.entourage.android.deeplinks

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.widget.TextView
import social.entourage.android.BuildConfig
import social.entourage.android.MainActivity
import social.entourage.android.notifications.EntourageFirebaseMessagingService
import java.util.Locale

/**
 * Handles the deep links received by the app
 * Created by Mihai Ionescu on 31/10/2017.
 *
 * entourage://...
 * http://websiteurl/key/info
 * https://websiteurl/key/info
 * /feed/
 * /feed/filters
 * /badge
 * /webview/...url=...
 * adb shell am start -W -a android.intent.action.VIEW -d "example://gizmos" com.example.android
 */
object DeepLinksManager {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var intent: Intent? = null
    private var currentUri: Uri? = null
    // ----------------------------------
    // DEEP LINK HANDLING
    // ----------------------------------

    /**
     * Handles the current deep link, and sets it to null if successfully
     * @param activity
     */
    fun handleCurrentDeepLink(activity: MainActivity) {
        intent?.let {
            currentUri = it.data
            it.data?.scheme?.let { scheme ->
                if (scheme.contains(BuildConfig.DEEP_LINKS_SCHEME)) {
                    handleEntourageDeepLink(activity)
                } else {
                    handleHttpDeepLink(activity)
                }
            } ?: run {
                intent = null
            }
        }
    }

    /**
     * Handles deeplinks with format "entourage:// *"
     * @param activity
     */
    private fun handleEntourageDeepLink(activity: MainActivity) {
        val host = currentUri?.host
        if (host == null) {
            intent = null
            return
        }
        handleDeepLink(activity, host.lowercase(Locale.ROOT), currentUri?.pathSegments)
    }

    /**
     * Handles the deeplinks with format "http(s)://"
     * @param activity
     */
    private fun handleHttpDeepLink(activity: MainActivity) {
        currentUri?.let {
            val pathSegments: ArrayList<String> = ArrayList(it.pathSegments)
            if (pathSegments.size >= 2) {
                val requestedView = pathSegments[0]
                val key = pathSegments[1]
                if (requestedView.equals(DeepLinksView.DEEPLINK.view, ignoreCase = true)) {
                    //path like /deeplink/key/...
                    //Remove the requested view and the key from path segments
                    pathSegments.removeAt(0)
                    pathSegments.removeAt(0) // zero, because it was shifted when we removed requestedview
                    //Handle the deep link
                    handleDeepLink(activity, key, pathSegments)
                    return  // we don't suppress the intent in this case
                }
            }
        }
        intent = null
    }

    private fun handleDeepLink(activity: MainActivity, key: String, pathSegments: List<String>?) {
        /*if (key == DeepLinksView.FEED.view) {
            activity.showFeed()
            if (pathSegments != null && pathSegments.isNotEmpty()) {
                if (DeepLinksView.FILTERS.view.equals(pathSegments[0], ignoreCase = true)) {
                    //TODO: ??? activity.showMapFilters()
                }
            }
        } else*/
        if (key == DeepLinksView.BADGE.view) {
            activity.showProfile()
        } else if (key == DeepLinksView.WEBVIEW.view) {
            try {
                currentUri?.getQueryParameter("url")?.let { urlToOpen ->
                    val url  = if (!urlToOpen.lowercase(Locale.ROOT).startsWith("http")) {
                        "https://$urlToOpen"
                    } else urlToOpen
                    activity.showFeed()
                    activity.showWebView(url)
                }
            } catch (ignored: Exception) {
            }
        } else if (key == DeepLinksView.PROFILE.view) {
            activity.showProfile()
        } else if (key == DeepLinksView.GUIDE.view) {
            activity.showGuide()
        } else if (key == DeepLinksView.EVENTS.view) {
            activity.showEvents()
        } else if (key == DeepLinksView.CREATE_ACTION.view) {
            activity.showActionsTab()
        } else if (key == DeepLinksView.ENTOURAGE.view || key == DeepLinksView.ENTOURAGES.view || key == DeepLinksView.APPLINK_ACTION.view) {
            if (pathSegments != null && pathSegments.isNotEmpty()) {
                //TODO EntBus.post(OnFeedItemInfoViewRequestedEvent(TimestampedObject.ENTOURAGE_CARD, "", pathSegments[0]))
            }
            //TODO check if it is working ??
        } else if (key == DeepLinksView.TUTORIAL.view) {
            activity.showTutorial(true)
        }
        else if (key == DeepLinksView.GUIDE_MAP.view) {
            activity.showGuideMap()
        }
        intent = null
    }

    /**
     * Enum that contains the keywords our [DeepLinksManager] can manage
     */
    enum class DeepLinksView(val view: String) {
        DEEPLINK("deeplink"),
        ENTOURAGE("entourage"),
        ENTOURAGES("entourages"),
        //FEED("feed"),
        BADGE("badge"),
        WEBVIEW("webview"),
        PROFILE("profile"),
        //FILTERS("filters"),
        EVENTS("events"),
        GUIDE("guide"),
        CREATE_ACTION("create-action"),
        TUTORIAL("tutorial"),
        GUIDE_MAP("guidemap"),
        APPLINK_ACTION("actions"); //Deeplink from https web link
    }

    /**
     * Linkify the textview, by adding the app deep link
     * @param textView textview to be linkified
     */
    fun linkify(textView: TextView) {
        Linkify.addLinks(textView, Linkify.ALL) // to add support for standard URLs, emails, phones a.s.o.
        /*Not working
        val pattern = Pattern.compile(BuildConfig.DEEP_LINKS_SCHEME + "://\\S+")
        Linkify.addLinks(textView, pattern, null)
        */
    }

    fun storeIntent(newIntent: Intent) {
        val extras: Bundle? = newIntent.extras
        if (Intent.ACTION_VIEW == newIntent.action) {
            // Save the deep link intent
            intent = newIntent
        } else if (extras != null && extras.containsKey(EntourageFirebaseMessagingService.KEY_CTA)) {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(extras.getString(
                EntourageFirebaseMessagingService.KEY_CTA)))
        }
    }

    fun findFirstDeeplinkInText(content: String): String? {
        val patternDeepLink  = (BuildConfig.DEEP_LINKS_SCHEME + "://\\S+").toRegex()
        patternDeepLink.find(content)?.let {
            return it.value
        }
        val patternHTTPDeepLink  = ("http.?://"+BuildConfig.DEEP_LINKS_URL + "/deeplink/\\S+").toRegex()
        patternHTTPDeepLink.find(content)?.let {
            return it.value
        }
        val patternHTTPWWWDeepLink  = ("http.?://www\\."+BuildConfig.DEEP_LINKS_URL + "/deeplink/\\S+").toRegex()
        patternHTTPWWWDeepLink.find(content)?.let {
            return it.value
        }
        val patternHTTPEntourage  = ("http.?://www\\."+BuildConfig.DEEP_LINKS_URL + "/entourage\\S+").toRegex()
        patternHTTPEntourage.find(content)?.let {
            return it.value
        }
        return null
    }
}