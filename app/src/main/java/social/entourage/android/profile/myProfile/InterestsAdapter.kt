package social.entourage.android.profile.myProfile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.api.model.EventUtils

/** [extraLabels] : pastilles déjà traduites, affichées avant les centres d'intérêt. */
class InterestsAdapter(
    private val mList: List<String>,
    private val extraLabels: List<String> = emptyList(),
) :
    RecyclerView.Adapter<InterestsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_interest, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.label.context
        holder.label.text = if (position < extraLabels.size) {
            extraLabels[position]
        } else {
            EventUtils.showTagTranslated(context, mList[position - extraLabels.size].lowercase())
        }
    }

    override fun getItemCount(): Int {
        return extraLabels.size + mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val label: TextView = itemView.findViewById(R.id.content)
    }
}