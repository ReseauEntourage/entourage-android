package social.entourage.android.small_talks

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.api.model.UserSmallTalkRequest

@RunWith(AndroidJUnit4::class)
class SmallTalkViewModelLogTest {

    private lateinit var viewModel: SmallTalkViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        viewModel = SmallTalkViewModel(context)
    }



    @Test
    fun testMatchRequest_logsResponse() {
        val dummyId = "fake-id" // Remplacer par un vrai ID si possible

        viewModel.matchRequest(dummyId)

        viewModel.matchResult.observeForever {
            Log.w("SmallTalkTest", "matchRequest → result: $it")
        }

        Thread.sleep(3000)
    }

    @Test
    fun testDeleteRequest_logsResponse() {
        val dummyId = "fake-id"

        viewModel.deleteRequest(dummyId)

        viewModel.requestDeleted.observeForever {
            Log.w("SmallTalkTest", "deleteRequest → deleted: $it")
        }

        Thread.sleep(3000)
    }

    @Test
    fun testListUserRequests_logsResponse() {
        viewModel.listUserRequests()

        viewModel.userRequests.observeForever {
            Log.w("SmallTalkTest", "listUserRequests → count: ${it?.size}")
        }

        Thread.sleep(3000)
    }
}
