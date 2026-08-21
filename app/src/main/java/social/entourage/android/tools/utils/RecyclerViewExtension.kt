package social.entourage.android.tools.utils

import android.content.Context
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

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
 * Positions this NestedScrollView so [view] ends up near the top of the visible viewport,
 * regardless of which/how many siblings above it are currently GONE. Jumps there directly
 * (no scroll animation) — meant to be called while a loading skeleton still hides the feed,
 * so the target view is already in place once the skeleton is removed.
 */
fun NestedScrollView.scrollToView(view: View, topOffsetPx: Int = 24.px) {
    val viewLocation = IntArray(2)
    view.getLocationInWindow(viewLocation)
    val containerLocation = IntArray(2)
    this.getLocationInWindow(containerLocation)
    val targetY = (this.scrollY + (viewLocation[1] - containerLocation[1]) - topOffsetPx).coerceAtLeast(0)
    scrollTo(0, targetY)
}
