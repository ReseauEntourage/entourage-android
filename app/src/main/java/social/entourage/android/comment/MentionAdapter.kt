package social.entourage.android.comment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R

class MentionAdapter(
    private val mentionList: List<String>,
    private val onMentionSelected: (String) -> Unit
) : RecyclerView.Adapter<MentionAdapter.MentionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mention, parent, false)
        return MentionViewHolder(view)
    }

    override fun onBindViewHolder(holder: MentionViewHolder, position: Int) {
        val mention = mentionList[position]
        holder.mentionTextView.text = mention
        holder.itemView.setOnClickListener {
            onMentionSelected(mention)
        }
    }

    override fun getItemCount(): Int = mentionList.size

    class MentionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mentionTextView: TextView = itemView.findViewById(R.id.text_mention)
    }
}
