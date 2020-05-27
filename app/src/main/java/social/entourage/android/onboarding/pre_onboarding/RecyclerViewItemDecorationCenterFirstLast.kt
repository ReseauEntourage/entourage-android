package social.entourage.android.onboarding.pre_onboarding

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Jr on 15/04/2020.
 */
class RecyclerViewItemDecorationCenterFirstLast (val cellSpacing:Int): RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.left = cellSpacing
        outRect.right = cellSpacing
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.left = 0
        } else if (parent.getChildAdapterPosition(view) == state.itemCount - 1) {
            outRect.right = 0
        }
    }
}