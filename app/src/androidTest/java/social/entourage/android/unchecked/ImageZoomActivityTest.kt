package social.entourage.android.unchecked

import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.comment.ImageZoomActivity

@RunWith(AndroidJUnit4::class)
class ImageZoomActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(ImageZoomActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun test_activity_launches() {
        // Activity is launched by the Rule
    }
}