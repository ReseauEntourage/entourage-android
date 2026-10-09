package social.entourage.android.unchecked

import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import social.entourage.android.events.details.feed.CreatePostEventActivity

//TODO @RunWith(AndroidJUnit4::class)
class CreatePostEventActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(CreatePostEventActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    //TODO @Test
    fun test_activity_launches() {
        // Activity is launched by the Rule
    }
}
