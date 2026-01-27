package social.entourage.android.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R

class HomeSectionHeaderAdapter : RecyclerView.Adapter<HomeSectionHeaderAdapter.HeaderViewHolder>() {
    var title: String = ""
    var subtitle: String? = null
    var isVisible: Boolean = false

    fun update(title: String, subtitle: String?, isVisible: Boolean) {
        this.title = title
        this.subtitle = subtitle
        this.isVisible = isVisible
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (isVisible) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_section_header, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.titleView.text = title
        if (subtitle != null) {
            holder.subtitleView.text = subtitle
            holder.subtitleView.visibility = View.VISIBLE
        } else {
            holder.subtitleView.visibility = View.GONE
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.section_title)
        val subtitleView: TextView = view.findViewById(R.id.section_subtitle)
    }
}