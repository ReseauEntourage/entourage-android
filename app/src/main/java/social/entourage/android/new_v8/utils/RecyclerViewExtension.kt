package social.entourage.android.new_v8.utils

import android.content.Context
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
