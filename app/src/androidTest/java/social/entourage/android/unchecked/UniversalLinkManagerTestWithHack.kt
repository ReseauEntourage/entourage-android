package social.entourage.android.unchecked

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import social.entourage.android.R
import social.entourage.android.tools.TestHelper

//TODO @LargeTest
//TODO @RunWith(AndroidJUnit4::class)
class UniversalLinkManagerTestWithHack : OpenUniversalLinkManagerTest() {

    @Before
    override fun setUp() {
        TestHelper.forceHarness = false
        super.setUp()
    }

    @After
    override fun tearDown() {
        TestHelper.forceHarness = true
        super.tearDown()
    }

    //TODO @Test
    fun testGroupListLink() {
        val uri = Uri.parse(Companion.EntourageLink.GROUP_LIST.link)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.collapsing_toolbar)//, withText(R.string.action_name_Demand)
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.title_my_groups),
                ViewMatchers.isDisplayed()
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    //TODO @Test
    fun testOutingListLink() {
        val uri = Uri.parse(Companion.EntourageLink.OUTINGS_LIST.link)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.create_event_expanded)//, withText(R.string.action_name_Demand)
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.title_section_header_event),
                ViewMatchers.isDisplayed()
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    //TODO @Test
    fun testContributionListLink() {
        val uri = Uri.parse(Companion.EntourageLink.CONTRIBUTIONS_LIST.link)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        context.startActivity(intent)

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.create_action)//, withText(R.string.action_name_Demand)
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.actions_title),
                ViewMatchers.isDisplayed()
            )
        )
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.actions_tab_contribs),
                ViewMatchers.isDisplayed(),
                ViewMatchers.isSelected()
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    //TODO @Test
    fun testDemandListLink() {
        val uri = Uri.parse(Companion.EntourageLink.SOLICITATIONS_LIST.link)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.actions_title),
                ViewMatchers.isDisplayed()
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.actions_tab_demands),
                ViewMatchers.isDisplayed(),
                ViewMatchers.isSelected()
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}