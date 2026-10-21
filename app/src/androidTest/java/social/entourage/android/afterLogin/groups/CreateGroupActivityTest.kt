package social.entourage.android.afterLogin.groups

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.e2e.E2EScreenshot
import social.entourage.android.groups.create.CreateGroupActivity

/**
 * L'assistant de création de groupe est en Compose : ses éléments se ciblent par `testTag`
 * ("create_group_*", voir CreateGroupScreen / CreateGroupSteps).
 */
@RunWith(AndroidJUnit4::class)
class CreateGroupActivityTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("create_group")

    private val activityRule = ActivityScenarioRule(CreateGroupActivity::class.java)

    @get:Rule
    val composeTestRule = AndroidComposeTestRule(
        activityRule = activityRule,
        activityProvider = { rule ->
            var activity: CreateGroupActivity? = null
            rule.scenario.onActivity { activity = it }
            activity ?: error("Activity was not created")
        }
    )

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun test_activity_launches() {
        composeTestRule.onNodeWithTag("create_group_step_label").assertIsDisplayed()
        composeTestRule.onNodeWithTag("create_group_name").assertIsDisplayed()
        screenshot.shoot("creation_groupe_lancement")
    }

    @Test
    fun test_empty_step_one_shows_field_errors_and_keeps_button_active() {
        composeTestRule.onNodeWithTag("create_group_next").performClick()
        composeTestRule.onNodeWithTag("create_group_name_error").assertIsDisplayed()
        composeTestRule.onNodeWithTag("create_group_description_error").assertIsDisplayed()
        composeTestRule.onNodeWithTag("create_group_location_error").assertIsDisplayed()
        // L'étape n'est pas validée : on reste sur l'étape 1.
        composeTestRule.onNodeWithTag("create_group_name").assertIsDisplayed()
    }
}
