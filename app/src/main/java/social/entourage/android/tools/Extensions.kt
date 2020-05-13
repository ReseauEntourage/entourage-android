@file:Suppress("DEPRECATION")

package social.entourage.android.tools

import android.graphics.PorterDuff
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import social.entourage.android.R

/**
 * Created by Jr (MJ-DEVS) on 04/05/2020.
 */

fun ImageButton.disable() {
    Logger("Call disable")
    drawable.setColorFilter(resources.getColor(R.color.onboard_button_unselect), PorterDuff.Mode.LIGHTEN)
    isClickable = false
}

fun ImageButton.enable(res:Int) {
    Logger("Call enable")
    setImageDrawable(resources.getDrawable(res))
    isClickable = true
}

fun View.hideKeyboard() {
    clearFocus()
    val inputMethodManager = context?.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.hideSoftInputFromWindow(this.windowToken, 0)
}

fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}