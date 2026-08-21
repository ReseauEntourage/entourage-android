package social.entourage.android.tools.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R

private fun Context.getSmoothScroll(): LinearSmoothScroller {
    return object : LinearSmoothScroller(this) {
        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }
    }
}

fun RecyclerView.scrollToPositionSmooth(int: Int) {
    this.layoutManager?.startSmoothScroll(this.context.getSmoothScroll().apply {
        targetPosition = int
    })
}

/**
 * Scrolls this NestedScrollView so [view] ends up near the top of the visible viewport,
 * regardless of which/how many siblings above it are currently GONE.
 */
fun NestedScrollView.smoothScrollToView(view: View, topOffsetPx: Int = 24.px) {
    val viewLocation = IntArray(2)
    view.getLocationInWindow(viewLocation)
    val containerLocation = IntArray(2)
    this.getLocationInWindow(containerLocation)
    val targetY = this.scrollY + (viewLocation[1] - containerLocation[1]) - topOffsetPx
    this.smoothScrollTo(0, targetY.coerceAtLeast(0))
}

/**
 * Briefly flashes an overlay color on top of the view's existing background, to draw the
 * user's eye to it (e.g. after auto-scrolling to it from a notification deep link).
 */
fun View.flashHighlight() {
    val overlay = ColorDrawable(ContextCompat.getColor(context, R.color.orange)).apply { alpha = 140 }
    foreground = overlay
    postDelayed({
        ObjectAnimator.ofInt(overlay, "alpha", 140, 0).apply {
            duration = 600
            addUpdateListener { overlay.invalidateSelf() }
        }.start()
    }, 450)
    postDelayed({ foreground = null }, 1100)
}
