package social.entourage.android.tools.view

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min

class ZoomableImageView : AppCompatImageView {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    companion object {
        private const val MIN_SCALE = 1f
        private const val MAX_SCALE = 5f
        private const val DOUBLE_TAP_SCALE = 3f
    }

    private val workingMatrix = Matrix()
    private var currentScale = MIN_SCALE

    private fun init() {
        scaleType = ScaleType.MATRIX
    }

    private val scaleGestureDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val newScale = (currentScale * detector.scaleFactor).coerceIn(MIN_SCALE, MAX_SCALE)
                val factor = newScale / currentScale
                currentScale = newScale
                workingMatrix.postScale(factor, factor, detector.focusX, detector.focusY)
                applyMatrix()
                return true
            }
        }
    )

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (currentScale > MIN_SCALE + 0.01f) {
                    resetMatrix()
                } else {
                    val factor = DOUBLE_TAP_SCALE / currentScale
                    currentScale = DOUBLE_TAP_SCALE
                    workingMatrix.postScale(factor, factor, e.x, e.y)
                    applyMatrix()
                }
                return true
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (currentScale > MIN_SCALE && !scaleGestureDetector.isInProgress) {
                    workingMatrix.postTranslate(-distanceX, -distanceY)
                    applyMatrix()
                }
                return true
            }
        }
    )

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    override fun setImageBitmap(bm: android.graphics.Bitmap?) {
        super.setImageBitmap(bm)
        fitImageToView()
    }

    override fun setImageDrawable(drawable: android.graphics.drawable.Drawable?) {
        super.setImageDrawable(drawable)
        fitImageToView()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fitImageToView()
    }

    private fun fitImageToView() {
        val d = drawable ?: return
        if (width == 0 || height == 0) return

        currentScale = MIN_SCALE
        val dw = d.intrinsicWidth.toFloat()
        val dh = d.intrinsicHeight.toFloat()
        if (dw <= 0f || dh <= 0f) return

        val scale = min(width / dw, height / dh)
        workingMatrix.reset()
        workingMatrix.postScale(scale, scale)
        workingMatrix.postTranslate((width - dw * scale) / 2f, (height - dh * scale) / 2f)
        imageMatrix = workingMatrix
    }

    private fun resetMatrix() {
        fitImageToView()
    }

    /** Keeps the scaled image within the view bounds, centering it when smaller than the view. */
    private fun applyMatrix() {
        val d = drawable
        if (d != null) {
            val rect = RectF(0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat())
            workingMatrix.mapRect(rect)

            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()

            val dx = when {
                rect.width() <= viewWidth -> (viewWidth - rect.width()) / 2 - rect.left
                rect.left > 0 -> -rect.left
                rect.right < viewWidth -> viewWidth - rect.right
                else -> 0f
            }
            val dy = when {
                rect.height() <= viewHeight -> (viewHeight - rect.height()) / 2 - rect.top
                rect.top > 0 -> -rect.top
                rect.bottom < viewHeight -> viewHeight - rect.bottom
                else -> 0f
            }
            workingMatrix.postTranslate(dx, dy)
        }
        imageMatrix = workingMatrix
    }
}
