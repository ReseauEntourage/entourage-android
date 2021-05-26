package social.entourage.android


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import kotlinx.android.synthetic.main.layout_feed_action_card.view.*
import kotlinx.android.synthetic.main.layout_map_launcher.*
import kotlinx.android.synthetic.main.layout_plus_overlay.*
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.onboarding.login.LoginActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class ToursTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)

    private val login = "0606060607"
    private val password = "123456"

    @Before
    fun setUp() {
        //Logout
        activityRule.scenario.onActivity { activity ->
            EntourageApplication[activity].components.authenticationController.logOutUser()
        }

        //Login
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(login), closeSoftKeyboard())
        onView(withId(R.id.ui_login_et_code)).perform(typeText(password), closeSoftKeyboard())
        onView(withId(R.id.ui_login_button_signup)).perform(click())

        Thread.sleep(4000)
    }

    @Test
    fun tourTest() {
        val bottomBarPlusButton = onView(allOf(withId(R.id.bottom_bar_plus), isDisplayed()))
        bottomBarPlusButton.perform(click())

        val createTourLayout = onView(allOf(withId(R.id.layout_line_start_tour_launcher), isDisplayed()))
        createTourLayout.perform(click())

        val createTourButton = onView(allOf(withId(R.id.launcher_tour_go), isDisplayed()))
        createTourButton.perform(click())

        //Test tour is displayed
        val tourCardTv = onView(allOf(withId(R.id.tour_card_button_act), withText(R.string.tour_cell_button_ongoing)))
        tourCardTv.check(matches(isDisplayed()))

//        val toursButton = onView(allOf(withId(R.id.ui_bt_tour), isDisplayed()))
//        toursButton.perform(click())
    }
}
