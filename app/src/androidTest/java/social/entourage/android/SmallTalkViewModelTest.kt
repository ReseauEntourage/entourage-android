package social.entourage.android.small_talks

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.api.model.UserSmallTalkRequest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class SmallTalkViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SmallTalkViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        viewModel = SmallTalkViewModel(context)
    }

    @Test
    fun testMatchRequest() {
        val dummyId = "fake-id" // TODO : Remplacer par un vrai ID si possible
        val latch = CountDownLatch(1)
        var observedValue: Boolean? = null

        viewModel.matchResult.observeForever {
            observedValue = true
            Log.w("SmallTalkTest", "matchRequest → result: $it")
            latch.countDown()
        }

        viewModel.matchRequest(dummyId)

        if (!latch.await(30, TimeUnit.SECONDS)) {
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

        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail("LiveData did not receive value within timeout")
        }

        assertNotNull(observedValue)
    }

    @Test
    fun testListUserRequests() {
        val latch = CountDownLatch(1)
        var observedValue: List<UserSmallTalkRequest>? = null

        viewModel.userRequests.observeForever {
            observedValue = it
            Log.w("SmallTalkTest", "listUserRequests → count: ${it?.size}")
            latch.countDown()
        }

        viewModel.listUserRequests()

        if (!latch.await(30, TimeUnit.SECONDS)) {
            fail("LiveData did not receive value within timeout")
        }

        assertNotNull(observedValue)
    }
}
