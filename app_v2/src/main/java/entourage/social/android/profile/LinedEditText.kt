package entourage.social.android.profile

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import entourage.social.android.R


class LinedEditText(context: Context, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatEditText(context, attrs) {

    private val mRect: Rect = Rect()
    private val mPaint: Paint = Paint()
    private val marginVertical = 15
    override fun onDraw(canvas: Canvas) {
        val count = lineCount
        val r: Rect = mRect
        val paint: Paint = mPaint
        for (i in 0 until count) {
            val baseline = getLineBounds(i, r)
            canvas.drawLine(
                r.left.toFloat(), (baseline + marginVertical).toFloat(),
                r.right.toFloat(), (baseline + marginVertical).toFloat(), paint
            )
        }
        super.onDraw(canvas)
    }

    init {
        mPaint.style = Paint.Style.STROKE
        mPaint.color = ContextCompat.getColor(context, R.color.light_orange_opacity_50)
    }
}