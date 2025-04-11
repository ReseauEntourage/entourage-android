package social.entourage.android.tools

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.os.Build
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import social.entourage.android.R
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.Events
import social.entourage.android.base.BaseActivity
import social.entourage.android.events.EventModel
import timber.log.Timber
import java.util.Date
import kotlin.math.roundToInt

/**
 * Created by Jr (MJ-DEVS) on 04/05/2020.
 */

// Votre fonction customisée qui remplace les URLSpan par des URLSpan personnalisés
fun TextView.setHyperlinkClickable() {
    val spannable = SpannableString(text)
    // Récupérer tous les URLSpan existants
    val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
    spans.forEach { span ->
        val start = spannable.getSpanStart(span)
        val end = spannable.getSpanEnd(span)
        spannable.removeSpan(span)
        // Ajout d'un span personnalisé qui appelle showWebView
        spannable.setSpan(object : URLSpan(span.url) {
            override fun onClick(widget: View) {
                (widget.context as? BaseActivity)?.showWebView(span.url)
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    setText(spannable, TextView.BufferType.SPANNABLE)
    movementMethod = LinkMovementMethod.getInstance()
    linksClickable = true
    setLinkTextColor(Color.BLUE)
}


fun TextView.displayHtml(textContent: String) {
    var processedText = textContent
    if (!processedText.contains("<a", ignoreCase = true)) {
        val matcher = Patterns.WEB_URL.matcher(processedText)
        val sb = StringBuffer()
        while (matcher.find()) {
            val url = matcher.group()
            matcher.appendReplacement(sb, "<a href=\"$url\">$url</a>")
        }
        matcher.appendTail(sb)
        processedText = sb.toString()
    }
    processedText = processedText
        .replace(Regex("<p[^>]*>"), "")
        .replace("</p>", "<br>")
        .replace("\n", "<br>")

    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(processedText, Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(processedText)
    }
    setHyperlinkClickable()
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

fun Activity.updatePaddingBottomForEdgeToEdge(viewTop:View){
    // Listen for WindowInsets
    androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(viewTop) { view, windowInsets ->
        // Get the insets for the statusBars() type:
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
        view.layoutParams.height = insets.bottom +view.minimumHeight
        view.updatePadding(bottom =  insets.bottom)
        // Return the original insets so they aren’t consumed
        windowInsets
    }
}

fun Activity.updatePaddingTopForEdgeToEdge(viewTop:View){
    // Listen for WindowInsets
    androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(viewTop) { view, windowInsets ->
        // Get the insets for the statusBars() type:
        val insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars())
        view.updatePadding(
            top = insets.top
        )
        // Return the original insets so they aren’t consumed
        windowInsets
    }
}

fun Fragment.updatePaddingTopForEdgeToEdge(viewTop:View){
    // Listen for WindowInsets
    androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(viewTop) { view, windowInsets ->
        // Get the insets for the statusBars() type:
        val insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars())
        view.updatePadding(
            top = insets.top
        )
        // Return the original insets so they aren’t consumed
        windowInsets
    }
}

