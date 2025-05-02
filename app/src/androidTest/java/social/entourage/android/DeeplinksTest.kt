package social.entourage.android

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.deeplinks.DeepLinksManager
import social.entourage.android.onboarding.login.LoginActivity

@RunWith(AndroidJUnit4::class)
open class DeepLinkingTest {

    @Rule
    @JvmField
    val activityTestRule = ActivityScenarioRule(LoginActivity::class.java)

    private lateinit var device: UiDevice

    protected fun startIntent(intent: Intent) {
        activityTestRule.scenario.onActivity {
            it.startActivity(intent)

        }

        try {
            //Select Entourage app to open deeplink
            val entourageButton: UiObject = device.findObject(
                    UiSelector().text("EntourDBG")
            )
            entourageButton.click()

            val alwaysButton: UiObject = device.findObject(
                    UiSelector().text("ALWAYS")
            )
            alwaysButton.click()
        } catch (e: UiObjectNotFoundException) {
            //We arent asked to choose app
        }
    }

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        //User must be logged for the tests
        login("0651234145", "108674")

        //Wait for server response
        Thread.sleep(4000)
    }

    private fun login(phoneNumber: String, codePwd: String) {
        val authenticationController = EntourageApplication.get().authenticationController
        OnboardingAPI.getInstance().login(phoneNumber, codePwd) { isOK, loginResponse, _ ->
            if (isOK) {
                loginResponse?.let {
                    authenticationController.saveUser(loginResponse.user)
                }
                authenticationController.saveUserPhoneAndCode(phoneNumber, codePwd)

                //set the tutorial as done
                val sharedPreferences = EntourageApplication.get().sharedPreferences
                (sharedPreferences.getStringSet(EntourageApplication.KEY_TUTORIAL_DONE, HashSet()) as HashSet<String>?)?.let { loggedNumbers ->
                    loggedNumbers.add(phoneNumber)
                    sharedPreferences.edit().putStringSet(EntourageApplication.KEY_TUTORIAL_DONE, loggedNumbers).apply()
                }
            }
        }
    }
}

class DeepLinkingTestCreateAction : DeepLinkingTest() {

    private val link = DeepLinksManager.DeepLinksView.CREATE_ACTION.view
    //TODO
    @Test
    fun connectedCreateActionDeeplink() {
        connectedCreateActionDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://" + link)
    }
    //TODO
    @Test
    fun connectedCreateActionDeeplinkHTTP() {
        connectedCreateActionDeeplink("http://${BuildConfig.DEEP_LINKS_URL}/deeplink/${link}")
    }
    //TODO
    @Test
    fun connectedCreateActionDeeplinkHTTPS() {
        connectedCreateActionDeeplink("https://${BuildConfig.DEEP_LINKS_URL}/deeplink/${link}")
    }

    private fun connectedCreateActionDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        //TODO onView(withId(R.id.fragment_plus_overlay)).check(matches(isDisplayed()))
    }
}

class DeepLinkingTestBadge : DeepLinkingTest() {

    @Test
    fun connectedBadgeDeeplink() {
        connectedBadgeDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://badge")
    }

    @Test
    fun connectedBadgeDeeplinkHTTP() {
        connectedBadgeDeeplink("http://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/badge")
    }

    @Test
    fun connectedBadgeDeeplinkHTTPS() {
        connectedBadgeDeeplink("https://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/badge")
    }

    private fun connectedBadgeDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        onView(withText(R.string.user_profile_display_title)).check(matches(isDisplayed()))
    }
}

class DeepLinkingTestWebview : DeepLinkingTest() {

    @Test
    fun connectedWebviewDeeplink() {
        connectedWebviewDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://webview?url=http://www.google.com")
    }

    @Test
    fun connectedWebviewDeeplinkHTTP() {
        connectedWebviewDeeplink("http://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/webview?url=http://www.google.com")
    }

    @Test
    fun connectedWebviewDeeplinkHTTPS() {
        connectedWebviewDeeplink("https://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/webview?url=http://www.google.com")
    }

    private fun connectedWebviewDeeplink(uri: String) {
        Intents.init()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        val expected = allOf(IntentMatchers.hasAction(Intent.ACTION_VIEW), IntentMatchers.hasData(uri))
        Intents.intended(expected)
        Intents.release()
    }

}

