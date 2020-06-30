package social.entourage.android.base

import android.view.View
import android.view.View.OnLongClickListener
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import social.entourage.android.R

/**
 * Created by mihaiionescu on 10/11/16.
 */
class ItemClickSupport private constructor(private val mRecyclerView: RecyclerView) {
    private var mOnItemClickListener: OnItemClickListener? = null
    private var mOnItemLongClickListener: OnItemLongClickListener? = null

    private val mOnClickListener = View.OnClickListener { v ->
        // ask the RecyclerView for the viewHolder of this view.
        // then use it to get the position for the adapter
        mOnItemClickListener?.onItemClicked(mRecyclerView, mRecyclerView.getChildViewHolder(v).adapterPosition, v)
    }

    private val mOnLongClickListener = OnLongClickListener { v ->
        mOnItemLongClickListener?.onItemLongClicked(mRecyclerView, mRecyclerView.getChildViewHolder(v).adapterPosition, v)
                ?: false
    }

    private val mAttachListener: OnChildAttachStateChangeListener = object : OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) {
            // every time a new child view is attached add click listeners to it
            if (mOnItemClickListener != null) {
                view.setOnClickListener(mOnClickListener)
            }
            if (mOnItemLongClickListener != null) {
                view.setOnLongClickListener(mOnLongClickListener)
            }
        }

        override fun onChildViewDetachedFromWindow(view: View) {}
    }

    fun setOnItemClickListener(listener: OnItemClickListener): ItemClickSupport {
        mOnItemClickListener = listener
        return this
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener?): ItemClickSupport {
        mOnItemLongClickListener = listener
        return this
    }

    private fun detach(view: RecyclerView) {
        view.removeOnChildAttachStateChangeListener(mAttachListener)
        view.setTag(R.id.item_click_support, null)
    }

    interface OnItemClickListener {
        fun onItemClicked(recyclerView: RecyclerView?, position: Int, v: View?)
    }

    interface OnItemLongClickListener {
        fun onItemLongClicked(recyclerView: RecyclerView?, position: Int, v: View?): Boolean
    }

    companion object {
        fun addTo(view: RecyclerView): ItemClickSupport {
            // if there's already an ItemClickSupport attached to this RecyclerView do not replace it, use it
            return view.getTag(R.id.item_click_support) as ItemClickSupport? ?: ItemClickSupport(view)
        }

        fun removeFrom(view: RecyclerView): ItemClickSupport? {
            val support = view.getTag(R.id.item_click_support) as ItemClickSupport?
            support?.detach(view)
            return support
        }
    }

    init {
        // the ID must be declared in XML, used to avoid replacing the ItemClickSupport
        // without removing the old one from the RecyclerView
        mRecyclerView.setTag(R.id.item_click_support, this)
        mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener)
    }
}