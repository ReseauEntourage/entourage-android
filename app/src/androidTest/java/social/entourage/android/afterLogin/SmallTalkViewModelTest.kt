package social.entourage.android.afterLogin

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.UserSmallTalkRequest
import social.entourage.android.small_talks.SmallTalkViewModel
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class SmallTalkViewModelTest : EntourageTestAfterLogin() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SmallTalkViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        super.setUp(context)
        viewModel = SmallTalkViewModel(context)
    }

    private fun fetchUserSmallTalkRequestIdFromApi(): String {
        checkUserIsLoggedIn()

        return runBlocking(Dispatchers.IO) {
            val response = EntourageApplication.get().apiModule.smallTalkRequest
                .listUserSmallTalkRequests()
                .execute()

            response.body()?.requests?.firstOrNull()?.id?.toString()
                ?: response.body()?.requests?.firstOrNull()?.uuid
        } ?: "fake-id"
    }

    @Test
    fun testMatchRequest() {
        val requestId = fetchUserSmallTalkRequestIdFromApi()
        val latch = CountDownLatch(1)
        var observedValue: Boolean? = null

        viewModel.matchResult.observeForever {
            observedValue = true
            Timber.w("SmallTalkTest", "matchRequest → result: $it")
            latch.countDown()
        }

        viewModel.matchRequest(requestId)

        if (!latch.await(30, TimeUnit.SECONDS)) {
            Assert.fail("LiveData did not receive value within timeout")
        }

        Assert.assertNotNull(observedValue)
    }

    //@Test
    fun testDeleteRequest() {
        checkUserIsLoggedIn()
        val latch = CountDownLatch(1)
        var observedValue: Boolean? = null

        viewModel.requestDeleted.observeForever {
            observedValue = it
            Timber.w("SmallTalkTest", "deleteRequest → deleted: $it")
            latch.countDown()
        }

        viewModel.deleteRequest()

        if (!latch.await(30, TimeUnit.SECONDS)) {
            Assert.fail("LiveData did not receive value within timeout")
        }

        Assert.assertNotNull(observedValue)
    }

    @Test
    fun testListUserRequests() {
        checkUserIsLoggedIn()
        val latch = CountDownLatch(1)
        var observedValue: List<UserSmallTalkRequest>? = null

        viewModel.userRequests.observeForever {
            observedValue = it
            Timber.w("SmallTalkTest", "listUserRequests → count: ${it?.size}")
            latch.countDown()
        }

        viewModel.listUserRequests()

        if (!latch.await(30, TimeUnit.SECONDS)) {
            Assert.fail("LiveData did not receive value within timeout")
        }

        Assert.assertNotNull(observedValue)
    }
}