/*class DeepLinkingTestPoneSettings : DeepLinkingTest() {
    @Test
    fun connectedPhoneSettingsDeeplink() {
        connectedPhoneSettingsDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://phone-settings")
    }

    @Test
    fun connectedPhoneSettingsDeeplinkHTTP() {
        connectedPhoneSettingsDeeplink("http://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/phone-settings")
    }

    @Test
    fun connectedPhoneSettingsDeeplinkHTTPS() {
        connectedPhoneSettingsDeeplink("https://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/phone-settings")
    }

    private fun connectedPhoneSettingsDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Espresso.onView(ViewMatchers.withId(R.id.fragment_map_top_tab)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}*/

class DeepLinkingTestFilters : DeepLinkingTest() {

    @Test
    fun connectedFeedFilterDeeplink() {
        connectedFeedFilterDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://feed/filters")
    }

    @Test
    fun connectedFeedFilterDeeplinkHTTP() {
        connectedFeedFilterDeeplink("http://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/feed/filters")
    }

    @Test
    fun connectedFeedFilterDeeplinkHTTPS() {
        connectedFeedFilterDeeplink("https://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/feed/filters")
    }

    private fun connectedFeedFilterDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        onView(withText(R.string.map_filter_title)).check(matches(isDisplayed()))
    }
}

class DeepLinkingTestEvents : DeepLinkingTest() {

    @Test
    fun connectedEventsDeeplink() {
        connectedEventsDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://events")
    }

    @Test
    fun connectedEventsDeeplinkHTTP() {
        connectedEventsDeeplink("http://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/events")
    }

    @Test
    fun connectedEventsDeeplinkHTTPS() {
        connectedEventsDeeplink("https://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/events")
    }

    private fun connectedEventsDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        onView(allOf(withText(R.string.home_title_events), withId(R.id.ui_tv_title))).check(matches(isDisplayed()))
    }
}

class DeepLinkingTestFeed : DeepLinkingTest() {
    @Test
    fun connectedFeedDeeplink() {
        connectedFeedDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://feed")
    }

    @Test
    fun connectedFeedDeeplinkHTTP() {
        connectedFeedDeeplink("http://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/feed")
    }

    @Test
    fun connectedFeedDeeplinkHTTPS() {
        connectedFeedDeeplink("https://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/feed")
    }

    private fun connectedFeedDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Thread.sleep(1000) // We must wait for view to appear (when we run all tests at once)
        onView(withText(R.string.home_title_headlines)).check(matches(isDisplayed()))
    }
}

class DeepLinkingTestEntourage : DeepLinkingTest() {

    private val entourageID = if (BuildConfig.FLAVOR_env == "prod") "204" else "2300"
    private val entourageHash = if (BuildConfig.FLAVOR_env == "prod") "eWvL7X0WfPug" else "eP8v6B2UYM44"
    private val dmHash = if (BuildConfig.FLAVOR_env == "prod") "1_list_me-94" else "1_list_me-2790"
    private val dmID = if (BuildConfig.FLAVOR_env == "prod") "51946" else "2013"

