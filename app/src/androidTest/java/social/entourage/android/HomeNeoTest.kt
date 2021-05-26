package social.entourage.android


import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.onboarding.login.LoginActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class HomeNeoTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)

    private lateinit var context: Context

    private val login: String = "698765432"
    private val password: String = "125050"


    /****************************** Views ******************************/

    private val backButton = onView(allOf(withId(R.id.ui_bt_back), isDisplayed()))


    /****************************** Before each test ******************************/

    @Before
    fun setUp() {
        //Logout
        activityRule.scenario.onActivity { activity ->
            context = activity
            EntourageApplication[activity].components.authenticationController.logOutUser()
        }

        //Login
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(login), closeSoftKeyboard())
        onView(withId(R.id.ui_login_et_code)).perform(typeText(password), closeSoftKeyboard())
        onView(withId(R.id.ui_login_button_signup)).perform(click())

        Thread.sleep(4000)

        //Could be neo info dialog
        try {
            //If dialog is displayed, click OK button
            onView(allOf(withText(R.string.home_neo_pop_info_title), isDisplayed()))
            onView(allOf(withText(R.string.home_neo_pop_info_button_ok), isDisplayed())).perform(click())
        } catch (e: NoMatchingViewException) {
            //Dialog is not displayed
        }
    }


    /****************************** HomeNeoMainFragment ******************************/

    @Test
    fun homeNeoTest() {
        //Test go to HomeNeoHelpFragment
        goToHomeNeoHelpFragment()
        val helpTitleTv = onView(allOf(withId(R.id.ui_home_neo_help_title), isDisplayed()))
        helpTitleTv.check(matches(withText(R.string.home_neo_help_title)))

        backButton.perform(click())

        //Test go to HomeNeoActionFragment
        goToHomeNeoActionFragment()
        val actionTitleTv = onView(allOf(withId(R.id.textView28), isDisplayed()))
        actionTitleTv.check(matches(withText(R.string.home_neo_action_title)))
    }


    /****************************** HomeNeoHelpFragment ******************************/

    @Test
    fun helpToStreetTest() {
        goToHomeNeoHelpFragment()

        //Test go to HomeNeoStreetFragment
        goToHomeNeoStreetFragment()
        val streetTitleTv = onView(allOf(withId(R.id.ui_home_neo_help_title), withText(R.string.home_neo_street_subtitle)))
        streetTitleTv.check(matches(isDisplayed()))
    }

    @Test
    fun helpToStartTourTest() {
        goToHomeNeoHelpFragment()

        //Test go to HomeNeoTourStartFragment
        goToHomeNeoTourStartFragment()
        val tourStartTitleTv = onView(allOf(withId(R.id.ui_home_neo_help_title), withText(R.string.home_neo_tour_start_title)))
        tourStartTitleTv.check(matches(isDisplayed()))
    }

    @Test
    fun helpToActionsTest() {
        goToHomeNeoHelpFragment()

        //Test go to NewsFeedActionsFragment
        goToNewsFeedActionsFragment()
        val actionsTitleTv = onView(allOf(withId(R.id.ui_tv_title), isDisplayed()))
        actionsTitleTv.check(matches(withText(R.string.home_title_events)))
    }


    /****************************** HomeNeoTourStartFragment ******************************/

    @Test
    fun tourStartTest() {
        goToHomeNeoHelpFragment()

        goToHomeNeoTourStartFragment()

        //Test go to HomeNeoTourListFragment
        goToHomeNeoTourListFragment()
        val tourStartAreasTv = onView(allOf(withId(R.id.ui_home_neo_help_title2), isDisplayed()))
        tourStartAreasTv.check(matches(withText(R.string.home_neo_tour_areas_title)))
    }


    /****************************** HomeNeoTourListFragment ******************************/

    @Test
    fun tourListTest() {
        goToHomeNeoHelpFragment()

        goToHomeNeoTourStartFragment()

        goToHomeNeoTourListFragment()

        //Test go to HomeNeoTourSendFragment
        goToHomeNeoTourSendFragment()
        val sendTourButton = onView(allOf(withId(R.id.ui_button_send_tour), isDisplayed()))
        sendTourButton.check(matches(withText(R.string.home_neo_tour_send_button)))
    }


    /****************************** HomeNeoActionFragment ******************************/

    @Test
    fun actionsTest() {
        goToHomeNeoActionFragment()

        //Test go to NewsFeedActionsFragment -> actions
        val answerNeedButton = onView(allOf(withId(R.id.ui_home_action_button_1), isDisplayed()))
        answerNeedButton.perform(click())
        val actionsTitleTv = onView(allOf(withId(R.id.ui_tv_title), withText(R.string.home_title_actions)))
        actionsTitleTv.check(matches(isDisplayed()))
    }

    @Test
    fun eventsTest() {
        goToHomeNeoActionFragment()

        //Test go to NewsFeedActionsFragment -> events
        val eventsButton = onView(allOf(withId(R.id.ui_home_action_button_2), isDisplayed()))
        eventsButton.perform(click())
        val eventsTitleTv = onView(allOf(withId(R.id.ui_tv_title), withText(R.string.home_title_events)))
        eventsTitleTv.check(matches(isDisplayed()))
    }

    @Test
    fun offerMaterialTest() {
        actionTest(R.id.ui_home_action_button_3, R.string.entourage_create_type_contribution, "Faire un don matériel")
    }

    @Test
    fun offerServiceTest() {
        actionTest(R.id.ui_home_action_button_4, R.string.entourage_create_type_contribution, "Offrir un service")
    }

    @Test
    fun relayNeedTest() {
        actionTest(R.id.ui_home_action_button_5, R.string.entourage_create_type_demand, "Un don matériel")
    }

    @Test
    fun offerCoffeeTest() {
        actionTest(R.id.ui_home_action_button_6, R.string.entourage_create_type_contribution, "Partager un repas, un café")
    }

    private fun actionTest(buttonId: Int, actionTypeId: Int, actionText: String) {
        goToHomeNeoActionFragment()

        val actionButton = onView(allOf(withId(buttonId), isDisplayed()))
        actionButton.perform(click())

        onView(allOf(withId(R.id.entourage_disclaimer_text_chart), isDisplayed()))
        onView(allOf(withId(R.id.entourage_disclaimer_switch), isDisplayed())).perform(click())
        Thread.sleep(1000)

        val entourageCategoryTv = onView(allOf(withId(R.id.create_entourage_category), isDisplayed()))
        entourageCategoryTv.check(matches(withText(context.getString(actionTypeId, actionText))))
    }


    /****************************** Utils ******************************/

    private fun goToHomeNeoHelpFragment() {
        val firstStepLayout = onView(allOf(withId(R.id.ui_layout_button_neo_1), isDisplayed()))
        firstStepLayout.perform(click())
    }

    private fun goToHomeNeoActionFragment() {
        val actNowLayout = onView(allOf(withId(R.id.ui_layout_button_neo_2), isDisplayed()))
        actNowLayout.perform(click())
    }

    private fun goToHomeNeoStreetFragment() {
        val trainingLayout = onView(allOf(withId(R.id.ui_layout_button_help_1), isDisplayed()))
        trainingLayout.perform(click())
    }

    private fun goToHomeNeoTourStartFragment() {
        val tourStartLayout = onView(allOf(withId(R.id.ui_layout_button_help_2), isDisplayed()))
        tourStartLayout.perform(click())
    }

    private fun goToNewsFeedActionsFragment() {
        val actionsLayout = onView(allOf(withId(R.id.ui_layout_button_help_3), isDisplayed()))
        actionsLayout.perform(scrollTo(), click())
    }

    private fun goToHomeNeoTourListFragment() {
        val tourStartButton = onView(allOf(withId(R.id.ui_home_neo_button_tour_start), isDisplayed()))
        tourStartButton.perform(click())
    }

    private fun goToHomeNeoTourSendFragment() {
        val tourRecyclerView = onView(allOf(withId(R.id.ui_recyclerview), isDisplayed()))
        tourRecyclerView.perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
    }

}
