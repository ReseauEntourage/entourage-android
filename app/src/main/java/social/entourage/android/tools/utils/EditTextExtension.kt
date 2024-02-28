package social.entourage.android.tools.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.text.style.UnderlineSpan
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import social.entourage.android.R
import social.entourage.android.language.LanguageManager
import java.text.SimpleDateFormat
import java.util.*

fun EditText.transformIntoDatePicker(
    context: Context,
    format: String,
    maxDate: Date? = null,
    minDate: Date? = null
) {
    isFocusableInTouchMode = false
    isClickable = true
    isFocusable = false
    var locale = LanguageManager.getLocaleFromPreferences(context)
    val myCalendar = Calendar.getInstance()
    val datePickerOnDataSetListener =
        DatePickerDialog.OnDateSetListener { _, _, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val sdf = SimpleDateFormat(format, locale)
            setText(sdf.format(myCalendar.time))
        }

    setOnClickListener {
        DatePickerDialog(
            context, datePickerOnDataSetListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
            myCalendar.get(Calendar.DAY_OF_MONTH)
        ).run {
            maxDate?.time?.also { datePicker.maxDate = it }
            minDate?.time?.also { datePicker.minDate = it }
            show()
        }
    }
}

fun EditText.transformIntoTimePicker(context: Context, format: String) {
    isFocusableInTouchMode = false
    isClickable = true
    isFocusable = false

    val myCalendar = Calendar.getInstance()
    var locale = LanguageManager.getLocaleFromPreferences(context)
    val timePickerOnDataSetListener =
        TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            myCalendar.set(Calendar.MINUTE, minute)
            val sdf = SimpleDateFormat(format, locale)
            setText(sdf.format(myCalendar.time))

        }

    setOnClickListener {
        TimePickerDialog(
            context, timePickerOnDataSetListener, myCalendar
                .get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE),
            true
        ).run {
            show()
        }
    }
}

fun EditText.focusAndShowKeyboard() {
    /**
     * This is to be called when the window already has focus.
     */
    fun EditText.showTheKeyboardNow() {
        if (isFocused) {
            post {
                // We still post the call, just in case we are being notified of the windows focus
                // but InputMethodManager didn't get properly setup yet.
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    requestFocus()
    if (hasWindowFocus()) {
        // No need to wait for the window to get focus.
        showTheKeyboardNow()
    } else {
        // We need to wait until the window gets focus.
        viewTreeObserver.addOnWindowFocusChangeListener(
            object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    // This notification will arrive just before the InputMethodManager gets set up.
                    if (hasFocus) {
                        this@focusAndShowKeyboard.showTheKeyboardNow()
                        // It’s very important to remove this listener once we are done.
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
    }
}

fun TextView.underline(text: String) {
    val spannableString = SpannableString(text).apply {
        setSpan(UnderlineSpan(), 0, text.length, 0)
    }
    setText(spannableString)
}

fun TextView.underlineWithDistanceUnder(textLocation: String, textDistance:String,context: Context) {
    if (textDistance != ""){
        val spannableString = SpannableString("$textLocation\n$textDistance")
        spannableString.setSpan(UnderlineSpan(), 0, textLocation.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val start = textLocation.length + 1 // +1 pour le retour à la ligne
        val end = start + textDistance.length
        val style = TextAppearanceSpan(context, R.style.center_legend_grey)
        spannableString.setSpan(style, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        setText(spannableString)
    }else{
        val spannableString = SpannableString("$textLocation")
        spannableString.setSpan(UnderlineSpan(), 0, textLocation.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val start = textLocation.length + 1 // +1 pour le retour à la ligne
        setText(spannableString)
    }

}
