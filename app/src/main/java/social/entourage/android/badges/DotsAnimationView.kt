package social.entourage.android.badges

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import social.entourage.android.R

class DotsAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private data class Dot(
        val x: Float,
        val y: Float,
        val radius: Float,
        val colorRes: Int,
        val phaseOffset: Float
    )

    private val dotColorRes = listOf(
        R.color.orange,
        R.color.green,
        R.color.dodger_blue,
        R.color.red_dark
    )

    private val dots = mutableListOf<Dot>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var animProgress = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2500
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            animProgress = it.animatedValue as Float
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        dots.clear()
        if (w == 0 || h == 0) return

        val positions = listOf(
            0.15f to 0.18f, 0.75f to 0.12f, 0.88f to 0.35f,
            0.08f to 0.55f, 0.92f to 0.60f, 0.20f to 0.82f,
            0.80f to 0.80f, 0.50f to 0.08f, 0.55f to 0.90f,
            0.35f to 0.25f, 0.65f to 0.22f, 0.30f to 0.70f,
            0.70f to 0.68f, 0.42f to 0.50f, 0.10f to 0.38f
        )

        positions.forEachIndexed { i, (xRatio, yRatio) ->
            dots.add(
                Dot(
                    x = w * xRatio,
                    y = h * yRatio,
                    radius = (4 + (i % 3) * 2).toFloat(),
                    colorRes = dotColorRes[i % dotColorRes.size],
                    phaseOffset = i / positions.size.toFloat()
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        dots.forEach { dot ->
            val phase = ((animProgress + dot.phaseOffset) % 1f)
            // pulse: 0→1→0
            val alpha = (Math.sin(phase * Math.PI) * 200).toInt().coerceIn(30, 200)
            paint.color = ContextCompat.getColor(context, dot.colorRes)
            paint.alpha = alpha
            canvas.drawCircle(dot.x, dot.y, dot.radius, paint)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }
}
