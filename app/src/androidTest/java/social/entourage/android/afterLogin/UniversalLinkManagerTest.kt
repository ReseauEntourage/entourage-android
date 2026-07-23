package social.entourage.android

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.tools.TestHelper

@LargeTest
@RunWith(AndroidJUnit4::class)
class UniversalLinkManagerTest : OpenUniversalLinkManagerTest() {

    @Before
    override fun setUp() {
        TestHelper.forceHarness = true
        super.setUp()
    }

    @After
    override fun tearDown() {
        TestHelper.forceHarness = true
        super.tearDown()
    }

    private fun startActivity(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // S'assurer que l'intent ouvre une nouvelle activité
        }
        context.startActivity(intent)
        checkNoOnboarding()

    }

    @Test
    fun testAppHomeLink() {
        val uri = Uri.parse(Companion.EntourageLink.HOME.link)
        startActivity(uri)

        onView(allOf(withText(R.string.home_title), isDisplayed()))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testDemandDetailLink() {
        // Créer l'URI que vous souhaitez simuler
        val uri = Uri.parse(Companion.EntourageLink.SOLICITATION_DETAIL.link)

        startActivity(uri)
//        onView(allOf(
//            withId(R.id.ui_title_main)//, withText(R.string.action_name_Demand)
//        )).check(matches(isDisplayed()))
        onView(allOf(
            withText(R.string.action_name_Demand),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun testGroupDetailLink() {
        val uri = Uri.parse(Companion.EntourageLink.GROUP.link)
        startActivity(uri)

//        onView(allOf(
//            withId(R.id.group_name_toolbar)//, withText(R.string.action_name_Demand)
//        )).check(matches(isDisplayed()))
        onView(allOf(
            withText(R.string.group_event),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun testOutingDetailLink() {
        val uri = Uri.parse(Companion.EntourageLink.OUTING.link)
        startActivity(uri)

        onView(allOf(
            withId(R.id.event_name_toolbar)//, withText(R.string.action_name_Demand)
        )).check(matches(isDisplayed()))
        onView(allOf(
            withId(R.id.button_join),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun testCreateContributionLink() {
        val uri = Uri.parse(Companion.EntourageLink.NEW_CONTRIBUTION.link)
        startActivity(uri)

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
        val uri = Uri.parse(Companion.EntourageLink.NEW_SOLICITATION.link)
        startActivity(uri)

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
    fun testContributionDetailLink() {
        val uri = Uri.parse(Companion.EntourageLink.CONTRIBUTION_DETAIL.link)
        startActivity(uri)

        onView(allOf(
            withText(R.string.action_name_Contrib),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }
}

