package social.entourage.android.tools.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import social.entourage.android.R

/**
 * Created by Mihai Ionescu on 02/05/2018.
 */
class HalfCircleView : View {
    var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.HalfCircleView, defStyle, 0)
        paint.color = a.getColor(R.styleable.HalfCircleView_shapeColor, ContextCompat.getColor(context, R.color.background_accent_dark_translucent))
        a.recycle()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.translate(0f, -height.toFloat())
        canvas.drawArc(RectF(0F, 0F, width.toFloat(), (height * 2).toFloat()), 0f, 180f, false, paint)
    }
}