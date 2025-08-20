package social.entourage.android.comment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser

class MentionAdapter(
    private var users: List<EntourageUser>,
    private val onItemClick: (EntourageUser) -> Unit
) : RecyclerView.Adapter<MentionAdapter.MentionViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        // Évite les clignotements lors des updates
        val id = users.getOrNull(position)?.id ?: users.getOrNull(position)?.userId ?: 0
        return id.toLong()
    }

    override fun getItemCount(): Int = users.size // ← plus de limite

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mention_suggestion, parent, false)
        return MentionViewHolder(view)
    }

    override fun onBindViewHolder(holder: MentionViewHolder, position: Int) {
        holder.bind(users[position], position, onItemClick)
    }

    fun updateList(newUsers: List<EntourageUser>) {
        users = newUsers
        notifyDataSetChanged()
    }

    class MentionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

        fun bind(user: EntourageUser, position: Int, onItemClick: (EntourageUser) -> Unit) {
            nameTextView.text = user.displayName ?: "Utilisateur ${user.userId}"
            itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.beige_clair))

            Glide.with(itemView)
                .load(user.avatarURLAsString)
                .placeholder(R.drawable.placeholder_user)
                .error(R.drawable.placeholder_user)
                .apply(RequestOptions.circleCropTransform())
                .into(avatarImageView)

            itemView.setOnClickListener {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.beige))
                onItemClick(user)
            }
        }
    }
}
