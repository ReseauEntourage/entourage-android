package social.entourage.android.base

import social.entourage.android.api.model.TimestampedObject

open class HeaderFooterBaseAdapter : HeaderBaseAdapter() {
    protected var showBottomView = false
    protected var bottomViewContentType = 0
    protected fun showBottomView(showBottomView: Boolean, bottomViewContentType: Int) {
        this.showBottomView = showBottomView
        this.bottomViewContentType = bottomViewContentType
        if (items != null) {
            notifyItemChanged(bottomViewPosition)
        }
    }

    override fun getItemCount(): Int {
        return if (items == null) {
            0
        } else items.size + positionOffset + 1
        // +1 for the footer
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            TimestampedObject.BOTTOM_VIEW
        } else super.getItemViewType(position)
    }

    private val bottomViewPosition: Int
        get() = if (items != null) {
            items.size + positionOffset
        } else 0
}