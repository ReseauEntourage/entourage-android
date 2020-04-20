package social.entourage.android.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import social.entourage.android.R

/**
 * Created by Jr on 16/04/2020.
 * To create a overlay view ton create fake rounder imageview in Kitkat
 */

class RoundedCornersView : View {
    private var mRadius: Float = 0.toFloat()
    private var mColor = Color.RED
    private var mPaint: Paint? = null
    private var mPath: Path? = null

    private val DEFAULT_RADIUS = 20f
    private val DEFAULT_COLOR = Color.WHITE

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()

        val a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RoundedCornersView,
                0, 0)

        try {
            setRadius(a.getDimension(R.styleable.RoundedCornersView_radius, DEFAULT_RADIUS))
            setColor(a.getColor(R.styleable.RoundedCornersView_cornersColor, DEFAULT_COLOR))
        } finally {
            a.recycle()
        }
    }

    private fun init() {
        setColor(mColor)
        setRadius(mRadius)
    }

    private fun setColor(color: Int) {
        mColor = color
        mPaint = Paint()
        mPaint!!.setColor(mColor)
        mPaint!!.setStyle(Paint.Style.FILL)
        mPaint!!.setAntiAlias(true)

        invalidate()
    }

    private fun setRadius(radius: Float) {
        mRadius = radius
        val r = RectF(0f, 0f, 2 * mRadius, 2 * mRadius)
        mPath = Path()
        mPath!!.moveTo(0f, 0f)
        mPath!!.lineTo(0f, mRadius)
        mPath!!.arcTo(r, 180f, 90f)
        mPath!!.lineTo(0f, 0f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {

        /*This just draws 4 little inverted corners */

        val w = getWidth().toFloat()
        val h = getHeight().toFloat()
        mPath?.let { mPaint?.let { it1 -> canvas.drawPath(it, it1) } }
        canvas.save()
        canvas.translate(w, 0f)
        canvas.rotate(90f)
        mPath?.let { mPaint?.let { it1 -> canvas.drawPath(it, it1) } }
        canvas.restore()
        canvas.save()
        canvas.translate(w, h)
        canvas.rotate(180f)
        mPath?.let { mPaint?.let { it1 -> canvas.drawPath(it, it1) } }
        canvas.restore()
        canvas.translate(0f, h)
        canvas.rotate(270f)
        mPath?.let { mPaint?.let { it1 -> canvas.drawPath(it, it1) } }
    }
}