package social.entourage.android

import androidx.test.espresso.IdlingResource
import okhttp3.Dispatcher
import okhttp3.OkHttpClient

/**
 * A custom [IdlingResource] for OkHttp.
 *
 * This is a replacement for the unmaintained `com.jakewharton.espresso:okhttp3-idling-resource`.
 * It uses the OkHttp [Dispatcher.idleCallback] to notify Espresso when the network is idle.
 */
class OkHttpIdlingResource private constructor(
    private val name: String,
    private val dispatcher: Dispatcher
) : IdlingResource {

    companion object {
        /**
         * Create a new [IdlingResource] from [client] with [name].
         */
        @JvmStatic
        fun create(name: String, client: OkHttpClient): OkHttpIdlingResource {
            return OkHttpIdlingResource(name, client.dispatcher)
        }
    }

    @Volatile
    private var callback: IdlingResource.ResourceCallback? = null

    init {
        dispatcher.idleCallback = Runnable {
            callback?.onTransitionToIdle()
        }
    }

    override fun getName(): String = name

    override fun isIdleNow(): Boolean {
        val idle = dispatcher.runningCallsCount() == 0
        if (idle) {
            callback?.onTransitionToIdle()
        }
        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }
}
