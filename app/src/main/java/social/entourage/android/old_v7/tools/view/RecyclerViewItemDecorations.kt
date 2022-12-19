package social.entourage.android.old_v7.tools.view

import android.content.res.Resources
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/*
 * RecyclerView item decoration with spacing start/end + inter cell
 */
class RecyclerViewItemDecorationWithSpacing (val cellSpacing:Int, val startSpacing:Int,val resources: Resources): RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val cellSpace = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                cellSpacing.toFloat(),
                resources.displayMetrics
        )

        val startSpace = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                startSpacing.toFloat(),
                resources.displayMetrics
        )

        outRect.left = cellSpace.toInt()
        outRect.right = cellSpace.toInt()
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.left = startSpace.toInt()
        } else if (parent.getChildAdapterPosition(view) == state.itemCount - 1) {
            outRect.right = startSpace.toInt()
        }
    }
}