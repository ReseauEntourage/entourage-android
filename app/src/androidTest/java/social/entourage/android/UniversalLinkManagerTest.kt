package social.entourage.android

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.onboarding.login.LoginActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class UniversalLinkManagerTest : EntourageTestAfterLogin() {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @After
    override fun tearDown() {
        //keep it just for the annotation
        super.tearDown()
    }


    @Test
    fun testAppHomeLink() {
        forceLogIn()
        val uri = Uri.parse(URL)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)

        intended(hasComponent(MainActivity::class.java.name))
    }

    @Test
    fun testDemandDetailLink() {
        forceLogIn()
        // Créer l'URI que vous souhaitez simuler
        val uri = Uri.parse(URL+"solicitations/eibewY3GW-ek")

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK // S'assurer que l'intent ouvre une nouvelle activité
        }
        context.startActivity(intent)

        onView(allOf(
            withId(R.id.ui_title_main)//, withText(R.string.action_name_Demand)
        )).check(matches(isDisplayed()))
        onView(allOf(
            withText(R.string.action_name_Demand),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun testGroupDetailLink() {
        forceLogIn()
        val uri = Uri.parse(URL+"groups/bb8c3e77aa95")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)

        onView(allOf(
            withId(R.id.group_name_toolbar)//, withText(R.string.action_name_Demand)
        )).check(matches(isDisplayed()))
        onView(allOf(
            withText(R.string.group_event),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun testOutingDetailLink() {
        forceLogIn()
        val uri = Uri.parse(URL+"outings/ebJUCN-woYgM")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        onView(allOf(
            withId(R.id.event_name_toolbar)//, withText(R.string.action_name_Demand)
        )).check(matches(isDisplayed()))
        onView(allOf(
            withId(R.id.button_join),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun testGroupListLink() {
        forceLogIn()
        val uri = Uri.parse(URL+"groups")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        onView(allOf(
            withId(R.id.collapsing_toolbar)//, withText(R.string.action_name_Demand)
        )).check(matches(isDisplayed()))
        onView(allOf(
            withId(R.id.title_my_groups),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun testOutingListLink() {
        forceLogIn()
        val uri = Uri.parse(URL+"outings")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        onView(allOf(
            withId(R.id.create_event_expanded)//, withText(R.string.action_name_Demand)
        )).check(matches(isDisplayed()))
        onView(allOf(
            withId(R.id.title_section_header_event),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun testConversationDetailLink() {
        forceLogIn()
        val uri = Uri.parse(URL+"messages/er2BVAa5Vb4U")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        //TODO What to check ?
    }

    @Test
    fun testCreateContributionLink() {
        forceLogIn()
        val uri = Uri.parse(URL+"contributions/new")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        onView(allOf(
            withText(R.string.action_show_charte),
            isDisplayed()
        )).check(matches(isDisplayed()))
        onView(allOf(
            withText(R.string.accept),
            isDisplayed()
        )).perform(click())
        onView(allOf(
            withText(R.string.action_create_contrib_title),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun testCreateDemandLink() {
        forceLogIn()
        val uri = Uri.parse(URL+"solicitations/new")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        onView(allOf(
            withText(R.string.action_show_charte),
            isDisplayed()
        )).check(matches(isDisplayed()))
        onView(allOf(
            withText(R.string.accept),
            isDisplayed()
        )).perform(click())
        onView(allOf(
            withText(R.string.action_create_demand_title),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun testContributionListLink() {
        forceLogIn()
        val uri = Uri.parse(URL+"contributions")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
        onView(allOf(
            withId(R.id.create_action)//, withText(R.string.action_name_Demand)
        )).check(matches(isDisplayed()))
        onView(allOf(
            withText(R.string.actions_title),
            isDisplayed()
        )).check(matches(isDisplayed()))
        //TODO check if right tab is selected
    }

    @Test
    fun testDemandListLink() {
        forceLogIn()
        val uri = Uri.parse(URL+"solicitations")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        onView(allOf(
            withId(R.id.create_action)//, withText(R.string.action_name_Demand)
        )).check(matches(isDisplayed()))
        onView(allOf(
            withText(R.string.actions_title),
            isDisplayed()
        )).check(matches(isDisplayed()))
        //TODO check if right tab is selected
    }

    @Test
    fun testContributionDetailLink() {
        forceLogIn()
        val uri = Uri.parse(URL+"contributions/er2BVAa5Vb4U")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
        onView(allOf(
            withId(R.id.ui_title_main)//, withText(R.string.action_name_Demand)
        )).check(matches(isDisplayed()))
        onView(allOf(
            withText(R.string.action_name_Contrib),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }

    companion object {
        const val URL = "https://preprod.entourage.social/app/"
    }
}
