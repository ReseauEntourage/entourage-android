package social.entourage.android.onboarding

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import social.entourage.android.R
import kotlin.math.floor

/**
 * Created by Jr (MJ-DEVS) on 04/05/2020.
 */

class ProgressCellarView: View {
    private var percent = 10f
    private var bitmap: Bitmap? = null
    private var paint: Paint? = null

    constructor(context: Context?) : super(context) {
        init(null,0)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs,0)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs,defStyleAttr)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        if (paint == null) {
            paint = Paint(Paint.ANTI_ALIAS_FLAG)
        }

        bitmap?.let { bitmap ->
            val _canvas = Canvas(bitmap)
            _canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            //TODO: do we really need this _canvas? Iti is not used anywhere else

            val frame = RectF(0f,0f,width.toFloat(),height.toFloat())
            ProgressBar.drawProgressBar(canvas, frame, percent)
            canvas.drawBitmap(bitmap,0f,0f,paint)
        }
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressBarView,defStyleAttr,0)

//        percent = typeArray.getInt(R.styleable.ProgressCellarView.percent,percent)
        percent = typeArray.getFloat(R.styleable.ProgressBarView_percent,percent)

        typeArray.recycle()
    }

    fun updatePercent(percent:Float) {
        var _percent = percent
        if (_percent > 100) _percent = 100f
        this.percent = _percent
        invalidate()
    }
}

object ProgressBar {
    // Colors
    var bg_active = Color.argb(255, 245, 95, 36)
    var bg_background = Color.argb(255, 246, 246, 246)
    fun drawProgressBar(canvas: Canvas?, frame: RectF, progress: Float) {
        // General Declarations
        val percent = frame.width() / 100f * progress
        val paint = CacheForProgressBar.paint

        // progressBackground
        val progressBackgroundRect = CacheForProgressBar.progressBackgroundRect
        progressBackgroundRect[frame.left, frame.top, frame.left + floor(frame.width() + 0.5f.toDouble()).toFloat()] = frame.bottom
        val progressBackgroundPath = CacheForProgressBar.progressBackgroundPath
        progressBackgroundPath.reset()
        progressBackgroundPath.addRoundRect(progressBackgroundRect, frame.bottom, frame.bottom, Path.Direction.CW)
        paint.reset()
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.style = Paint.Style.FILL
        paint.color = bg_background
        canvas?.drawPath(progressBackgroundPath, paint)

        // progressActive
        canvas?.saveLayerAlpha(null, 204, Canvas.ALL_SAVE_FLAG)
        run {
            val progressActiveRect = CacheForProgressBar.progressActiveRect
            progressActiveRect[0f, 0f, percent] = frame.bottom
            val progressActivePath = CacheForProgressBar.progressActivePath
            progressActivePath.reset()
            progressActivePath.addRoundRect(progressActiveRect, frame.bottom, frame.bottom, Path.Direction.CW)
            paint.reset()
            paint.flags = Paint.ANTI_ALIAS_FLAG
            paint.style = Paint.Style.FILL
            paint.color = bg_active
            canvas?.drawPath(progressActivePath, paint)
        }
        canvas?.restore()
    }

    // Canvas Drawings
    // Tab
    private object CacheForProgressBar {
        val paint = Paint()
        val progressBackgroundRect = RectF()
        val progressBackgroundPath = Path()
        val progressActiveRect = RectF()
        val progressActivePath = Path()
    }
}