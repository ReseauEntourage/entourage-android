package social.entourage.android.afterLogin

import android.content.Intent
import android.os.SystemClock
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.core.net.toUri
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.BuildConfig
import social.entourage.android.MainActivity
import social.entourage.android.badges.BadgeKey
import social.entourage.android.deeplinks.DeepLinksManager
import social.entourage.android.e2e.E2EScreenshot
import social.entourage.android.tools.view.WebViewFragment

/**
 * One @Test per deep link case (hardcoded, not a loop): each launches and tears down
 * its own MainActivity via [runDeeplinkTestCase], so tests don't share activity/navigation state
 * and a failure in one case can't cascade into the next.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class UniversalLinkTest : EntourageTestAfterLogin() {
    private val scheme = BuildConfig.DEEP_LINKS_SCHEME
    private val domain = BuildConfig.DEEP_LINKS_URL

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private val screenshot = E2EScreenshot("universal_links")

    private fun assertCreateActionCharterScreenDisplayed() {
        val deadline = SystemClock.elapsedRealtime() + 10_000
        while (true) {
            try {
                composeTestRule.onNodeWithTag("charter_scroll").assertIsDisplayed()
                return
            } catch (e: Throwable) {
                if (SystemClock.elapsedRealtime() > deadline) throw e
                SystemClock.sleep(50)
            }
        }
    }

    // ----------------------------------
    // Custom schemes
    // ----------------------------------
    @Test
    fun customSchemeGuide() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "Custom scheme - Guide",
        uriString = "$scheme://guide",
        expectedView = DeepLinksManager.DeepLinksView.GUIDE,
        targetViewIdName = "navigation_home"
    ))

    @Test
    fun customSchemeEvents() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "Custom scheme - Events",
        uriString = "$scheme://events",
        expectedView = DeepLinksManager.DeepLinksView.EVENTS,
        targetViewIdName = "navigation_events"
    ))

    @Test
    fun customSchemeProfile() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "Custom scheme - Profile",
        uriString = "$scheme://profile",
        expectedView = DeepLinksManager.DeepLinksView.PROFILE,
        targetViewIdName = "container_profile"
    ))

    @Test
    fun customSchemeBadge() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "Custom scheme - Badge",
        uriString = "$scheme://badge",
        expectedView = DeepLinksManager.DeepLinksView.BADGE,
        targetViewIdName = "container_profile"
    ))

    @Test
    fun customSchemeCreateAction() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "Custom scheme - Create Action",
        uriString = "$scheme://create-action",
        expectedView = DeepLinksManager.DeepLinksView.CREATE_ACTION,
        targetViewIdName = "navigation_donations"
    ))

    @Test
    fun customSchemeWebviewWithUrl() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "Custom scheme - Webview with URL",
        uriString = "$scheme://webview?url=https://entourage.social",
        expectedView = DeepLinksManager.DeepLinksView.WEBVIEW,
        targetViewIdName = "webview"
    ))

    // ----------------------------------
    // Official web links (/deeplink/ & /app/)
    // ----------------------------------
    @Test
    fun httpDeeplinkGuide() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTP web link - Deeplink Guide",
        uriString = "http://$domain/deeplink/guide",
        expectedView = DeepLinksManager.DeepLinksView.GUIDE,
        targetViewIdName = "navigation_home"
    ))

    @Test
    fun httpsDeeplinkProfile() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS web link - Deeplink Profile",
        uriString = "https://$domain/deeplink/profile",
        expectedView = DeepLinksManager.DeepLinksView.PROFILE,
        targetViewIdName = "container_profile"
    ))

    @Test
    fun httpsDeeplinkEvents() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS web link - Deeplink Events",
        uriString = "https://$domain/deeplink/events",
        expectedView = DeepLinksManager.DeepLinksView.EVENTS,
        targetViewIdName = "navigation_events"
    ))

    @Test
    fun httpsDeeplinkCreateAction() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS web link - Deeplink Create Action",
        uriString = "https://$domain/deeplink/create-action",
        expectedView = DeepLinksManager.DeepLinksView.CREATE_ACTION,
        targetViewIdName = "navigation_donations"
    ))

    @Test
    fun httpsAppLinkHomepage() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Homepage",
        uriString = "https://$domain/app/",
        expectedView = DeepLinksManager.DeepLinksView.GUIDE,
        targetViewIdName = "navigation_home"
    ))

    @Test
    fun httpsAppLinkGroups() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Groups",
        uriString = "https://$domain/app/groups",
        expectedView = DeepLinksManager.DeepLinksView.ENTOURAGES,
        targetViewIdName = "navigation_groups"
    ))

    @Test
    fun httpsAppLinkGroupDetail() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Group Detail",
        uriString = "https://$domain/app/neighborhoods/$NEIGHBOURHOOD_ID",
        expectedView = DeepLinksManager.DeepLinksView.ENTOURAGE,
        targetViewIdName = "group_name"
    ))

    @Test
    fun httpsAppLinkOutings() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Outings",
        uriString = "https://$domain/app/outings",
        expectedView = DeepLinksManager.DeepLinksView.EVENTS,
        targetViewIdName = "title_section_header_event"
    ))

    @Test
    fun httpsAppLinkOutingsNew() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Outings New",
        uriString = "https://$domain/app/outings/new",
        expectedView = DeepLinksManager.DeepLinksView.EVENTS,
        targetViewIdName = "create_event_layout"
    ))

    @Test
    fun httpsAppLinkContributions() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Contributions",
        uriString = "https://$domain/app/contributions",
        expectedView = DeepLinksManager.DeepLinksView.CREATE_ACTION,
        targetViewIdName = "navigation_donations"
    ))

    @Test
    fun httpsAppLinkSolicitations() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Solicitations",
        uriString = "https://$domain/app/solicitations",
        expectedView = DeepLinksManager.DeepLinksView.CREATE_ACTION,
        targetViewIdName = "navigation_donations"
    ))

    @Test
    fun httpsAppLinkMap() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Map",
        uriString = "https://$domain/app/map",
        expectedView = DeepLinksManager.DeepLinksView.GUIDE_MAP,
        targetViewIdName = "ui_container"
    ))

    @Test
    fun httpsAppLinkBadges() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Badges",
        uriString = "https://$domain/app/badges",
        expectedView = DeepLinksManager.DeepLinksView.BADGE,
        targetViewIdName = "rv_badges"
    ))

    @Test
    fun httpsAppLinkResources() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Resources",
        uriString = "https://$domain/app/resources",
        expectedView = DeepLinksManager.DeepLinksView.GUIDE,
        targetViewIdName = "content"
    ))

    @Test
    fun httpsAppLinkResourceDetail() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Resource Detail",
        uriString = "https://$domain/app/resources/$RESOURCE_ID",
        expectedView = DeepLinksManager.DeepLinksView.GUIDE,
        targetViewIdName = "content"
    ))

    @Test
    fun httpsAppLinkNationalGroup() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - national Group",
        uriString = "https://$domain/app/groups/national",
        expectedView = DeepLinksManager.DeepLinksView.ENTOURAGE,
        targetViewIdName = "groups_recyclerview"
    ))

    @Test
    fun httpsAppLinkOutingDetail() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Outing Detail",
        uriString = "https://$domain/app/outings/$OUTING_ID",
        expectedView = DeepLinksManager.DeepLinksView.EVENTS,
        targetViewIdName = "event_name"
    ))

    // This one is starting a calendar activity that we can't trace @Test
    fun httpsAppLinkOutingCalendar() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Outing add to Calendar",
        uriString = "https://$domain/app/outings/$OUTING_ID/agenda",
        expectedView = DeepLinksManager.DeepLinksView.EVENTS,
        targetViewIdName = "event_name"
    ))

    @Test
    fun httpsAppLinkWebinar() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Webinar",
        uriString = "https://$domain/app/outings/webinar",
        expectedView = DeepLinksManager.DeepLinksView.EVENTS,
        targetViewIdName = "tv_welcome_list_title"
    ))

    @Test
    fun httpsAppLinkWelcome() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Welcome",
        uriString = "https://$domain/app/outings/welcome",
        expectedView = DeepLinksManager.DeepLinksView.EVENTS,
        targetViewIdName = "tv_welcome_list_title"
    ))

    @Test
    fun httpsAppLinkFirstSteps() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - First Steps",
        uriString = "https://$domain/app/outings/first_steps",
        expectedView = DeepLinksManager.DeepLinksView.EVENTS,
        targetViewIdName = "tv_welcome_list_title"
    ))

    @Test
    fun httpsAppLinkSensibilisation() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - sensibilisation",
        uriString = "https://$domain/app/outings/sensibilisation",
        expectedView = DeepLinksManager.DeepLinksView.EVENTS,
        targetViewIdName = "tv_welcome_list_title"
    ))

    @Test
    fun httpsAppLinkPapotages() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - papotages",
        uriString = "https://$domain/app/outings/papotages",
        expectedView = DeepLinksManager.DeepLinksView.EVENTS,
        targetViewIdName = "tv_welcome_list_title"
    ))

    @Test
    fun httpsAppLinkNewContribution() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - New Contribution",
        uriString = "https://$domain/app/contributions/new",
        expectedView = DeepLinksManager.DeepLinksView.CREATE_ACTION
    )) {
        assertCreateActionCharterScreenDisplayed()
    }

    @Test
    fun httpsAppLinkNewSolicitation() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - New Solicitation",
        uriString = "https://$domain/app/solicitations/new",
        expectedView = DeepLinksManager.DeepLinksView.CREATE_ACTION
    )) {
        assertCreateActionCharterScreenDisplayed()
    }

    @Test
    fun httpsAppLinkEventChart() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Event Chart",
        uriString = "https://$domain/app/chart-event",
        expectedView = DeepLinksManager.DeepLinksView.GUIDE_MAP,
        targetViewIdName = "activity_group_rules"
    ))

    @Test
    fun httpsAppLinkConversations() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Conversations",
        uriString = "https://$domain/app/conversation-message",
        expectedView = DeepLinksManager.DeepLinksView.GUIDE_MAP,
        targetViewIdName = "navigation_messages"
    ))

    @Test
    fun httpsAppLinkUsers() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Users Profile",
        uriString = "https://$domain/app/users/$USER_ID",
        expectedView = DeepLinksManager.DeepLinksView.PROFILE
    ))

    @Test
    fun httpsAppLinkUser() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - User Profile",
        uriString = "https://$domain/app/user/$USER_ID",
        expectedView = DeepLinksManager.DeepLinksView.PROFILE
    ))

    @Test
    fun httpsAppLinkWelcomeVideo() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Welcome Video",
        uriString = "https://$domain/app/welcome-video",
        expectedView = DeepLinksManager.DeepLinksView.GUIDE,
        targetViewIdName = "webview_video",
        inBottomSheet = true
    ))

    @Test
    fun httpsAppLinkBadgePresentation() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Badge Presentation",
        uriString = "https://$domain/app/badges/intro",
        expectedView = DeepLinksManager.DeepLinksView.BADGE,
        targetViewIdName = "btn_discover_badges",
        inBottomSheet = true
    ))

    @Test
    fun httpsAppLinkBadgeBienvenue() = runDeeplinkTestCase(DeeplinkTestCase(
        description = "HTTPS App link - Badge ${BadgeKey.PREMIER_PAS}",
        uriString = "https://$domain/app/badges/${BadgeKey.PREMIER_PAS.apiKey}",
        expectedView = DeepLinksManager.DeepLinksView.BADGE,
        targetViewIdName = "tv_detail_title"
    ))

    // ----------------------------------
    // Shared runner
    // ----------------------------------
    private fun runDeeplinkTestCase(testCase: DeeplinkTestCase, extraAssert: (() -> Unit)? = null) {
        // Registers the OkHttp idling resource (so onIdle() waits for API calls) and logs in if needed
        setUp(ApplicationProvider.getApplicationContext())
        // No Custom Tabs provider => showWebView() falls back to the in-app WebViewFragment,
        // which Espresso can assert on (a Chrome Custom Tab runs outside our process)
        WebViewFragment.customTabsPackages = arrayListOf()

        var uriString = testCase.uriString
        if (uriString.contains(NEIGHBOURHOOD_ID)) {
            val id = getNeighborhoodId()
            uriString = uriString.replace(NEIGHBOURHOOD_ID, id)
        } else if (uriString.contains(RESOURCE_ID)) {
            val id = getMyResourceId()
            uriString = uriString.replace(RESOURCE_ID, id)
        } else if (uriString.contains(OUTING_ID)) {
            val id = getOutingId()
            uriString = uriString.replace(OUTING_ID, id)
        } else if (uriString.contains(USER_ID)) {
            val id = getUserId()
            uriString = uriString.replace(USER_ID, id)
        }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            Espresso.onIdle()

            val intent = Intent(Intent.ACTION_VIEW, uriString.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            scenario.onActivity { activity ->
                if(!uriString.contains("/app/")) {
                    DeepLinksManager.storeIntent(intent)
                    DeepLinksManager.handleCurrentDeepLink(activity)
                } else {
//                  activity.intent = intent
                    activity.handleUniversalLinkFromMain(intent)
//                  activity.useIntentForRedirection(intent)
                }
            }
            Espresso.onIdle()

            val resolvedView = DeepLinksManager.resolveDeepLinkView(uriString)
            Assert.assertEquals(
                "Mismatch for ${testCase.description}",
                testCase.expectedView,
                resolvedView
            )

            testCase.targetViewIdName?.let { viewIdName ->
                var resId = 0
                scenario.onActivity { activity ->
                    resId = activity.resources.getIdentifier(viewIdName, "id", activity.packageName)
                }
                if (resId != 0) {
                    waitForView(resId, root = if (testCase.inBottomSheet) RootMatchers.isDialog() else null)
                } else {
                    Assert.fail("View with ID $viewIdName not found")
                }
            }
            extraAssert?.invoke()
            screenshot.shoot(testCase.description)
        }
    }

    @After
    fun resetCustomTabsPackages() {
        WebViewFragment.customTabsPackages = null
    }

    // ----------------------------------
    // Test data
    // ----------------------------------
    data class DeeplinkTestCase(
        val description: String,
        val uriString: String,
        val expectedView: DeepLinksManager.DeepLinksView,
        val targetViewIdName: String? = null,
        // Target view lives in a BottomSheetDialogFragment window, not the activity's
        val inBottomSheet: Boolean = false
    )

    companion object {
        // Placeholders replaced at runtime with IDs fetched from the staging API
        const val NEIGHBOURHOOD_ID = "%neighbourhoodId%"
        const val RESOURCE_ID = "%resourceId%"
        const val OUTING_ID = "%outingId%"
        const val USER_ID = "%userId%"
    }
}