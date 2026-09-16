package social.entourage.android.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import social.entourage.android.R

class HomeSkeletonAdapter : RecyclerView.Adapter<HomeSkeletonAdapter.ViewHolder>() {

    // Number of skeleton sections to show (e.g. Events, Actions, Groups, Pedago)
    override fun getItemCount(): Int {
        return 4
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_skeleton_horizontal_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.shimmerFrameLayout.startShimmer()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val shimmerFrameLayout: ShimmerFrameLayout = view.findViewById(R.id.shimmer_view_container)
    }
}
