@file:Suppress("DEPRECATION")

package social.entourage.android.tools

import android.graphics.PorterDuff
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import androidx.core.content.res.ResourcesCompat
import social.entourage.android.R
import timber.log.Timber

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

fun View.hideKeyboard() {
    clearFocus()
    val inputMethodManager = context?.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.hideKeyboardFromLayout() {
    val inputMethodManager = context?.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.hideSoftInputFromWindow(this.windowToken, 0)
}

fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}