    //TODO
    /*@Test
    fun connectedEntouragesIdDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourages", entourageID)
    }*/
    //TODO
    /*@Test
    fun connectedEntouragesHashDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourages", entourageHash)
    }*/
    //TODO
    /*@Test
    fun connectedEntourageIdDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourage", entourageID)
    }*/
    //TODO
    /*@Test
    fun connectedEntourageHashDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourage", entourageHash)
    }*/
    //TODO
    /*@Test
    fun connectedEntouragesIdHTTP() {
        connectedEntourageHTTP("entourages", entourageID)
    }*/
    //TODO
    /*@Test
    fun connectedEntouragesHashHTTP() {
        connectedEntourageHTTP("entourages", entourageHash)
    }*/
    //TODO
    /*@Test
    fun connectedEntourageIdHTTP() {
        connectedEntourageHTTP("entourage", entourageID)
    }*/
    //TODO
    /*@Test
    fun connectedEntourageHashHTTP() {
        connectedEntourageHTTP("entourage", entourageHash)
    }*/
    //TODO
    /*@Test
    fun connectedEntouragesIdHTTPS() {
        connectedEntourageHTTPS("entourages", entourageID)
    }*/
    //TODO
    /*@Test
    fun connectedEntouragesHashHTTPS() {
        connectedEntourageHTTPS("entourages", entourageHash)
    }*/
    //TODO
    /*@Test
    fun connectedEntourageIdHTTPS() {
        connectedEntourageHTTPS("entourage", entourageID)
    }*/
    //TODO
    /*@Test
    fun connectedEntourageHashHTTPS() {
        connectedEntourageHTTPS("entourage", entourageHash)
    }*/
    //TODO
    /*@Test
    fun connectedEntouragesIdDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourages", entourageID)
    }*/
    //TODO
    /*@Test
    fun connectedEntouragesHashDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourages", entourageHash)
    }*/
    //TODO
    /*@Test
    fun connectedEntourageIdDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourage", entourageID)
    }*/
    //TODO
    /*@Test
    fun connectedEntourageHashDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourage", entourageHash)
    }*/
    //TODO
    /*@Test
    fun connectedEntouragesIdDeeplink() {
        connectedEntourageDeeplink("entourages", entourageID)
    }*/
    //TODO
    /*@Test
    fun connectedEntouragesHashDeeplink() {
        connectedEntourageDeeplink("entourages", entourageHash)
    }*/
    //TODO
    /*@Test
    fun connectedEntourageIdDeeplink() {
        connectedEntourageDeeplink("entourage", entourageID)
    }*/
    //TODO
    /*@Test
    fun connectedEntourageHashDeeplink() {
        connectedEntourageDeeplink("entourage", entourageHash)
    }*/
    //TODO
    /*@Test
    fun connectedDMsIdDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourages", dmID)
    }*/
    //TODO
    /*@Test
    fun connectedDMsHashDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourages", dmHash)
    }*/
    //TODO
    /*@Test
    fun connectedDMIdDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourage", dmID)
    }*/
    //TODO
    /*@Test
    fun connectedDMHashDeeplinkHTTP() {
        connectedEntourageDeeplinkHTTP("entourage", dmHash)
    }*/
    //TODO
    /*@Test
    fun connectedDMsIdHTTP() {
        connectedEntourageHTTP("entourages", dmID)
    }*/
    //TODO
    /*@Test
    fun connectedDMsHashHTTP() {
        connectedEntourageHTTP("entourages", dmHash)
    }*/
    //TODO
    /*@Test
    fun connectedDMIdHTTP() {
        connectedEntourageHTTP("entourage", dmID)
    }*/
    //TODO
    /*@Test
    fun connectedDMHashHTTP() {
        connectedEntourageHTTP("entourage", dmHash)
    }*/
    //TODO
    /*@Test
    fun connectedDMsIdHTTPS() {
        connectedEntourageHTTPS("entourages", dmID)
    }*/
    //TODO
    /*@Test
    fun connectedDMsHashHTTPS() {
        connectedEntourageHTTPS("entourages", dmHash)
    }*/
    //TODO
    /*@Test
    fun connectedDMIdHTTPS() {
        connectedEntourageHTTPS("entourage", dmID)
    }*/
    //TODO
    /*@Test
    fun connectedDMHashHTTPS() {
        connectedEntourageHTTPS("entourage", dmHash)
    }*/
    //TODO
    /*@Test
    fun connectedDMsIdDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourages", dmID)
    }*/
    //TODO
    /*@Test
    fun connectedDMsHashDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourages", dmHash)
    }*/
    //TODO
    /*@Test
    fun connectedDMIdDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourage", dmID)
    }*/
    //TODO
    /*@Test
    fun connectedDMHashDeeplinkHTTPS() {
        connectedEntourageDeeplinkHTTPS("entourage", dmHash)
    }*/
    //TODO
    /*@Test
    fun connectedDMsIdDeeplink() {
        connectedEntourageDeeplink("entourages", dmID)
    }*/
    //TODO
    /*@Test
    fun connectedDMsHashDeeplink() {
        connectedEntourageDeeplink("entourages", dmHash)
    }*/
    //TODO
    /*@Test
    fun connectedDMIdDeeplink() {
        connectedEntourageDeeplink("entourage", dmID)
    }*/
    //TODO
    /*@Test
    fun connectedDMHashDeeplink() {
        connectedEntourageDeeplink("entourage", dmHash)
    }*/

