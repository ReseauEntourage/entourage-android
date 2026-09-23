package social.entourage.android.deeplinks

import android.content.Intent
import androidx.core.net.toUri
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.MainActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class DeepLinksEspressoTest {

    @Test
    fun testAllCatalogDeeplinksNavigation() {
        DeeplinkTestData.getTestCases().forEach { testCase ->
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                val intent = Intent(Intent.ACTION_VIEW, testCase.uriString.toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                scenario.onActivity { activity ->
                    DeepLinksManager.storeIntent(intent)
                    DeepLinksManager.handleCurrentDeepLink(activity)
                }

                val resolvedView = DeepLinksManager.resolveDeepLinkView(testCase.uriString)
                assertNotNull("DeepLink view resolution failed for ${testCase.uriString}", resolvedView)
            }
        }
    }
}
