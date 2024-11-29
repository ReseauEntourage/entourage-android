package social.entourage.android.tools.utils

import android.graphics.Paint
import android.text.TextPaint
import android.text.style.TypefaceSpan
import android.graphics.Typeface

class CustomTypefaceSpan(private val newTypeface: Typeface?) : TypefaceSpan("") {
    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, newTypeface)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, newTypeface)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface?) {
        tf?.let {
            paint.typeface = it
        }
    }
}
