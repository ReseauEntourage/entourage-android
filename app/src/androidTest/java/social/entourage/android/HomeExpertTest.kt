package social.entourage.android

import android.content.Context
import android.view.WindowManager
import androidx.test.espresso.Espresso
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Description
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import social.entourage.android.onboarding.login.LoginActivity
import java.io.IOException

//TODO @LargeTest
//TODO @RunWith(AndroidJUnit4::class)
class HomeExpertTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)

    private lateinit var context: Context

    private var jsonResponse: String = ""
    private val login: String = "651234145"
    private val password: String = "108674"

    /****************************** Views ******************************/

    //private val mainRecyclerView = onView(allOf(withId(R.id.ui_recyclerview), isDisplayed()))
    private val eventsAndActionsTitleTv = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.ui_tv_title),
            ViewMatchers.isDisplayed()
        )
    )
    //private val profilePictureIv = onView(allOf(withId(R.id.drawer_header_user_photo), isDisplayed()))
    //private val editProfileButton = onView(allOf(withId(R.id.action_edit_profile), isDisplayed()))
    private val saveProfileButton =
        Espresso.onView(ViewMatchers.withText(R.string.user_save_button))
    //private val saveButton = onView(allOf(withId(R.id.user_edit_profile_save), withText(R.string.user_button_confirm_changes)))
    //private val editPasswordButton = onView(allOf(withId(R.id.user_password_layout), isDisplayed()))
    private val oldPasswordEditText = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.user_old_password),
            ViewMatchers.isDisplayed()
        )
    )
    private val newPasswordEditText = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.user_new_password),
            ViewMatchers.isDisplayed()
        )
    )
    private val confirmPasswordEditText = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.user_confirm_password),
            ViewMatchers.isDisplayed()
        )
    )
    private val savePasswordButton = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.button_validate),
            ViewMatchers.isDisplayed()
        )
    )

    /*private val bottomBarFeedButton = onView(
            allOf(withId(R.id.bottom_bar_newsfeed),
                    withContentDescription(R.string.action_map),
                    isDisplayed()))
    private val bottomBarGuideButton = onView(
            allOf(withId(R.id.bottom_bar_guide),
                    withContentDescription(R.string.action_guide),
                    isDisplayed()))
    private val bottomBarPlusButton = onView(
            allOf(withId(R.id.bottom_bar_plus),
                    isDisplayed()))
    private val bottomBarMessagesButton = onView(
            allOf(withId(R.id.bottom_bar_mymessages),
                    withContentDescription(R.string.action_my_messages),
                    isDisplayed()))
    private val bottomBarProfileButton = onView(
            allOf(withId(R.id.bottom_bar_profile),
                    withContentDescription(R.string.action_profile),
                    isDisplayed()))*/

    private val aboutButton = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.ui_layout_help),
            ViewMatchers.isDisplayed()
        )
    )
    private val backButton = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.ui_bt_back),
            ViewMatchers.isDisplayed()
        )
    )
    //private val closeButton = onView(allOf(withId(R.id.entourage_info_close), isDisplayed()))
    private val titleCloseButton = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.title_close_button),
            ViewMatchers.isDisplayed()
        )
    )

    /****************************** Before each test ******************************/

    @Before
    fun setUp() {
        //Logout
        activityRule.scenario.onActivity { activity ->
            context = activity
            EntourageApplication.Companion[activity].authenticationController.logOutUser()
            jsonResponse = getJsonDataFromAsset(activity, "home_response_success.json") ?: ""
        }

        //Login
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone))
            .perform(ViewActions.typeText(login), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_et_code))
            .perform(ViewActions.typeText(password), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_signup))
            .perform(ViewActions.click())

        Thread.sleep(4000)
    }

    /****************************** HomeExpertFragment ******************************/

    //If you get PerformException, you may need to disable animations on your test device/emulator
    //https://stackoverflow.com/questions/44005338/android-espresso-performexception
    //TODO
    /*@Test
    fun homeExpertFeedTest() {
        //Change Home feed data with json file
        loadTestData()

        val headlineRv = onView(allOf(withId(R.id.ui_recyclerview_headline), isDisplayed()))

        //Click on an Action item type demand
        headlineRv.perform(actionOnItemAtPosition<ViewHolder>(0, click()))
        val joinButton = onView(allOf(withId(R.id.entourage_info_request_join_button), isDisplayed()))
        joinButton.check(matches(withText(context.getString(R.string.entourage_info_request_join_button_entourage)
            .uppercase(Locale.getDefault()))))

        clickCloseButton()

        //Click on an Announcement item
        headlineRv.perform(actionOnItemAtPosition<ViewHolder>(1, click()))
        val gdsTitleTv = onView(allOf(withId(R.id.textView28), isDisplayed()))
        gdsTitleTv.check(matches(withText(R.string.gds_title)))

        clickBackButton()

        //Click on an Action item type outing
        headlineRv.perform(actionOnItemAtPosition<ViewHolder>(2, click()))
        joinButton.check(matches(withText(context.getString(R.string.entourage_info_request_join_button_event)
            .uppercase(Locale.getDefault()))))

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
        joinButton.check(matches(withText(context.getString(R.string.entourage_info_request_join_button_event)
            .uppercase(Locale.getDefault()))))

        clickCloseButton()

        //Test actions section
        mainRecyclerView.perform(scrollToPosition<ViewHolder>(2))
        val actionDetailButton = onView(allOf(withId(R.id.ui_action_show_more), isDisplayed()))
        actionDetailButton.perform(click())
        eventsAndActionsTitleTv.check(matches(withText(R.string.home_title_actions)))

        clickBackButton()

        val actionsRv = onView(allOf(withId(R.id.ui_recyclerview_action), isDisplayed()))
        actionsRv.perform(actionOnItemAtPosition<ViewHolder>(0, click()))
        joinButton.check(matches(withText(context.getString(R.string.entourage_info_request_join_button_entourage)
            .uppercase(Locale.getDefault()))))

        clickCloseButton()
    }*/
    //TODO
    /*@Test
    fun retrieveFeedFailureNoInternetConnection() {
        clickGuideButton()

        //Disable wifi and data
        enableWifiAndData(false)
        Thread.sleep(1000)

        //Try to retrieve feed
        clickFeedButton()

        //Check that error is displayed
        onView(withText(R.string.network_error)).check(matches(isDisplayed()))

        //Enable wifi and data
        enableWifiAndData(true)
    }*/
    //TODO
    /*@Test
    fun homeExpertGuideTest() {
        clickGuideButton()

        val aroundMeLayout = onView(allOf(withId(R.id.ui_layout_cell_1), isDisplayed()))
        aroundMeLayout.perform(click())
        val gdsTitleTv = onView(allOf(withId(R.id.textView28), isDisplayed()))
        gdsTitleTv.check(matches(withText(R.string.gds_title)))
    }*/
    //TODO
    /*@Test
    fun contributeTest() {
        clickPlusButton()

        val contributeLayout = onView(allOf(withId(R.id.layout_line_create_entourage_contribute), isDisplayed()))
        contributeLayout.perform(click())
        val disclaimerChartTv = onView(allOf(withId(R.id.entourage_disclaimer_text_chart), withText(R.string.entourage_disclaimer_text_chart2)))
        disclaimerChartTv.check(matches(isDisplayed()))

        val acceptChartSwitch = onView(allOf(withId(R.id.entourage_disclaimer_switch), isDisplayed()))
        acceptChartSwitch.perform(click())
        Thread.sleep(1000)
        val contributeTitleTv = onView(allOf(withId(R.id.entourage_category_group_label), withText(R.string.entourage_category_type_contribution_label)))
        contributeTitleTv.check(matches(isDisplayed()))
    }*/
    //TODO
    /*@Test
    fun askHelpTest() {
        clickPlusButton()

        val askHelpLayout = onView(allOf(withId(R.id.layout_line_create_entourage_ask_help), isDisplayed()))
        askHelpLayout.perform(click())
        val disclaimerChartTv = onView(allOf(withId(R.id.entourage_disclaimer_text_chart), withText(R.string.entourage_disclaimer_text_chart2)))
        disclaimerChartTv.check(matches(isDisplayed()))

        val acceptChartSwitch = onView(allOf(withId(R.id.entourage_disclaimer_switch), isDisplayed()))
        acceptChartSwitch.perform(click())
        Thread.sleep(1000)
        val askHelpTitleTv = onView(allOf(withId(R.id.entourage_category_group_label), withText(R.string.entourage_category_type_demand_label)))
        askHelpTitleTv.check(matches(isDisplayed()))
    }*/
    //TODO
    /*@Test
    fun homeExpertMessagesTest() {
        clickMessagesButton()

        //Test open chat
        val messagesRecyclerView = onView(allOf(withId(R.id.myentourages_list_view), isDisplayed()))
        messagesRecyclerView.perform(actionOnItemAtPosition<ViewHolder>(1, click()))
        val typeMessageEditText = onView(withId(R.id.entourage_info_comment))
        typeMessageEditText.check(matches(isDisplayed()))

        //Test go to FeedItemInformationFragment
        val infoButton = onView(allOf(withId(R.id.entourage_info_description_button), isDisplayed()))
        infoButton.perform(click())
        val showOptionsButton = onView(allOf(withId(R.id.entourage_info_more_button)))
        showOptionsButton.check(matches(isDisplayed()))

        //Test go to FeedItemOptionsFragment
        showOptionsButton.perform(click())
        val cancelButton = onView(allOf(withId(R.id.entourage_option_cancel), isDisplayed()))
        cancelButton.check(matches(withText(R.string.entourage_info_options_close)))
    }*/
    //TODO
    /*@Test
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
    }*/
    //TODO
    /*@Test
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
    }*/
    //TODO
    /*@Test
    fun homeExpertUserEditTest() {
        clickProfileButton()
        editProfileButton.perform(click())

        //Test go to ChooseProfilePhotoFragment
        val editProfilePictureButton = onView(allOf(withId(R.id.user_photo_button), isDisplayed()))
        editProfilePictureButton.perform(click())
        val takePhotoButton = onView(allOf(withId(R.id.take_picture), isDisplayed()))
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
    }*/

    /****************************** UserEditProfileFragment ******************************/
    //TODO
    /*@Test
    fun testEditNamesAndEmail() {
        clickProfileButton()
        editProfileButton.perform(click())
        val firstNameLayout = onView(allOf(withId(R.id.user_firstname_layout), isDisplayed()))
        firstNameLayout.perform(click())

        //Input new firstname, lastname and email
        val randomInt = Random.nextInt(0, 1000)
        val randomFirstName = "Jean $randomInt"
        val firstNameEditText = onView(allOf(withId(R.id.user_edit_profile_firstname), isDisplayed()))
        firstNameEditText.perform(clearText(), typeText(randomFirstName))

        val randomLastName = "Dupont $randomInt"
        val lastNameEditText = onView(allOf(withId(R.id.user_edit_profile_lastname), isDisplayed()))
        lastNameEditText.perform(clearText(), typeText(randomLastName))

        val randomEmail = "jean.dupont$randomInt@jeandupont.fr"
        val emailEditText = onView(allOf(withId(R.id.user_edit_profile_email), isDisplayed()))
        emailEditText.perform(clearText(), typeText(randomEmail))

        saveButton.perform(click())

        //Test new values are set
        val firstNameTv = onView(allOf(withId(R.id.user_edit_firstname), isDisplayed()))
        firstNameTv.check(matches(withText(randomFirstName)))

        val lastNameTv = onView(allOf(withId(R.id.user_edit_lastname), isDisplayed()))
        lastNameTv.check(matches(withText(randomLastName)))

        //Scroll to bottom
        onView(allOf(withId(R.id.user_delete_account_button))).perform(scrollTo())

        val emailTv = onView(allOf(withId(R.id.user_email), isDisplayed()))
        emailTv.check(matches(withText(randomEmail)))
    }*/

    /****************************** UserEditPasswordFragment ******************************/
    //TODO
    /*@Test
    fun testEditPassword() {
        //Random valid password
        val randomPassword = "${Random.nextInt(100000, 999999)}"

        try {
            clickProfileButton()
            editProfileButton.perform(click())
            //Scroll to bottom
            onView(allOf(withId(R.id.user_delete_account_button))).perform(scrollTo())

            //Test that wrong old password show snack bar message
            changePassword("111111", "222222", "222222")
            onView(withText(R.string.user_edit_password_invalid_current_password)).check(matches(isDisplayed()))
            clickTitleCloseButton()

            //Test that too short password show snack bar message
            changePassword(password, "111", "111")
            onView(withText(R.string.user_edit_password_new_password_too_short)).check(matches(isDisplayed()))
            clickTitleCloseButton()

            //Test that different new and confirm passwords show snack bar message
            changePassword(password, "222222", "333333")
            onView(withText(R.string.user_edit_password_not_match)).check(matches(isDisplayed()))
            clickTitleCloseButton()

            //Set new valid password
            changePassword(password, randomPassword, randomPassword)

            //Check that toast shows given message
            onView(withText(R.string.user_text_update_ok)).inRoot(ToastMatcher()).check(matches(isDisplayed()))
        }
        catch (e: NoMatchingViewException) {
            //Some check went wrong
        }
        finally {
            //Reset initial password (for next test run)
            changePassword(randomPassword, password, password)

            //Check that toast shows given message
            onView(withText(R.string.user_text_update_ok)).inRoot(ToastMatcher()).check(matches(isDisplayed()))
        }
    }

    private fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        editPasswordButton.perform(click())
        oldPasswordEditText.perform(typeText(oldPassword))
        newPasswordEditText.perform(typeText(newPassword))
        confirmPasswordEditText.perform(typeText(confirmPassword))
        savePasswordButton.perform(click())
    }*/

    /****************************** AboutFragment ******************************/
    //TODO
    /*@Test
    fun testAboutTutorial() {
        goToTutorial()

        //Test swipe pages
        val viewPager = onView(allOf(withId(R.id.carousel_view), isDisplayed()))
        viewPager.perform(swipeLeft())
        val titlePage2Tv = onView(withText(R.string.carousel_p2_title))
        titlePage2Tv.check(matches(isDisplayed()))

        viewPager.perform(swipeLeft())
        val titlePage3Tv = onView(withText(R.string.carousel_p3_title))
        titlePage3Tv.check(matches(isDisplayed()))

        viewPager.perform(swipeLeft())
        val titlePage4Tv = onView(withText(R.string.carousel_p4_title))
        titlePage4Tv.check(matches(isDisplayed()))
    }*/
    //TODO
    /*@Test
    fun testCloseTutorial() {
        goToTutorial()

        val closeTutorialButton = onView(allOf(withId(R.id.carousel_close_button), isDisplayed()))
        closeTutorialButton.perform(click())
        val aboutTitleTv = onView(withText(R.string.about_title))
        aboutTitleTv.check(matches(isDisplayed()))
    }

    private fun goToTutorial() {
        clickProfileButton()
        //Scroll to bottom
        onView(withId(R.id.ui_iv_fb)).perform(scrollTo())
        aboutButton.perform(click())

        //Test go to CarouselFragment
        val tutorialButton = onView(allOf(withId(R.id.about_tutorial_layout), isDisplayed()))
        tutorialButton.perform(click())
        val titlePage1Tv = onView(withText(R.string.carousel_p1_title))
        titlePage1Tv.check(matches(isDisplayed()))
    }*/

    /****************************** UserEditAboutFragment ******************************/
    //TODO
    /*@Test
    fun testEditAbout() {
        clickProfileButton()
        editProfileButton.perform(click())

        val editAboutButton = onView(allOf(withId(R.id.user_about_edit_button), isDisplayed()))
        editAboutButton.perform(click())

        //Input new about text
        val aboutEditText = onView(allOf(withId(R.id.user_edit_about), isDisplayed()))
        val randomText = "Random text ${Random.nextInt(0, 1000)}"
        aboutEditText.perform(typeText(randomText))

        val validateAboutButton = onView(allOf(withId(R.id.user_edit_about_save_button), isDisplayed()))
        validateAboutButton.perform(click())

        //Test new value is set
        val aboutTv = onView(allOf(withId(R.id.user_about), isDisplayed()))
        aboutTv.check(matches(withText(randomText)))
    }*/

    /****************************** Bottom bar buttons ******************************/
    //TODO
    /*@Test
    fun testFeedButton() {
        clickFeedButton()

        val feedFragmentLayout = onView(withId(R.id.ui_container))
        feedFragmentLayout.check(matches(isDisplayed()))
    }*/
    //TODO
    /*@Test
    fun testGuideButton() {
        clickGuideButton()

        val guideTitleTv = onView(allOf(withId(R.id.ui_title_top), isDisplayed()))
        guideTitleTv.check(matches(withText(R.string.hub_title)))
    }*/
    //TODO
    /*@Test
    fun testPlusButton() {
        clickPlusButton()

        val plusFragmentLayout = onView(withId(R.id.fragment_plus_overlay))
        plusFragmentLayout.check(matches(isDisplayed()))
    }*/
    //TODO
    /*@Test
    fun testMessagesButton() {
        clickMessagesButton()

        val messagesTabLayout = onView(withId(R.id.myentourages_tab))
        messagesTabLayout.check(matches(isDisplayed()))
    }*/
    //TODO
    /*@Test
    fun testProfileButton() {
        clickProfileButton()

        val editProfileTv = onView(allOf(withId(R.id.action_edit_profile), isDisplayed()))
        editProfileTv.check(matches(withText(R.string.action_edit_profile_regular)))
    }*/

    /****************************** Utils ******************************/

    /*private fun clickFeedButton() {
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
    }*/

    /*private fun clickCloseButton() {
        closeButton.perform(click())
        Thread.sleep(1000)
    }*/

    private fun clickTitleCloseButton() {
        titleCloseButton.perform(ViewActions.click())
        Thread.sleep(1000)
    }

    /*private fun loadTestData() {
        runOnUiThread {
            EntBus.post(HomeCard.OnGetHomeFeed(jsonResponse))
        }
    }*/

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

    class ToastMatcher : TypeSafeMatcher<Root>() {
        override fun matchesSafely(item: Root?): Boolean {
            item?.windowLayoutParams?.get()?.type?.let { type ->
                if (type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) {
                    val windowToken = item.decorView.windowToken
                    val appToken = item.decorView.applicationWindowToken
                    if (windowToken == appToken) {
                        //Means this window isn't contained by any other windows
                        return true
                    }
                }
            }
            return false
        }

        override fun describeTo(description: Description?) {}
    }
}