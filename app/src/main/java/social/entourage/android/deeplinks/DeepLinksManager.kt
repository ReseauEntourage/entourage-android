package social.entourage.android.deeplinks

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.widget.TextView
import social.entourage.android.BuildConfig
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.api.tape.Events.OnFeedItemInfoViewRequestedEvent
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.tools.BusProvider
import social.entourage.android.user.edit.UserEditFragment
import timber.log.Timber
import java.util.*
import java.util.regex.Pattern

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
        if (intent == null) return
        currentUri = intent!!.data
        if (currentUri == null || currentUri?.scheme == null) {
            intent = null
            return
        }
        Timber.i("New deeplink : %s", currentUri.toString())
        if (currentUri!!.scheme!!.contains(BuildConfig.DEEP_LINKS_SCHEME)) {
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
        val host = currentUri?.host
        if (host == null) {
            intent = null
            return
        }
        handleDeepLink(activity, host.toLowerCase(), currentUri?.pathSegments)
    }

    /**
     * Handles the deeplinks with format "http(s)://"
     * @param activity
     */
    private fun handleHttpDeepLink(activity: MainActivity) {
        val pathSegments: ArrayList<String> = ArrayList(currentUri!!.pathSegments)
        if (pathSegments.size >= 2) {
            val requestedView = pathSegments[0]
            val key = pathSegments[1]
            if (requestedView.equals(DeepLinksView.ENTOURAGES.view, ignoreCase = true)
                    ||requestedView.equals(DeepLinksView.ENTOURAGE.view, ignoreCase = true)) {
                //path like /entourage/UUID...
                BusProvider.getInstance().post(OnFeedItemInfoViewRequestedEvent(FeedItem.ENTOURAGE_CARD, "", key))
            } else if (requestedView.equals(DeepLinksView.DEEPLINK.view, ignoreCase = true)) {
                //path like /deeplink/key/...
                //Remove the requested view and the key from path segments
                pathSegments.removeAt(0)
                pathSegments.removeAt(0) // zero, because it was shifted when we removed requestedview
                //Handle the deep link
                handleDeepLink(activity, key, pathSegments)
                return  // we don't suppress the intent in this case
            }
        }
        intent = null
    }

    private fun handleDeepLink(activity: MainActivity, key: String, pathSegments: List<String>?) {
        if (key == DeepLinksView.FEED.view) {
            activity.showFeed()
            activity.dismissMapFragmentDialogs()
            if (pathSegments != null && pathSegments.isNotEmpty()) {
                if (DeepLinksView.FILTERS.view.equals(pathSegments[0], ignoreCase = true)) {
                    activity.showMapFilters()
                }
            }
        } else if (key == DeepLinksView.BADGE.view) {
            val userEditFragment = activity.supportFragmentManager.findFragmentByTag(UserEditFragment.TAG) as UserEditFragment?
            if (userEditFragment != null) {
                userEditFragment.onAddAssociationClicked()
            } else {
                activity.selectItem(R.id.action_edit_profile)
            }
        } else if (key == DeepLinksView.WEBVIEW.view) {
            try {
                var urlToOpen = currentUri?.getQueryParameter("url")
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
        } else if (key == DeepLinksView.ENTOURAGE.view || key == DeepLinksView.ENTOURAGES.view) {
            if (pathSegments != null && pathSegments.isNotEmpty()) {
                BusProvider.getInstance().post(OnFeedItemInfoViewRequestedEvent(FeedItem.ENTOURAGE_CARD, "", pathSegments[0]))
            }
        } else if (key == DeepLinksView.TUTORIAL.view) {
            activity.showTutorial(true)
        }
        intent = null
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

    /**
     * Linkify the textview, by adding the app deep link
     * @param textView textview to be linkified
     */
    fun linkify(textView: TextView) {
        val pattern = Pattern.compile(BuildConfig.DEEP_LINKS_SCHEME + "://" + DeepLinksView.ENTOURAGE.view + "/[0-9]+")
        Linkify.addLinks(textView, Linkify.ALL) // to add support for standard URLs, emails, phones a.s.o.
        Linkify.addLinks(textView, pattern, null)
    }

    fun storeIntent(newIntent: Intent) {
        val extras: Bundle? = newIntent.extras
        if (Intent.ACTION_VIEW == newIntent.action) {
            // Save the deep link intent
            intent = newIntent
        } else if (extras != null && extras.containsKey(PushNotificationManager.KEY_CTA)) {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(extras.getString(PushNotificationManager.KEY_CTA)))
        }
    }


}