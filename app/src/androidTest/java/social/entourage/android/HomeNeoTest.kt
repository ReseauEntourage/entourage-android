package social.entourage.android


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
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

    private val login: String = "698765432"
    private val password: String = "125050"


    /****************************** Views ******************************/

    private val backButton = onView(allOf(withId(R.id.ui_bt_back), isDisplayed()))


    /****************************** Before each test ******************************/

    @Before
    fun setUp() {
        //Login
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(login), closeSoftKeyboard())
        onView(withId(R.id.ui_login_et_code)).perform(typeText(password), closeSoftKeyboard())
        onView(withId(R.id.ui_login_button_signup)).perform(click())

        Thread.sleep(4000)
    }


    /****************************** HomeNeoMainFragment ******************************/

    @Test
    fun homeNeoTest() {
        //Test go to HomeNeoHelpFragment
        val firstStepLayout = onView(allOf(withId(R.id.ui_layout_button_neo_1), isDisplayed()))
        firstStepLayout.perform(click())
        val helpTitleTv = onView(allOf(withId(R.id.ui_home_neo_help_title), isDisplayed()))
        helpTitleTv.check(matches(withText(R.string.home_neo_help_title)))

        backButton.perform(click())

        //Test go to HomeNeoActionFragment
        val actNowLayout = onView(allOf(withId(R.id.ui_layout_button_neo_2), isDisplayed()))
        actNowLayout.perform(click())
        val actionTitleTv = onView(allOf(withId(R.id.textView28), isDisplayed()))
        actionTitleTv.check(matches(withText(R.string.home_neo_action_title)))
    }


    /****************************** HomeNeoHelpFragment ******************************/

    @Test
    fun helpToStreetTest() {
        val firstStepLayout = onView(allOf(withId(R.id.ui_layout_button_neo_1), isDisplayed()))
        firstStepLayout.perform(click())

        //Test go to HomeNeoStreetFragment
        val trainingLayout = onView(allOf(withId(R.id.ui_layout_button_help_1), isDisplayed()))
        trainingLayout.perform(click())
        val streetTitleTv = onView(allOf(withId(R.id.ui_home_neo_help_title), withText(R.string.home_neo_street_subtitle)))
        streetTitleTv.check(matches(isDisplayed()))
    }

    @Test
    fun helpToStartTourTest() {
        val firstStepLayout = onView(allOf(withId(R.id.ui_layout_button_neo_1), isDisplayed()))
        firstStepLayout.perform(click())

        //Test go to HomeNeoTourStartFragment
        val tourStartLayout = onView(allOf(withId(R.id.ui_layout_button_help_2), isDisplayed()))
        tourStartLayout.perform(click())
        val tourStartTitleTv = onView(allOf(withId(R.id.ui_home_neo_help_title), withText(R.string.home_neo_tour_start_title)))
        tourStartTitleTv.check(matches(isDisplayed()))
    }

    @Test
    fun helpToActionsTest() {
        val firstStepLayout = onView(allOf(withId(R.id.ui_layout_button_neo_1), isDisplayed()))
        firstStepLayout.perform(click())

        //Test go to NewsFeedActionsFragment
        val actionsLayout = onView(allOf(withId(R.id.ui_layout_button_help_3), isDisplayed()))
        actionsLayout.perform(scrollTo(), click())
        val actionsTitleTv = onView(allOf(withId(R.id.ui_tv_title), isDisplayed()))
        actionsTitleTv.check(matches(withText(R.string.home_title_events)))
    }


    /****************************** HomeNeoTourStartFragment ******************************/

    


    /****************************** HomeNeoActionFragment ******************************/




}
