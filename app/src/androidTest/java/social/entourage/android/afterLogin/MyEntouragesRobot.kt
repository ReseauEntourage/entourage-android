
package social.entourage.android.afterLogin

import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import social.entourage.android.R

fun myEntouragesRobot(
    composeTestRule: ComposeTestRule? = null,
    func: MyEntouragesRobot.() -> Unit
) = MyEntouragesRobot(composeTestRule).apply { func() }

class MyEntouragesRobot(private val composeTestRule: ComposeTestRule? = null) {

    fun goToDonations() {
        clickOn(R.id.navigation_donations)
    }

    fun clickMyGroupsTab() {
        clickOn(R.string.actions_tab_mygroup)
        // Wait for ViewPager2 animation to finish
        Thread.sleep(1000)
    }

    fun clickFirstAction() {
        // Wait for the recycler view to be visible and have at least one item
        onView(allOf(withId(R.id.recycler_view), isDisplayingAtLeast(90)))
            .check(matches(hasMinimumChildCount(1)))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
    }

    fun clickModify() {
        clickOn(R.id.ui_bt_modify)
    }

    fun clickAcceptCharte() {
        if (composeTestRule != null) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            composeTestRule.onNodeWithText(context.getString(R.string.action_cgu_accept_button))
                .performClick()
            composeTestRule.waitForIdle()
        } else {
            clickOn(R.string.action_cgu_accept_button)
        }
    }

    fun clickNext() {
        clickOn(R.id.next)
    }

    fun selectCategoryAt(position: Int) {
        onView(allOf(withId(R.id.recycler_view), isDisplayingAtLeast(90)))
            .check(matches(hasMinimumChildCount(position + 1)))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(position, click()))
    }

    infix fun verify(func: MyEntouragesVerificationRobot.() -> Unit) =
        MyEntouragesVerificationRobot().apply { func() }
}

class MyEntouragesVerificationRobot {

    fun isMyGroupsTabSelected() {
        assertDisplayed(R.string.actions_tab_mygroup)
    }

    fun isActionDetailDisplayed(@StringRes vararg resIds: Int) {
        if (resIds.size == 1) {
            assertDisplayed(R.id.header_title, resIds[0])
        } else {
            val titleMatchers = resIds.map { withText(it) }
            onView(allOf(withId(R.id.header_title), isDisplayed()))
                .check(matches(anyOf(titleMatchers)))
        }
    }

    fun isActionDetailDisplayed(vararg titles: String) {
        if (titles.size == 1) {
            assertDisplayed(R.id.header_title, titles[0])
        } else {
            val titleMatchers = titles.map { withText(it) }
            onView(allOf(withId(R.id.header_title), isDisplayed()))
                .check(matches(anyOf(titleMatchers)))
        }
    }

    fun isCategoryDisplayed(@StringRes vararg resIds: Int) {
        if (resIds.size == 1) {
            assertDisplayed(resIds[0])
        } else {
            val categoryMatchers = resIds.map { withText(it) }
            onView(anyOf(categoryMatchers)).check(matches(isDisplayed()))
        }
    }

    fun isCategoryDisplayed(vararg categories: String) {
        if (categories.size == 1) {
            assertDisplayed(categories[0])
        } else {
            val categoryMatchers = categories.map { withText(it) }
            onView(anyOf(categoryMatchers)).check(matches(isDisplayed()))
        }
    }

    fun isNetworkErrorDisplayed() {
        assertDisplayed(R.string.network_error)
    }
}
