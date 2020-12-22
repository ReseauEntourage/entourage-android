package social.entourage.android

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeepLinkingTest {

    private val isAppStarted = false

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(MainActivity::class.java, isAppStarted, isAppStarted)
    private val entourageHash = if(BuildConfig.FLAVOR_env=="prod") "eWvL7X0WfPug" else "eP8v6B2UYM44"
    private val entourageID = if(BuildConfig.FLAVOR_env=="prod") "204" else "2300"
    private val dmHash= if(BuildConfig.FLAVOR_env=="prod") "1_list_me-94" else "1_list_me-2790"
    private val dmID=if(BuildConfig.FLAVOR_env=="prod") "51946" else "2013"
    
    private fun startIntent(intent: Intent) {
        if(!isAppStarted) {
            activityTestRule.launchActivity(intent)
        } else {
            activityTestRule.activity.startActivity(intent)
        }
    }


    @Before
    fun setUp() {
        /*if(EntourageApplication.get(activityTestRule.activity).entourageComponent.authenticationController.isAuthenticated == false) {
            Espresso.onView(ViewMatchers.withId(R.id.login_button_login)).perform(ViewActions.click())
            Espresso.onView(ViewMatchers.withId(R.id.login_edit_phone)).perform(ViewActions.typeText(BuildConfig.TEST_ACCOUNT_LOGIN), ViewActions.closeSoftKeyboard())
            Espresso.onView(ViewMatchers.withId(R.id.login_edit_code)).perform(ViewActions.typeText(BuildConfig.TEST_ACCOUNT_PWD), ViewActions.closeSoftKeyboard())
            Espresso.onView(ViewMatchers.withId(R.id.login_button_signin)).perform(ViewActions.click())
        }*/
    }

    @Test
    fun connectedCreateActionDeeplink() {
        connectedCreateActionDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://create-action")
    }

    @Test
    fun connectedCreateActionDeeplinkHTTP() {
        connectedCreateActionDeeplink("http://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/create-action")
    }

    @Test
    fun connectedCreateActionDeeplinkHTTPS() {
        connectedCreateActionDeeplink("https://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/create-action")
    }

    private fun connectedCreateActionDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withText(R.string.entourage_disclaimer_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun connectedBadgeDeeplink() {
        connectedBadgeDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://badge")
    }

    @Test
    fun connectedBadgeDeeplinkHTTP() {
        connectedBadgeDeeplink("http://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/badge")
    }

    @Test
    fun connectedBadgeDeeplinkHTTPS() {
        connectedBadgeDeeplink("https://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/badge")
    }

    private fun connectedBadgeDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withText(R.string.user_profile_display_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun connectedWebviewDeeplink() {
        connectedWebviewDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://webview?url=http://www.google.com")
    }

    @Test
    fun connectedWebviewDeeplinkHTTP() {
        connectedWebviewDeeplink("http://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/webview?url=http://www.google.com")
    }

    @Test
    fun connectedWebviewDeeplinkHTTPS() {
        connectedWebviewDeeplink("https://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/webview?url=http://www.google.com")
    }

    private fun connectedWebviewDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withId(R.id.webview_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun connectedPhoneSettingsDeeplink() {
        connectedPhoneSettingsDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://phone-settings")
    }

    @Test
    fun connectedPhoneSettingsDeeplinkHTTP() {
        connectedPhoneSettingsDeeplink("http://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/phone-settings")
    }

    @Test
    fun connectedPhoneSettingsDeeplinkHTTPS() {
        connectedPhoneSettingsDeeplink("https://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/phone-settings")
    }

    private fun connectedPhoneSettingsDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withId(R.id.fragment_map_top_tab)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun connectedFeedFilterDeeplink() {
        connectedFeedFilterDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://feed/filters")
    }

    @Test
    fun connectedFeedFilterDeeplinkHTTP() {
        connectedFeedFilterDeeplink("http://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/feed/filters")
    }

    @Test
    fun connectedFeedFilterDeeplinkHTTPS() {
        connectedFeedFilterDeeplink("https://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/feed/filters")
    }

    private fun connectedFeedFilterDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withText(R.string.map_filter_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun connectedEventsDeeplink() {
        connectedEventsDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://events")
    }

    @Test
    fun connectedEventsDeeplinkHTTP() {
        connectedEventsDeeplink("http://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/events")
    }

    @Test
    fun connectedEventsDeeplinkHTTPS() {
        connectedEventsDeeplink("https://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/events")
    }

    private fun connectedEventsDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withText(R.string.map_tab_events)).check(ViewAssertions.matches(ViewMatchers.isSelected()))
    }

    @Test
    fun connectedFeedDeeplink() {
        connectedFeedDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://feed")
    }

    @Test
    fun connectedFeedDeeplinkHTTP() {
        connectedFeedDeeplink("http://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/feed")
    }

    @Test
    fun connectedFeedDeeplinkHTTPS() {
        connectedFeedDeeplink("https://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/feed")
    }

    private fun connectedFeedDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withId(R.id.fragment_map_top_tab)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun connectedEntouragesIdDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourages",entourageID)
    }

    @Test
    fun connectedEntouragesHashDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourages",entourageHash)
    }

    @Test
    fun connectedEntourageIdDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourage",entourageID)
    }

    @Test
    fun connectedEntourageHashDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourage",entourageHash)
    }

    @Test
    fun connectedEntouragesIdHTTP() {
        connectedEntourageHTTP("entourages",entourageID)
    }

    @Test
    fun connectedEntouragesHashHTTP() {
        connectedEntourageHTTP("entourages",entourageHash)
    }

    @Test
    fun connectedEntourageIdHTTP() {
        connectedEntourageHTTP("entourage",entourageID)
    }

    @Test
    fun connectedEntourageHashHTTP() {
        connectedEntourageHTTP("entourage",entourageHash)
    }

    @Test
    fun connectedEntouragesIdHTTPS() {
        connectedEntourageHTTPS("entourages",entourageID)
    }

    @Test
    fun connectedEntouragesHashHTTPS() {
        connectedEntourageHTTPS("entourages",entourageHash)
    }

    @Test
    fun connectedEntourageIdHTTPS() {
        connectedEntourageHTTPS("entourage",entourageID)
    }

    @Test
    fun connectedEntourageHashHTTPS() {
        connectedEntourageHTTPS("entourage",entourageHash)
    }

    @Test
    fun connectedEntouragesIdDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourages",entourageID)
    }

    @Test
    fun connectedEntouragesHashDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourages",entourageHash)
    }

    @Test
    fun connectedEntourageIdDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourage",entourageID)
    }

    @Test
    fun connectedEntourageHashDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourage",entourageHash)
    }

    @Test
    fun connectedEntouragesIdDeeplink() {
        connectedEntourageDeeplink("entourages",entourageID)
    }

    @Test
    fun connectedEntouragesHashDeeplink() {
        connectedEntourageDeeplink("entourages",entourageHash)
    }

    @Test
    fun connectedEntourageIdDeeplink() {
        connectedEntourageDeeplink("entourage",entourageID)
    }

    @Test
    fun connectedEntourageHashDeeplink() {
        connectedEntourageDeeplink("entourage",entourageHash)
    }

    @Test
    fun connectedDMsIdDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourages",dmID)
    }

    @Test
    fun connectedDMsHashDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourages",dmHash)
    }

    @Test
    fun connectedDMIdDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourage",dmID)
    }

    @Test
    fun connectedDMHashDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourage",dmHash)
    }

    @Test
    fun connectedDMsIdHTTP() {
        connectedEntourageHTTP("entourages",dmID)
    }

    @Test
    fun connectedDMsHashHTTP() {
        connectedEntourageHTTP("entourages",dmHash)
    }

    @Test
    fun connectedDMIdHTTP() {
        connectedEntourageHTTP("entourage",dmID)
    }

    @Test
    fun connectedDMHashHTTP() {
        connectedEntourageHTTP("entourage",dmHash)
    }

    @Test
    fun connectedDMsIdHTTPS() {
        connectedEntourageHTTPS("entourages",dmID)
    }

    @Test
    fun connectedDMsHashHTTPS() {
        connectedEntourageHTTPS("entourages",dmHash)
    }

    @Test
    fun connectedDMIdHTTPS() {
        connectedEntourageHTTPS("entourage",dmID)
    }

    @Test
    fun connectedDMHashHTTPS() {
        connectedEntourageHTTPS("entourage",dmHash)
    }

    @Test
    fun connectedDMsIdDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourages",dmID)
    }

    @Test
    fun connectedDMsHashDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourages",dmHash)
    }

    @Test
    fun connectedDMIdDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourage",dmID)
    }

    @Test
    fun connectedDMHashDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourage",dmHash)
    }

    @Test
    fun connectedDMsIdDeeplink() {
        connectedEntourageDeeplink("entourages",dmID)
    }

    @Test
    fun connectedDMsHashDeeplink() {
        connectedEntourageDeeplink("entourages",dmHash)
    }

    @Test
    fun connectedDMIdDeeplink() {
        connectedEntourageDeeplink("entourage",dmID)
    }

    @Test
    fun connectedDMHashDeeplink() {
        connectedEntourageDeeplink("entourage",dmHash)
    }

    private fun connectedEntourageDeeplink(key:String, id: String) {
        connectedEntourageDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://"+key+"/"+id)
    }

    private fun connectedEntourageDeeplinkHTTP(key:String, id: String) {
        connectedEntourageDeeplink("http://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/"+key+"/"+id)
    }

    private fun connectedEntourageHTTP(key:String, id: String) {
        connectedEntourageDeeplink("http://"+ BuildConfig.DEEP_LINKS_URL + "/"+key+"/"+id)
    }

    private fun connectedEntourageDeeplinkHTTPS(key:String, id: String) {
        connectedEntourageDeeplink("https://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/"+key+"/"+id)
    }

    private fun connectedEntourageHTTPS(key:String, id: String) {
        connectedEntourageDeeplink("https://"+ BuildConfig.DEEP_LINKS_URL + "/"+key+"/"+id)
    }

    private fun connectedEntourageDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withId(R.id.entourage_info_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun connectedMessagesDeeplink() {
        connectedMessagesDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://messages")
    }

    @Test
    fun connectedMessagesDeeplinkHTTP() {
        connectedMessagesDeeplink("http://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/messages")
    }

    @Test
    fun connectedMessagesDeeplinkHTTPS() {
        connectedMessagesDeeplink("https://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/messages")
    }

    private fun connectedMessagesDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withId(R.id.myentourages_tab)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun connectedTutorialDeeplink() {
        connectedTutorialDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://tutorial")
    }

    @Test
    fun connectedTutorialDeeplinkHTTP() {
        connectedTutorialDeeplink("http://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/tutorial")
    }

    @Test
    fun connectedTutorialDeeplinkHTTPS() {
        connectedTutorialDeeplink("https://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/tutorial")
    }

    private fun connectedTutorialDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withId(R.id.carousel_indicator_layout)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun connectedGuideDeeplink() {
        connectedGuideDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://guide")
    }

    @Test
    fun connectedGuideDeeplinkHTTP() {
        connectedGuideDeeplink("http://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/guide")
    }

    @Test
    fun connectedGuideDeeplinkHTTPS() {
        connectedGuideDeeplink("https://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/guide")
    }

    private fun connectedGuideDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withId(R.id.button_poi_propose)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun connectedProfileDeeplink() {
        connectedProfileDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://profile")
    }

    @Test
    fun connectedProfileDeeplinkHTTP() {
        connectedProfileDeeplink("http://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/profile")
    }

    @Test
    fun connectedProfileDeeplinkHTTPS() {
        connectedProfileDeeplink("https://"+ BuildConfig.DEEP_LINKS_URL + "/deeplink/profile")
    }

    private fun connectedProfileDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withText(R.string.user_profile_display_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun connectedWrongProfileDeeplink() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.DEEP_LINKS_SCHEME + "://profile/toto"))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withText(R.string.user_profile_display_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun unkownDeeplinkDeeplink() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.DEEP_LINKS_SCHEME + "://deeplink/profile"))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withText(R.string.user_profile_display_title)).check(ViewAssertions.doesNotExist())
    }

}