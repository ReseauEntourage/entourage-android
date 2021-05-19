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

    private val mainRecyclerView = onView(allOf(withId(R.id.ui_recyclerview), isDisplayed()))
    private val backButton = onView(allOf(withId(R.id.ui_bt_back), isDisplayed()))
    private val closeButton = onView(allOf(withId(R.id.entourage_info_close), isDisplayed()))
    private val titleCloseButton = onView(allOf(withId(R.id.title_close_button), isDisplayed()))

    private var jsonResponse: String = ""

    @Before
    fun setUp() {
        //Logout
        activityRule.scenario.onActivity { activity ->
            EntourageApplication[activity].components.authenticationController.logOutUser()
            jsonResponse = getJsonDataFromAsset(activity, "home_response_success.json") ?: ""
        }

        //Login
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText("651234145"), closeSoftKeyboard())
        onView(withId(R.id.ui_login_et_code)).perform(typeText("661192"), closeSoftKeyboard())
        onView(withId(R.id.ui_login_button_signup)).perform(click())

        Thread.sleep(6000)
    }

    //If you get PerformException, you may need to disable animations on your test device/emulator
    //https://stackoverflow.com/questions/44005338/android-espresso-performexception
    @Test
    fun homeExpertTest() {
        //Change Home feed data with json file
        runOnUiThread {
            EntBus.post(HomeCard.OnGetHomeFeed(jsonResponse))
        }

        val headlineRv = onView(allOf(withId(R.id.ui_recyclerview_headline), isDisplayed()))

        //Click on an Announcement item (without deeplink)
//        headlineRv.perform(actionOnItemAtPosition<ViewHolder>(0, click()))
//
//        clickBackButton()

        //Click on an Action item type outing
        headlineRv.perform(actionOnItemAtPosition<ViewHolder>(1, click()))

        clickCloseButton()

        //Click on an Action item type demand
        headlineRv.perform(actionOnItemAtPosition<ViewHolder>(2, click()))

        clickCloseButton()

        //Test events section
        mainRecyclerView.perform(scrollToPosition<ViewHolder>(2))
        val eventDetailButton = onView(allOf(withId(R.id.ui_event_show_more), isDisplayed()))
        eventDetailButton.perform(click())

        clickBackButton()

//        val eventsRv = onView(allOf(withId(R.id.ui_recyclerview_event), isDisplayed()))
//        eventsRv.perform(actionOnItemAtPosition<ViewHolder>(0, click()))
//
//        clickTitleCloseButton()

        //Test actions section
        val actionDetailButton = onView(allOf(withId(R.id.ui_action_show_more), isDisplayed()))
        actionDetailButton.perform(click())

        clickBackButton()

        val actionsRv = onView(allOf(withId(R.id.ui_recyclerview_action), isDisplayed()))
        actionsRv.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

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
        Thread.sleep(1000)
    }

    private fun clickCloseButton() {
        closeButton.perform(click())
        Thread.sleep(1000)
    }

    private fun clickTitleCloseButton() {
        titleCloseButton.perform(click())
        Thread.sleep(1000)
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
