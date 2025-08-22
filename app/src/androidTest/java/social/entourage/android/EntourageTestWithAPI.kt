package social.entourage.android

import android.view.autofill.AutofillManager
import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.espresso.OkHttp3IdlingResource

open class EntourageTestWithAPI {
    private var afM: AutofillManager? = null
    private var resource: IdlingResource? = null

    open fun setUp(activity: AppCompatActivity ) {
        val client = EntourageApplication[activity].apiModule.okHttpClient
        afM = activity.getSystemService(AutofillManager::class.java)
        afM?.disableAutofillServices()
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
            if(SHOULD_SET_WIFI_STATE) {
                executeShellCommand("svc wifi $parameter")
            }
            executeShellCommand("svc data $parameter")
        }
    }
    protected fun closeAutofill(activity: AppCompatActivity?) {
        if (afM == null) {
            afM = activity?.getSystemService(AutofillManager::class.java)
        }
        afM?.cancel()
        afM?.commit()

        // Attempt to dismiss password suggestion dialog using UI Automator (less reliable)
        /*try {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            val dismissButton = device.findObject(UiSelector().clickable(true).instance(0)) // Adjust selector as needed
            if (dismissButton.exists()) {
                dismissButton.click()
            }
        } catch (e: Exception) {
            Timber.d(e)
        }*/
    }

    companion object {
        const val SHOULD_SET_WIFI_STATE = true
    }
}
