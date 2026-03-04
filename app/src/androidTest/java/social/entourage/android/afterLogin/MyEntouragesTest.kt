package social.entourage.android.afterLogin

import android.Manifest
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import social.entourage.android.MainActivity
import social.entourage.android.R

@LargeTest
@RunWith(AndroidJUnit4::class)
class MyEntouragesTest : EntourageTestAfterLogin() {

    //private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private var activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS))
        .around(activityRule)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
            forceOnboarding(true)
        }
    }

    @After
    override fun tearDown() {
        super.tearDown()
        forceOnboarding(false)
    }

    override fun closeAutofill() {
        super.closeAutofill()
        activityRule.scenario.onActivity { activity ->
            closeAutofill(activity)
        }
    }

    @Test
    fun retrieveEntourages() {
        checkNoPopUpOnHome()
        //Try to retrieve feeds
        val bottomBarMessagesButton = onView(allOf(
            withId(R.id.navigation_donations),
            isDisplayed()
            )
        )
        bottomBarMessagesButton.perform(click())
        val myEntouragesTab = onView(allOf(withText(R.string.actions_tab_mygroup), isDisplayed()))
        myEntouragesTab.perform(click())
        onView(allOf(withText(R.string.actions_tab_mygroup), isDisplayed(), isSelected()))
            .check(ViewAssertions.matches(isDisplayed()))

        val actionList = onView(
            allOf(withId(R.id.recycler_view),
                childAtPosition(
                    withClassName(`is`("android.widget.LinearLayout")),
                    0)))
        actionList.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        val actionScreenTitle = onView(
            allOf(withId(R.id.header_title), withText("Demande"),isDisplayed()))
        actionScreenTitle.check(matches(withText("Demande")))

        val actionCategory = onView(
            allOf(withId(R.id.ui_title_cat_demand),isDisplayed()))
        actionCategory.check(matches(isDisplayed()))

        val modifyButton = onView(
            allOf(withId(R.id.ui_bt_modify), withText("Modifier"),isDisplayed()))
        modifyButton.perform(click())

        val charteButton = onView(
            allOf(withId(R.id.accept), withText("Accepter"),isDisplayed()))
        charteButton.perform(click())

        val newCatItem = onView(
            allOf(withId(R.id.recycler_view),
                childAtPosition(
                    withClassName(`is`("android.widget.LinearLayout")),
                    4)))
        newCatItem.perform(actionOnItemAtPosition<ViewHolder>(1, click()))

        val nextButton1 = onView(
            allOf(withId(R.id.next), withText("Suivant"),isDisplayed()))
        nextButton1.perform(click())

        val nextButton2 = onView(
            allOf(withId(R.id.next), withText("Suivant"),isDisplayed()))
        nextButton2.perform(click())

        val nextButton3 = onView(
            allOf(withId(R.id.next), withText("Suivant"),isDisplayed()))
        nextButton3.perform(click())

        val newActionCategory = onView(
            allOf(withId(R.id.ui_title_cat_demand),isDisplayed()))
        newActionCategory.check(matches(withText("Service")))

    }

    //TODO @Test
    fun retrieveFeedsFailureNoInternetConnection() {
        checkNoOnboarding()
        //Disable wifi and data before launching the activity
        enableWifiAndData(false)

        //Try to retrieve feeds
        val bottomBarMessagesButton = onView(allOf(withId(R.id.navigation_donations), isDisplayed()))
        bottomBarMessagesButton.perform(click())

        //Check that error is displayed
        onView(allOf(withText(R.string.network_error))).check(ViewAssertions.matches(isDisplayed()))
    }
}
