
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.EntourageTestAfterLogin
import social.entourage.android.MainActivity
import social.entourage.android.onboarding.login.LoginActivity

@RunWith(AndroidJUnit4::class)
class UniversalLinkManagerPreprodTest : EntourageTestAfterLogin() {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun pauseForTwoSeconds() {
        Thread.sleep(4000) // Pause de 2 secondes pour afficher la vue
    }

    @get:Rule
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
        //Thread.sleep(4000)
    }

    @After
    override fun tearDown() {
        //keep it just for the annotation
        super.tearDown()
    }


    @Test
    fun testDemandDetailLink() {
        forceLogIn()
        // Revenir à l'écran principal
        // Créer l'URI que vous souhaitez simuler
        val uri = Uri.parse("https://preprod.entourage.social/app/solicitations/eibewY3GW-ek")

        // Créer un intent avec l'action ACTION_VIEW pour ouvrir l'URI
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK // S'assurer que l'intent ouvre une nouvelle activité
        }
        // Démarrer l'activité en utilisant le contexte
        context.startActivity(intent)
        //(context as Activity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        //TODO What to check ?
        intended(hasComponent(MainActivity::class.java.name))
    }

    @Test
    fun testAppHomeLink() {
        forceLogIn()
        val uri = Uri.parse("https://preprod.entourage.social/app/")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)

        intended(hasComponent(MainActivity::class.java.name))
    }

    @Test
    fun testGroupDetailLink() {
        forceLogIn()
        val uri = Uri.parse("https://preprod.entourage.social/app/groups/bb8c3e77aa95")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        //TODO What to check ?
    }
    @Test
    fun testGroupListLink() {
        forceLogIn()
        val uri = Uri.parse("https://preprod.entourage.social/app/groups")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        //TODO What to check ?
    }

    @Test
    fun testOutingDetailLink() {
        forceLogIn()
        val uri = Uri.parse("https://preprod.entourage.social/app/outings/ebJUCN-woYgM")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        //TODO What to check ?
    }

    @Test
    fun testOutingListLink() {
        forceLogIn()
        val uri = Uri.parse("https://preprod.entourage.social/app/outings")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        //TODO What to check ?
    }

    @Test
    fun testConversationDetailLink() {
        forceLogIn()
        val uri = Uri.parse("https://preprod.entourage.social/app/messages/er2BVAa5Vb4U")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        //TODO What to check ?
    }

    @Test
    fun testCreateContributionLink() {
        forceLogIn()
        val uri = Uri.parse("https://preprod.entourage.social/app/contributions/new")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        //TODO What to check ?
    }

    @Test
    fun testCreateDemandLink() {
        forceLogIn()
        val uri = Uri.parse("https://preprod.entourage.social/app/solicitations/new")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        //TODO What to check ?
    }

    @Test
    fun testContributionListLink() {
        forceLogIn()
        val uri = Uri.parse("https://preprod.entourage.social/app/contributions")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
        //TODO What to check ?
    }

    @Test
    fun testDemandListLink() {
        forceLogIn()
        val uri = Uri.parse("https://preprod.entourage.social/app/solicitations")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        //TODO What to check ?
    }

    @Test
    fun testContributionDetailLink() {
        forceLogIn()
        val uri = Uri.parse("https://preprod.entourage.social/app/contributions/er2BVAa5Vb4U")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
        //TODO What to check ?
    }

    @Test
    fun testAppLink() {
        forceLogIn()
        val uri = Uri.parse("https://preprod.entourage.social/app/")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        //TODO What to check ?
    }
}
