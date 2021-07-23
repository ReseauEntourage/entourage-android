package social.entourage.android


import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.onboarding.login.LoginActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class ToursTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)

    private lateinit var authenticationController: AuthenticationController

    private val login = "0606060607"
    private val password = "123456"

    @Before
    fun setUp() {
        //Logout
        activityRule.scenario.onActivity { activity ->
            authenticationController = EntourageApplication[activity].components.authenticationController
            authenticationController.logOutUser()
        }

        //Login
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(login), closeSoftKeyboard())
        onView(withId(R.id.ui_login_et_code)).perform(typeText(password), closeSoftKeyboard())
        onView(withId(R.id.ui_login_button_signup)).perform(click())

        Thread.sleep(4000)

        //If there already is an ongoing tour, close it
        if (authenticationController.savedTour != null) {
            onView(allOf(withId(R.id.ui_bt_tour), isDisplayed())).perform(click())
            stopTour()
        }
    }

    //This test needs to have no ongoing tour to pass
    @Test
    fun startAndStopTourTest() {
        val bottomBarPlusButton = onView(allOf(withId(R.id.bottom_bar_plus), isDisplayed()))
        bottomBarPlusButton.perform(click())

        startTour()
        Thread.sleep(2000)

        if(true) {
            bottomBarPlusButton.perform(click())
            Thread.sleep(1000)

            try {
                //If EncounterDisclaimerFragment is displayed, validate checkbox and click OK button
                onView(allOf(withText(R.string.encounter_disclaimer_text), isDisplayed()))
                onView(allOf(withId(R.id.encounter_disclaimer_checkbox), isDisplayed())).perform(click())
                onView(allOf(withId(R.id.encounter_disclaimer_ok_button), isDisplayed())).perform(click())
                Thread.sleep(1000)
            } catch (e: NoMatchingViewException) {
                //EncounterDisclaimerFragment is not displayed
            } finally {
                createEncounter()
                Thread.sleep(2000)

            }
        }
        stopTour()

        Thread.sleep(5000)
        assert(authenticationController.savedTour == null)
        //not possible to catch the snackbar that display just for a little while
    }

    private fun startTour() {
        val createTourLayout = onView(allOf(withId(R.id.layout_line_start_tour_launcher), isDisplayed()))
        createTourLayout.perform(click())

        val createTourButton = onView(allOf(withId(R.id.launcher_tour_go), isDisplayed()))
        createTourButton.perform(click())
    }

    private fun createEncounter() {
        //Address
        val positionLayout = onView(allOf(withId(R.id.create_encounter_position_layout), isDisplayed()))
        positionLayout.perform(click())

        val searchInput = onView(allOf(withId(R.id.places_autocomplete_search_input), isDisplayed()))
        searchInput.perform(click())

        val searchBar = onView(allOf(withId(R.id.places_autocomplete_search_bar), isDisplayed()))
        searchBar.perform(typeText("avenue des champs-elysees"), closeSoftKeyboard())

        val resultsRecyclerView = onView(allOf(withId(R.id.places_autocomplete_list), isDisplayed()))
        resultsRecyclerView.perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        Thread.sleep(1000)

        val validateButton = onView(allOf(withId(R.id.title_action_button), isDisplayed()))
        validateButton.perform(click())

        //Name
        val nameTv = onView(allOf(withId(R.id.edittext_street_person_name), isDisplayed()))
        nameTv.perform(typeText("Bernard"))

        //Description
        val descriptionTv = onView(allOf(withId(R.id.edittext_message), isDisplayed()))
        descriptionTv.perform(replaceText("Rencontre avec Bernard"))

        validateButton.perform(click())
    }

    private fun stopTour() {
        val tourStopButton = onView(allOf(withId(R.id.tour_stop_button), isDisplayed()))
        tourStopButton.perform(click())
        Thread.sleep(1000)

        val confirmStopButton = onView(allOf(withId(R.id.confirmation_end_button), isDisplayed()))
        confirmStopButton.perform(click())
    }
}
