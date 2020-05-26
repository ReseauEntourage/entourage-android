package social.entourage.android.tools.log

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsLog : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority <= Log.INFO) {
            //we don't record anything below Info messages (DEBUG, INFO, VERBOSE)
            return
        }

        val crashlytics = FirebaseCrashlytics.getInstance()

        crashlytics.setCustomKey(CRASHLYTICS_KEY_PRIORITY, priority)
       if(tag!=null)crashlytics.setCustomKey(CRASHLYTICS_KEY_TAG, tag)
        crashlytics.setCustomKey(CRASHLYTICS_KEY_MESSAGE, message)

        if (t == null) {
            crashlytics.recordException(Exception(message))
        } else {
            crashlytics.recordException(t)
        }
    }

    companion object {
        private const val CRASHLYTICS_KEY_PRIORITY = "priority"
        private const val CRASHLYTICS_KEY_TAG = "tag"
        private const val CRASHLYTICS_KEY_MESSAGE = "message"
    }
}