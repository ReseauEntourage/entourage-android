package social.entourage.android


import android.content.Context
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.newsfeed.v2.HomeCard
import social.entourage.android.onboarding.login.LoginActivity
import social.entourage.android.tools.EntBus
import java.io.IOException


@LargeTest
@RunWith(AndroidJUnit4::class)
class HomeExpertTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)

    private lateinit var context: Context

    private val mainRecyclerView = onView(allOf(withId(R.id.ui_recyclerview), isDisplayed()))
    private val backButton = onView(allOf(withId(R.id.ui_bt_back), isDisplayed()))
    private val closeButton = onView(allOf(withId(R.id.entourage_info_close), isDisplayed()))

    private var jsonResponse: String = ""

    @Before
    fun setUp() {
        //Logout
        activityRule.scenario.onActivity { activity ->
            context = activity
            EntourageApplication[activity].components.authenticationController.logOutUser()
            jsonResponse = getJsonDataFromAsset(activity, "home_response_success.json") ?: ""
        }

        //Login
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText("651234145"), closeSoftKeyboard())
        onView(withId(R.id.ui_login_et_code)).perform(typeText("661192"), closeSoftKeyboard())
        onView(withId(R.id.ui_login_button_signup)).perform(click())

        Thread.sleep(4000)
    }

    //If you get PerformException, you may need to disable animations on your test device/emulator
    //https://stackoverflow.com/questions/44005338/android-espresso-performexception
    @Test
    fun homeExpertTest() {
        //Change Home feed data with json file
        loadTestData()

        val headlineRv = onView(allOf(withId(R.id.ui_recyclerview_headline), isDisplayed()))

        //Click on an Action item type demand
        headlineRv.perform(actionOnItemAtPosition<ViewHolder>(0, click()))
        val joinButton = onView(allOf(withId(R.id.entourage_info_request_join_button), isDisplayed()))
        joinButton.check(matches(withText(context.getString(R.string.tour_info_request_join_button_entourage).toUpperCase())))

        clickCloseButton()

        //Click on an Announcement item
        headlineRv.perform(actionOnItemAtPosition<ViewHolder>(1, click()))
        val gdsTitleTv = onView(allOf(withId(R.id.textView28), isDisplayed()))
        gdsTitleTv.check(matches(withText(R.string.gds_title)))

        clickBackButton()

        //Click on an Action item type outing
        headlineRv.perform(actionOnItemAtPosition<ViewHolder>(2, click()))
        joinButton.check(matches(withText(context.getString(R.string.tour_info_request_join_button_event).toUpperCase())))

        clickCloseButton()

        //Click on an Announcement item
        headlineRv.perform(actionOnItemAtPosition<ViewHolder>(3, click()))
        val messageEditText = onView(withId(R.id.entourage_info_comment))
        messageEditText.check(matches(isDisplayed()))

        clickCloseButton()

        //Test events section
        val eventDetailButton = onView(allOf(withId(R.id.ui_event_show_more), isDisplayed()))
        eventDetailButton.perform(click())
        val eventsAndActionsTitleTv = onView(allOf(withId(R.id.ui_tv_title), isDisplayed()))
        eventsAndActionsTitleTv.check(matches(withText(R.string.home_title_events)))

        clickBackButton()

        val eventsRv = onView(allOf(withId(R.id.ui_recyclerview_event), isDisplayed()))
        eventsRv.perform(actionOnItemAtPosition<ViewHolder>(0, click()))
        joinButton.check(matches(withText(context.getString(R.string.tour_info_request_join_button_event).toUpperCase())))

        clickCloseButton()

        //Test actions section
        mainRecyclerView.perform(scrollToPosition<ViewHolder>(2))
        val actionDetailButton = onView(allOf(withId(R.id.ui_action_show_more), isDisplayed()))
        actionDetailButton.perform(click())
        eventsAndActionsTitleTv.check(matches(withText(R.string.home_title_actions)))

        clickBackButton()

        val actionsRv = onView(allOf(withId(R.id.ui_recyclerview_action), isDisplayed()))
        actionsRv.perform(actionOnItemAtPosition<ViewHolder>(0, click()))
        joinButton.check(matches(withText(context.getString(R.string.tour_info_request_join_button_entourage).toUpperCase())))

        clickCloseButton()
    }

    @Test
    fun testFeedButton() {
        val bottomBarFeedButton = onView(
                allOf(withId(R.id.bottom_bar_newsfeed),
                        withContentDescription(R.string.action_map),
                        isDisplayed()))
        bottomBarFeedButton.perform(click())

        val feedFragmentLayout = onView(withId(R.id.ui_container))
        feedFragmentLayout.check(matches(isDisplayed()))
    }

    @Test
    fun testGuideButton() {
        val bottomBarGuideButton = onView(
                allOf(withId(R.id.bottom_bar_guide),
                        withContentDescription(R.string.action_guide),
                        isDisplayed()))
        bottomBarGuideButton.perform(click())

        val guideTitleTv = onView(allOf(withId(R.id.ui_title_top), isDisplayed()))
        guideTitleTv.check(matches(withText(R.string.hub_title)))
    }

    @Test
    fun testPlusButton() {
        val bottomBarPlusButton = onView(
                allOf(withId(R.id.bottom_bar_plus),
                        withContentDescription(R.string.action_plus),
                        isDisplayed()))
        bottomBarPlusButton.perform(click())

        val plusFragmentLayout = onView(withId(R.id.fragment_plus_overlay))
        plusFragmentLayout.check(matches(isDisplayed()))
    }

    @Test
    fun testMessagesButton() {
        val bottomBarMessagesButton = onView(
                allOf(withId(R.id.bottom_bar_mymessages),
                        withContentDescription(R.string.action_my_messages),
                        isDisplayed()))
        bottomBarMessagesButton.perform(click())

        val messagesTabLayout = onView(withId(R.id.myentourages_tab))
        messagesTabLayout.check(matches(isDisplayed()))
    }

    @Test
    fun testProfileButton() {
        val bottomBarProfileButton = onView(
                allOf(withId(R.id.bottom_bar_profile),
                        withContentDescription(R.string.action_profile),
                        isDisplayed()))
        bottomBarProfileButton.perform(click())

        val editProfileTv = onView(allOf(withId(R.id.action_edit_profile), isDisplayed()))
        editProfileTv.check(matches(withText(R.string.action_edit_profile_regular)))
    }

    private fun clickBackButton() {
        backButton.perform(click())
        //Pressing back button reload feed data so we need to replace it with test data
        loadTestData()
        Thread.sleep(1000)
    }

    private fun clickCloseButton() {
        closeButton.perform(click())
        Thread.sleep(1000)
    }

    private fun loadTestData() {
        runOnUiThread {
            EntBus.post(HomeCard.OnGetHomeFeed(jsonResponse))
        }
    }

    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }
}
