package social.entourage.android.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.URLSpan
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.Events
import social.entourage.android.base.BaseActivity
import social.entourage.android.events.EventModel
import timber.log.Timber
import java.util.*
import kotlin.math.roundToInt

/**
 * Created by Jr (MJ-DEVS) on 04/05/2020.
 */

fun TextView.setHyperlinkClickable() {
    val pattern = Patterns.WEB_URL
    val matcher = pattern.matcher(this.text)
    if (matcher.find()) {
        val url = matcher.group()
        val spannableString = SpannableString(text)
        spannableString.setSpan(URLSpan(url), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
        this.movementMethod = EntLinkMovementMethod
        this.setOnClickListener {
            (this.context as? BaseActivity)?.showWebView(url)
        }
    }
}


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
    val yesterday = Date(today.time)
    val endDate = this.metadata?.endsAt
    if (endDate != null) {
        if (endDate.before(yesterday)) {
            return true
        }
    }
    return false
}

fun EventModel.displayDistance(context:Context):String{
    val distance = this.distance
    if(this.online == true){
        return ""
    }
    if (distance != null) {
        if (distance > 0){
            val formattedString = context.getString(R.string.at_km_from_me, distance.roundToInt())
            return formattedString
        }else{
            val distInMeter = distance * 1000
            if (distInMeter > 100){
                val formattedString = context.getString(R.string.at_meter_from_me, distInMeter.roundToInt())
                return formattedString
            }else{
                val formattedString = context.getString(R.string.inf_hundred_meter)
                return formattedString
            }
        }
    }
    return ""
}

fun Action.displayDistance(context:Context):String{
        val distance = this.distance
        if (distance != null) {
            if (distance > 1){
                val formattedString = context.getString(R.string.at_km_from_me, distance.roundToInt())
                return formattedString
            }else{
                val distInMeter:Double = distance * 1000
                if (distInMeter > 100){
                    val formattedString = context.getString(R.string.at_meter_from_me, distInMeter.roundToInt())
                    return formattedString
                }else{
                    val formattedString = context.getString(R.string.inf_hundred_meter)
                    return formattedString
                }
            }
        }
    return ""
}

