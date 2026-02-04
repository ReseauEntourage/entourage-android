package social.entourage.android

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.rules.TestWatcher
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class EntourageTestWithAPI {
    private var afM: AutofillManager? = null
    protected var resource: IdlingResource? = null
    private val shouldTakeSnapshot = false

    @get:Rule
    val screenshotWatcher = object : TestWatcher() {
        override fun failed(e: Throwable?, description: org.junit.runner.Description?) {
            if(shouldTakeSnapshot) {
                takeSnapshot(description?.className ?: "Unknown")
            }
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
                Timber.d("EntourageTest", "Screenshot taken: ${file.absolutePath}")
            } else {
                Timber.e("EntourageTest", "Failed to take screenshot")
            }
        } catch (e: Exception) {
            Timber.e("EntourageTest", "Error taking screenshot", e)
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
        resource = OkHttpIdlingResource.create("OkHttp", client)
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

    protected fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: org.hamcrest.Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

    companion object {
        const val SHOULD_SET_WIFI_STATE = true
        const val SHOULD_DISABLE_GOOGLE_PWD_MGR = true
    }
}
