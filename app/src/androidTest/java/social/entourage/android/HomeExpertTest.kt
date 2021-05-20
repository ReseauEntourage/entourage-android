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
    private val eventsAndActionsTitleTv = onView(allOf(withId(R.id.ui_tv_title), isDisplayed()))
    private val profilePictureIv = onView(allOf(withId(R.id.drawer_header_user_photo), isDisplayed()))
    private val editProfileButton = onView(allOf(withId(R.id.action_edit_profile), isDisplayed()))
    private val saveProfileButton = onView(withText(R.string.user_save_button))
    private val saveButton = onView(allOf(withId(R.id.user_edit_profile_save), withText(R.string.user_button_confirm_changes)))

    private val bottomBarFeedButton = onView(
            allOf(withId(R.id.bottom_bar_newsfeed),
                    withContentDescription(R.string.action_map),
                    isDisplayed()))
    private val bottomBarGuideButton = onView(
            allOf(withId(R.id.bottom_bar_guide),
                    withContentDescription(R.string.action_guide),
                    isDisplayed()))
    private val bottomBarPlusButton = onView(
            allOf(withId(R.id.bottom_bar_plus),
                    withContentDescription(R.string.action_plus),
                    isDisplayed()))
    private val bottomBarMessagesButton = onView(
            allOf(withId(R.id.bottom_bar_mymessages),
                    withContentDescription(R.string.action_my_messages),
                    isDisplayed()))
    private val bottomBarProfileButton = onView(
            allOf(withId(R.id.bottom_bar_profile),
                    withContentDescription(R.string.action_profile),
                    isDisplayed()))

    private val aboutButton = onView(allOf(withId(R.id.ui_layout_help), isDisplayed()))
    private val backButton = onView(allOf(withId(R.id.ui_bt_back), isDisplayed()))
    private val closeButton = onView(allOf(withId(R.id.entourage_info_close), isDisplayed()))
    private val titleCloseButton = onView(allOf(withId(R.id.title_close_button), isDisplayed()))

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
    fun homeExpertFeedTest() {
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
        headlineRv.perform(scrollToPosition<ViewHolder>(3))
        headlineRv.perform(actionOnItemAtPosition<ViewHolder>(3, click()))
        val messageEditText = onView(withId(R.id.entourage_info_comment))
        messageEditText.check(matches(isDisplayed()))

        clickCloseButton()

        //Test events section
        val eventDetailButton = onView(allOf(withId(R.id.ui_event_show_more), isDisplayed()))
        eventDetailButton.perform(click())
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
    fun homeExpertGuideTest() {
        clickGuideButton()

        val aroundMeLayout = onView(allOf(withId(R.id.ui_layout_cell_1), isDisplayed()))
        aroundMeLayout.perform(click())
        val gdsTitleTv = onView(allOf(withId(R.id.textView28), isDisplayed()))
        gdsTitleTv.check(matches(withText(R.string.gds_title)))
    }

    @Test
    fun homeExpertPlusTest() {
        clickPlusButton()

        //TODO
    }

    @Test
    fun homeExpertMessagesTest() {
        clickMessagesButton()

        //TODO
    }

    @Test
    fun homeExpertProfileTest() {
        clickProfileButton()

        //Test go to UserFragment
        profilePictureIv.perform(click())
        val userTitleTv = onView(withText(R.string.user_profile_display_title))
        userTitleTv.check(matches(isDisplayed()))

        clickTitleCloseButton()

        val profileNameTv = onView(allOf(withId(R.id.drawer_header_user_name), isDisplayed()))
        profileNameTv.perform(click())
        userTitleTv.check(matches(isDisplayed()))

        clickTitleCloseButton()

        //Test go to UserEditFragment
        editProfileButton.perform(click())
        saveProfileButton.check(matches(isDisplayed()))

        clickTitleCloseButton()

        //Test go to events page
        val showEventsButton = onView(allOf(withId(R.id.ui_layout_show_events), isDisplayed()))
        showEventsButton.perform(click())
        val eventsTitleTv = onView(allOf(withId(R.id.ui_tv_title), withText(R.string.home_title_events)))
        eventsTitleTv.check(matches(isDisplayed()))

        clickProfileButton()

        //Test go to actions page
        val showActionsButton = onView(allOf(withId(R.id.ui_layout_show_actions), isDisplayed()))
        showActionsButton.perform(click())
        val actionsTitleTv = onView(allOf(withId(R.id.ui_tv_title), withText(R.string.home_title_actions)))
        actionsTitleTv.check(matches(isDisplayed()))

        clickProfileButton()

        //Scroll to bottom
        onView(withId(R.id.ui_iv_fb)).perform(scrollTo())

        //Test go to AboutFragment
        aboutButton.perform(click())
        val aboutTitleTv = onView(withText(R.string.about_title))
        aboutTitleTv.check(matches(isDisplayed()))

        clickTitleCloseButton()

        //Test go to PreonboardingStartActivity
        val logoutButton = onView(allOf(withId(R.id.ui_layout_logout), isDisplayed()))
        logoutButton.perform(click())
        val preOnboardingTitleTv = onView(allOf(withId(R.id.ui_tv_title), isDisplayed()))
        preOnboardingTitleTv.check(matches(withText(R.string.pre_onboard_tutorial_title1)))
    }


    /****************************** UserFragment ******************************/

    @Test
    fun homeExpertUserTest() {
        clickProfileButton()
        profilePictureIv.perform(click())

        //Test go to UserEditFragment
        testUserEditFragmentDisplayed(R.id.user_profile_edit_button)
        testUserEditFragmentDisplayed(R.id.user_photo)
        testUserEditFragmentDisplayed(R.id.user_name)
        testUserEditFragmentDisplayed(R.id.user_identification_email_layout)
        testUserEditFragmentDisplayed(R.id.user_identification_phone_layout)
    }

    private fun testUserEditFragmentDisplayed(clickedViewId: Int) {
        val clickedView = onView(allOf(withId(clickedViewId), isDisplayed()))
        clickedView.perform(click())
        saveProfileButton.check(matches(isDisplayed()))

        clickTitleCloseButton()
    }


    /****************************** UserEditFragment ******************************/

    @Test
    fun homeExpertUserEditTest() {
        clickProfileButton()
        editProfileButton.perform(click())

        //Test go to ChoosePhotoFragment
        val editProfilePictureButton = onView(allOf(withId(R.id.user_photo_button), isDisplayed()))
        editProfilePictureButton.perform(click())
        val takePhotoButton = onView(allOf(withId(R.id.ui_bt_take), isDisplayed()))
        takePhotoButton.check(matches(withText(R.string.onboard_photo_bt_take_photo)))

        clickTitleCloseButton()

        //Test go to UserEditProfileFragment
        testUserEditProfileFragmentDisplayed(R.id.user_firstname_layout)
        testUserEditProfileFragmentDisplayed(R.id.user_lastname_layout)

        //Test go to UserEditAboutFragment
        val editAboutButton = onView(allOf(withId(R.id.user_about_edit_button), isDisplayed()))
        editAboutButton.perform(click())
        val validateAboutButton = onView(allOf(withId(R.id.user_edit_about_save_button), isDisplayed()))
        validateAboutButton.check(matches(withText(R.string.user_edit_about_save_button)))

        validateAboutButton.perform(click())

        //Test go to UserEditActionZoneFragment
        val editZoneButton = onView(allOf(withId(R.id.ui_iv_action_zone1_mod), isDisplayed()))
        editZoneButton.perform(click())
        val editZoneTitleTv = onView(allOf(withId(R.id.ui_onboard_place_tv_title), isDisplayed()))
        editZoneTitleTv.check(matches(withText(R.string.profile_edit_zone_title)))

        clickTitleCloseButton()

        //Scroll to bottom
        val deleteAccountButton = onView(allOf(withId(R.id.user_delete_account_button)))
        deleteAccountButton.perform(scrollTo())

        //Test go to UserEditProfileType
        val editActionType = onView(allOf(withId(R.id.ui_iv_action_type_mod), isDisplayed()))
        editActionType.perform(click())
        onView(allOf(withId(R.id.ui_onboard_type_tv_description), isDisplayed()))
                .check(matches(withText(R.string.onboard_type_sub)))

        clickBackButton()

        //Test go to UserEditProfileFragment
        testUserEditProfileFragmentDisplayed(R.id.user_email_layout)

        //Test go to UserEditPasswordFragment
        val editPasswordButton = onView(allOf(withId(R.id.user_password_layout), isDisplayed()))
        editPasswordButton.perform(click())
        onView(allOf(withId(R.id.textView10), isDisplayed()))
                .check(matches(withText(R.string.user_edit_password_old_password_label)))

        clickTitleCloseButton()
    }

    private fun testUserEditProfileFragmentDisplayed(clickedViewId: Int) {
        val clickedView = onView(allOf(withId(clickedViewId), isDisplayed()))
        clickedView.perform(click())
        saveButton.check(matches(isDisplayed()))

        saveButton.perform(click())
    }


    /****************************** AboutFragment ******************************/

    @Test
    fun homeExpertAboutTest() {
        clickProfileButton()
        aboutButton.perform(click())
    }


    /****************************** Bottom bar buttons ******************************/

    @Test
    fun testFeedButton() {
        clickFeedButton()

        val feedFragmentLayout = onView(withId(R.id.ui_container))
        feedFragmentLayout.check(matches(isDisplayed()))
    }

    @Test
    fun testGuideButton() {
        clickGuideButton()

        val guideTitleTv = onView(allOf(withId(R.id.ui_title_top), isDisplayed()))
        guideTitleTv.check(matches(withText(R.string.hub_title)))
    }

    @Test
    fun testPlusButton() {
        clickPlusButton()

        val plusFragmentLayout = onView(withId(R.id.fragment_plus_overlay))
        plusFragmentLayout.check(matches(isDisplayed()))
    }

    @Test
    fun testMessagesButton() {
        clickMessagesButton()

        val messagesTabLayout = onView(withId(R.id.myentourages_tab))
        messagesTabLayout.check(matches(isDisplayed()))
    }

    @Test
    fun testProfileButton() {
        clickProfileButton()

        val editProfileTv = onView(allOf(withId(R.id.action_edit_profile), isDisplayed()))
        editProfileTv.check(matches(withText(R.string.action_edit_profile_regular)))
    }


    /****************************** Utils ******************************/

    private fun clickFeedButton() {
        bottomBarFeedButton.perform(click())
    }

    private fun clickGuideButton() {
        bottomBarGuideButton.perform(click())
    }

    private fun clickPlusButton() {
        bottomBarPlusButton.perform(click())
    }

    private fun clickMessagesButton() {
        bottomBarMessagesButton.perform(click())
    }

    private fun clickProfileButton() {
        bottomBarProfileButton.perform(click())
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

    private fun clickTitleCloseButton() {
        titleCloseButton.perform(click())
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
