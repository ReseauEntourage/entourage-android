package social.entourage.android.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R

class HomeHorizontalWrapperAdapter(
    private val innerAdapter: RecyclerView.Adapter<*>,
    private val recycledViewPool: RecyclerView.RecycledViewPool
) : RecyclerView.Adapter<HomeHorizontalWrapperAdapter.WrapperViewHolder>() {

    var isVisible: Boolean = false

    fun setVisible(visible: Boolean) {
        if (this.isVisible != visible) {
            this.isVisible = visible
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return if (isVisible) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WrapperViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_horizontal_recycler_view, parent, false)
        return WrapperViewHolder(view, innerAdapter, recycledViewPool)
    }

    override fun onBindViewHolder(holder: WrapperViewHolder, position: Int) {
        // Nothing specific to bind as the RV is set up in ViewHolder init
        // But we might want to ensure scroll position is reset if needed?
        // For now, let's leave it.
    }

    class WrapperViewHolder(
        view: View,
        adapter: RecyclerView.Adapter<*>,
        pool: RecyclerView.RecycledViewPool
    ) : RecyclerView.ViewHolder(view) {
        val recyclerView: RecyclerView = view.findViewById(R.id.horizontal_recycler_view)

        init {
            recyclerView.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = adapter
            recyclerView.setRecycledViewPool(pool)
        }
    }
}