    /*private fun connectedEntourageDeeplink(key: String, id: String) {
        connectedEntourageDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://" + key + "/" + id)
    }

    private fun connectedEntourageDeeplinkHTTP(key: String, id: String) {
        connectedEntourageDeeplink("http://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/" + key + "/" + id)
    }

    private fun connectedEntourageHTTP(key: String, id: String) {
        connectedEntourageDeeplink("http://" + BuildConfig.DEEP_LINKS_URL + "/" + key + "/" + id)
    }

    private fun connectedEntourageDeeplinkHTTPS(key: String, id: String) {
        connectedEntourageDeeplink("https://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/" + key + "/" + id)
    }

    private fun connectedEntourageHTTPS(key: String, id: String) {
        connectedEntourageDeeplink("https://" + BuildConfig.DEEP_LINKS_URL + "/" + key + "/" + id)
    }

    private fun connectedEntourageDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        Thread.sleep(1000) // We must wait for view to appear (when we run all tests at once)
        onView(withId(R.id.entourage_info_title_layout)).check(matches(isDisplayed()))
    }*/
}

class DeepLinkingTestMessages : DeepLinkingTest() {
    //TODO
    /*@Test
    fun connectedMessagesDeeplink() {
        connectedMessagesDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://messages")
    }*/
    //TODO
    /*@Test
    fun connectedMessagesDeeplinkHTTP() {
        connectedMessagesDeeplink("http://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/messages")
    }*/
    //TODO
    /*@Test
    fun connectedMessagesDeeplinkHTTPS() {
        connectedMessagesDeeplink("https://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/messages")
    }*/

    /*private fun connectedMessagesDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        onView(withId(R.id.myentourages_tab)).check(matches(isDisplayed()))
    }*/
}

class DeepLinkingTestTutorial : DeepLinkingTest() {
    private val link = DeepLinksManager.DeepLinksView.TUTORIAL.view
    //TODO
    /*@Test
    fun connectedTutorialDeeplink() {
        connectedTutorialDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://" + link)
    }*/
    //TODO
    /*@Test
    fun connectedTutorialDeeplinkHTTP() {
        connectedTutorialDeeplink("http://${BuildConfig.DEEP_LINKS_URL}/deeplink/${link}")
    }*/
    //TODO
    /*@Test
    fun connectedTutorialDeeplinkHTTPS() {
        connectedTutorialDeeplink("https://${BuildConfig.DEEP_LINKS_URL}/deeplink/${link}")
    }*/

    /*private fun connectedTutorialDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        onView(withId(R.id.carousel_indicator_layout)).check(matches(isDisplayed()))
    }*/

}

class DeepLinkingTestGuide : DeepLinkingTest() {
    @Test
    fun connectedGuideDeeplink() {
        connectedGuideDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://guide")
    }

    @Test
    fun connectedGuideDeeplinkHTTP() {
        connectedGuideDeeplink("http://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/guide")
    }

    @Test
    fun connectedGuideDeeplinkHTTPS() {
        connectedGuideDeeplink("https://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/guide")
    }

    private fun connectedGuideDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        onView(withId(R.id.ui_title_top)).check(matches(isDisplayed()))
    }

}

class DeepLinkingTestProfile : DeepLinkingTest() {
    @Test
    fun connectedProfileDeeplink() {
        connectedProfileDeeplink(BuildConfig.DEEP_LINKS_SCHEME + "://profile")
    }

    @Test
    fun connectedProfileDeeplinkHTTP() {
        connectedProfileDeeplink("http://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/profile")
    }

    @Test
    fun connectedProfileDeeplinkHTTPS() {
        connectedProfileDeeplink("https://" + BuildConfig.DEEP_LINKS_URL + "/deeplink/profile")
    }

    private fun connectedProfileDeeplink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startIntent(intent)
        onView(withText(R.string.user_profile_display_title)).check(matches(isDisplayed()))
    }
}

class DeepLinkingTestGeneric : DeepLinkingTest() {

    @Test
    fun connectedWrongProfileDeeplink() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.DEEP_LINKS_SCHEME + "://profile/toto"))
        startIntent(intent)
        onView(withText(R.string.user_profile_display_title)).check(matches(isDisplayed()))
    }

    @Test
    fun unknownDeeplinkDeeplink() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.DEEP_LINKS_SCHEME + "://deeplink/profile"))
        startIntent(intent)
        onView(withText(R.string.user_profile_display_title)).check(ViewAssertions.doesNotExist())
    }

}
