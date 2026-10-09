package social.entourage.android.unchecked

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import social.entourage.android.R
import social.entourage.android.tools.TestHelper

//TODO @LargeTest
//TODO @RunWith(AndroidJUnit4::class)
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

    //TODO @Test
    fun testAppHomeLink() {
        val uri = Uri.parse(Companion.EntourageLink.HOME.link)
        startActivity(uri)

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.home_title),
                ViewMatchers.isDisplayed()
            )
        )
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    //TODO @Test
    fun testDemandDetailLink() {
        // Créer l'URI que vous souhaitez simuler
        val uri = Uri.parse(Companion.EntourageLink.SOLICITATION_DETAIL.link)

        startActivity(uri)
//        onView(allOf(
//            withId(R.id.ui_title_main)//, withText(R.string.action_name_Demand)
//        )).check(matches(isDisplayed()))
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.action_name_Demand),
                ViewMatchers.isDisplayed()
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    //TODO @Test
    fun testGroupDetailLink() {
        val uri = Uri.parse(Companion.EntourageLink.GROUP.link)
        startActivity(uri)

//        onView(allOf(
//            withId(R.id.group_name_toolbar)//, withText(R.string.action_name_Demand)
//        )).check(matches(isDisplayed()))
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.group_event),
                ViewMatchers.isDisplayed()
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    //TODO @Test
    fun testOutingDetailLink() {
        val uri = Uri.parse(Companion.EntourageLink.OUTING.link)
        startActivity(uri)

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.event_name_toolbar)//, withText(R.string.action_name_Demand)
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.button_join),
                ViewMatchers.isDisplayed()
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    //TODO @Test
    fun testCreateContributionLink() {
        val uri = Uri.parse(Companion.EntourageLink.NEW_CONTRIBUTION.link)
        startActivity(uri)

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.action_show_charte),
                ViewMatchers.isDisplayed()
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.accept),
                ViewMatchers.isDisplayed()
            )
        ).perform(ViewActions.click())
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.action_create_contrib_title),
                ViewMatchers.isDisplayed()
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    //TODO @Test
    fun testCreateDemandLink() {
        val uri = Uri.parse(Companion.EntourageLink.NEW_SOLICITATION.link)
        startActivity(uri)

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.action_show_charte),
                ViewMatchers.isDisplayed()
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.accept),
                ViewMatchers.isDisplayed()
            )
        ).perform(ViewActions.click())
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.action_create_demand_title),
                ViewMatchers.isDisplayed()
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    //TODO @Test
    fun testContributionDetailLink() {
        val uri = Uri.parse(Companion.EntourageLink.CONTRIBUTION_DETAIL.link)
        startActivity(uri)

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.action_name_Contrib),
                ViewMatchers.isDisplayed()
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}