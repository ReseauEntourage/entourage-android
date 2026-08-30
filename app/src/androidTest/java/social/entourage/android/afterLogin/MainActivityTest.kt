package social.entourage.android.afterLogin

import android.Manifest
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import social.entourage.android.MainActivity
import social.entourage.android.R

@RunWith(AndroidJUnit4::class)
class MainActivityTest : EntourageTestAfterLogin() {

    private val permissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(permissionRule)

    @Before
    fun customSetUp() {
        super.setUp(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testSimpleStartActivity() {
        checkUserIsLoggedIn()
        ActivityScenario.launch(MainActivity::class.java).use {
            checkNoOnboarding()
            myTakeSnapshot(this::class.java.simpleName)
            Espresso.onView(ViewMatchers.withText(R.string.home_title))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }
}
