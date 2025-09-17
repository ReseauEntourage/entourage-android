package social.entourage.android.tools.utils

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class CustomTypefaceSpan(private val customTypeface: Typeface) : MetricAffectingSpan() {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, customTypeface)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, customTypeface)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        val oldStyle = paint.typeface?.style ?: 0
        val fake = oldStyle and tf.style.inv()

        // Simuler gras ou italique si la police ne le propose pas
        if (fake and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }

        if (fake and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }

        paint.typeface = tf
    }
}
