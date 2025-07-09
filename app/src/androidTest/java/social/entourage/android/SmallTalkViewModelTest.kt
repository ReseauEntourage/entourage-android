package social.entourage.android.small_talks

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SmallTalkViewModelLogTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SmallTalkViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        viewModel = SmallTalkViewModel(context)
    }



    @Test
    fun testMatchRequest_logsResponse() {
        val dummyId = "fake-id" // Remplacer par un vrai ID si possible
        val latch = CountDownLatch(1)
        var observedValue: Boolean? = null

        viewModel.matchResult.observeForever {
            observedValue = true
            Log.w("SmallTalkTest", "matchRequest → result: $it")
            latch.countDown()
        }

        viewModel.matchRequest(dummyId)

        if (!latch.await(3, TimeUnit.SECONDS)) {
            fail("LiveData did not receive value within timeout")
        }

        assertNotNull(observedValue)
    }

    @Test
    fun testDeleteRequest() {
        val dummyId = "fake-id"
        val latch = CountDownLatch(1)
        var observedValue: Boolean? = null

        viewModel.requestDeleted.observeForever {
            observedValue = it
            Log.w("SmallTalkTest", "deleteRequest → deleted: $it")
            latch.countDown()
        }

        viewModel.deleteRequest()// Wait for the LiveData to emit, with a timeout

        if (!latch.await(3, TimeUnit.SECONDS)) {
            fail("LiveData did not receive value within timeout")
        }

        assertNotNull(observedValue)
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
