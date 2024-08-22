import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.MainActivity

@RunWith(AndroidJUnit4::class)
class UniversalLinkManagerPreprodTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext


    private fun pauseForTwoSeconds() {
        Thread.sleep(4000) // Pause de 2 secondes pour afficher la vue
    }

    @Test
    fun testDemandDetailLink() {
        // Revenir à l'écran principal
        // Créer l'URI que vous souhaitez simuler
        val uri = Uri.parse("https://preprod.entourage.social/app/solicitations/eibewY3GW-ek")

        // Créer un intent avec l'action ACTION_VIEW pour ouvrir l'URI
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK // S'assurer que l'intent ouvre une nouvelle activité
        }
        // Démarrer l'activité en utilisant le contexte
        context.startActivity(intent)
        // Attendre 2 secondes pour observer le résultat
        pauseForTwoSeconds()
    }

    @Test
    fun testAppHomeLink() {
        val uri = Uri.parse("https://preprod.entourage.social/app/")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        pauseForTwoSeconds()
    }

    @Test
    fun testGroupDetailLink() {
        val uri = Uri.parse("https://preprod.entourage.social/app/groups/bb8c3e77aa95")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        pauseForTwoSeconds()
    }
    @Test
    fun testGroupListLink() {
        val uri = Uri.parse("https://preprod.entourage.social/app/groups")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        pauseForTwoSeconds()
    }

    @Test
    fun testOutingDetailLink() {
        val uri = Uri.parse("https://preprod.entourage.social/app/outings/ebJUCN-woYgM")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        pauseForTwoSeconds()
    }

    @Test
    fun testOutingListLink() {
        val uri = Uri.parse("https://preprod.entourage.social/app/outings")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        pauseForTwoSeconds()
    }

    @Test
    fun testConversationDetailLink() {
        val uri = Uri.parse("https://preprod.entourage.social/app/messages/er2BVAa5Vb4U")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        pauseForTwoSeconds()
    }

    @Test
    fun testCreateContributionLink() {
        val uri = Uri.parse("https://preprod.entourage.social/app/contributions/new")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        pauseForTwoSeconds()
    }

    @Test
    fun testCreateDemandLink() {
        val uri = Uri.parse("https://preprod.entourage.social/app/solicitations/new")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        pauseForTwoSeconds()
    }

    @Test
    fun testContributionListLink() {
        val uri = Uri.parse("https://preprod.entourage.social/app/contributions")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
        pauseForTwoSeconds()
    }

    @Test
    fun testDemandListLink() {
        val uri = Uri.parse("https://preprod.entourage.social/app/solicitations")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        pauseForTwoSeconds()
    }

    @Test
    fun testContributionDetailLink() {
        val uri = Uri.parse("https://preprod.entourage.social/app/contributions/er2BVAa5Vb4U")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
        pauseForTwoSeconds()
    }

    @Test
    fun testAppLink() {
        val uri = Uri.parse("https://preprod.entourage.social/app/")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        pauseForTwoSeconds()
    }
}
