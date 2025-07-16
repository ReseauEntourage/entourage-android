package social.entourage.android

import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.espresso.OkHttp3IdlingResource

open class EntourageTestWithAPI {
    private var resource: IdlingResource? = null

    open fun setUp(activity: AppCompatActivity ) {
        val client = EntourageApplication[activity].apiModule.okHttpClient
        resource = OkHttp3IdlingResource.create("OkHttp", client)
        IdlingRegistry.getInstance().register(resource)
        enableWifiAndData(true)
    }

    open fun tearDown() {
        IdlingRegistry.getInstance().unregister(resource)
        enableWifiAndData(true)
    }

    protected fun enableWifiAndData(enable: Boolean) {
        val parameter = if (enable) "enable" else "disable"
        InstrumentationRegistry.getInstrumentation().uiAutomation.apply {
            //executeShellCommand("svc wifi $parameter")
            executeShellCommand("svc data $parameter")
        }
    }
}
