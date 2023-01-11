package social.entourage.android.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import social.entourage.android.R
import social.entourage.android.api.model.Events
import timber.log.Timber
import java.util.*

/**
 * Created by Jr (MJ-DEVS) on 04/05/2020.
 */

fun ImageButton.disable() {
    Timber.d("Call disable")
    drawable.setColorFilter(resources.getColor(R.color.onboard_button_unselect), PorterDuff.Mode.LIGHTEN)
    isClickable = false
}

fun ImageButton.enable(res:Int) {
    Timber.d("Call enable")
    setImageDrawable(ResourcesCompat.getDrawable(resources,res,null))
    isClickable = true
}

fun Button.disable() {
    alpha = 0.5f
    isClickable = false
    isEnabled = false
}

fun Button.enable() {
    alpha = 1f
    isClickable = true
    isEnabled = true
}

fun View.hideKeyboard() {
    clearFocus()
    val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.hideKeyboardFromLayout() {
    val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.hideSoftInputFromWindow(this.windowToken, 0)
}

fun String?.isValidEmail(): Boolean {
    return this?.let {
        android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
    } ?: false
}

fun EditText.showKeyboard() {
    post {
        requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }
}

fun TextView.disable() {
    alpha = 0.5f
    isClickable = false
    isEnabled = false
}

fun TextView.enable() {
    alpha = 1f
    isClickable = true
    isEnabled = true
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Events.calculateIfEventPassed():Boolean{

    val today = Date()
    val startDate = this.metadata?.endsAt
    if (startDate != null) {
        if (startDate.before(today)) {
            return true
        }
    }
    return false
}