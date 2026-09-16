package social.entourage.android.tools

import android.view.MotionEvent
import android.view.Window
import timber.log.Timber

/**
 * Guards against a known Android framework crash: tapping a TextView/EditText right as its
 * text becomes empty can make ArrowKeyMovementMethod call Selection.setSelection(-1, -1),
 * which throws IndexOutOfBoundsException deep inside dispatchTouchEvent with no app frame
 * in the stack. Swallowing just that touch event here is the standard mitigation since the
 * bug is in the platform/emoji2 text layout code, not reachable from app code.
 */
class SafeTouchWindowCallback(private val base: Window.Callback) : Window.Callback by base {
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return try {
            base.dispatchTouchEvent(event)
        } catch (e: IndexOutOfBoundsException) {
            Timber.e(e, "Ignored known Android text-selection touch crash")
            true
        }
    }
}
