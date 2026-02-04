package social.entourage.android

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.autofill.AutofillManager
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.jakewharton.espresso.OkHttp3IdlingResource
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

open class EntourageTestWithAPI {
    private var afM: AutofillManager? = null
    protected var resource: IdlingResource? = null

    @get:Rule
    val screenshotWatcher = object : TestWatcher() {
        override fun failed(e: Throwable?, description: Description?) {
            takeSnapshot(description?.className ?: "Unknown")
        }
    }

    private fun takeSnapshot(className: String) {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = sdf.format(Date())
        val fileName = "${timestamp}_${className}.png"
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val storageDir = instrumentation.targetContext.filesDir
        val file = File(storageDir, fileName)
        try {
            if (UiDevice.getInstance(instrumentation).takeScreenshot(file)) {
                Log.d("EntourageTest", "Screenshot taken: ${file.absolutePath}")
            } else {
                Log.e("EntourageTest", "Failed to take screenshot")
            }
        } catch (e: Exception) {
            Log.e("EntourageTest", "Error taking screenshot", e)
        }
    }

    open fun setUp(activity: Context) {
        afM = activity.getSystemService(AutofillManager::class.java)
        afM?.disableAutofillServices()
        if (SHOULD_DISABLE_GOOGLE_PWD_MGR && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Autofill settings are relevant
            try {
                val instrumentation = InstrumentationRegistry.getInstrumentation()
                instrumentation.uiAutomation.executeShellCommand("settings put secure autofill_service null")
            } catch (e: Exception) {
                Log.e("TestSetup", "Failed to disable autofill_service via UiAutomation", e)
            }
        }

        val client = EntourageApplication[activity].apiModule.okHttpClient
        resource = OkHttp3IdlingResource.create("OkHttp", client)
        IdlingRegistry.getInstance().register(resource)

        enableWifiAndData(true)
    }

    open fun tearDown() {
        IdlingRegistry.getInstance().unregister(resource)
        if (SHOULD_DISABLE_GOOGLE_PWD_MGR && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Autofill settings are relevant
            try {
                val instrumentation = InstrumentationRegistry.getInstrumentation()
                instrumentation.uiAutomation.executeShellCommand("settings put secure autofill_service com.google.android.gms/com.google.android.gms.autofill.service.AutofillService")
            } catch (e: Exception) {
                Log.e("TestSetup", "Failed to enable autofill_service via UiAutomation", e)
            }
        }
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
    protected fun closeAutofill(activity: Context?) {
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
        const val SHOULD_DISABLE_GOOGLE_PWD_MGR = true
    }
}
