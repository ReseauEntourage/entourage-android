package social.entourage.android.unchecked

import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import social.entourage.android.comment.ImageZoomActivity

//TODO @RunWith(AndroidJUnit4::class)
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

    //TODO @Test
    fun testImageZoom() {
        // Activity is launched by the Rule
    }
}