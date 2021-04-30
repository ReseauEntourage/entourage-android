package social.entourage.android


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class PreOnboardingTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun preOnboardingTest() {
        val nextButton = onView(
                allOf(withId(R.id.ui_button_next), withText("Suivant"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                9),
                        isDisplayed()))
        nextButton.perform(click())

        val textView2 = onView(
                allOf(withId(R.id.ui_tv_title),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()))
        textView2.check(matches(withText("Entraidez-vous et créez du lien !")))

        nextButton.perform(click())

        val textView3 = onView(
                allOf(withId(R.id.ui_tv_title),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()))
        textView3.check(matches(withText("Participez à des événements solidaires")))

        nextButton.perform(click())

        val textView4 = onView(
                allOf(withId(R.id.ui_tv_title),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()))
        textView4.check(matches(withText("Entourage\nvous guide")))

        nextButton.perform(click())

        val button = onView(
                allOf(withId(R.id.ui_button_signup), withText("JE M’INSCRIS"),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()))
        button.check(matches(isDisplayed()))

        val button2 = onView(
                allOf(withId(R.id.ui_button_login), withText("J’AI DÉJÀ UN COMPTE"),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()))
        button2.check(matches(isDisplayed()))
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
