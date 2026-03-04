package social.entourage.android.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

class HomeSingleLayoutAdapter(
    @LayoutRes private val layoutId: Int,
    private val onBind: (View) -> Unit
) : RecyclerView.Adapter<HomeSingleLayoutAdapter.SingleViewHolder>() {

    private var isVisible: Boolean = true

    fun setVisible(visible: Boolean) {
        if (this.isVisible != visible) {
            this.isVisible = visible
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = if (isVisible) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return SingleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SingleViewHolder, position: Int) {
        onBind(holder.itemView)
    }

    class SingleViewHolder(view: View) : RecyclerView.ViewHolder(view)
}