package social.entourage.android.comment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.events.details.feed.EventCommentActivity

/**
 * Adapter pour afficher la liste de suggestions de mention (@) sous forme de RecyclerView.
 * @param users : liste des utilisateurs à suggérer
 * @param onItemClick : callback quand on clique sur un user
 */
class MentionAdapter(
    private val users: List<EntourageUser>,
    private val onItemClick: (EntourageUser) -> Unit
) : RecyclerView.Adapter<MentionAdapter.MentionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mention_suggestion, parent, false)
        return MentionViewHolder(view)
    }

    override fun onBindViewHolder(holder: MentionViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    class MentionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

        fun bind(user: EntourageUser) {
            // Nom affiché
            nameTextView.text = user.displayName ?: "Utilisateur ${user.userId}"
            // Pour l'avatar, on peut utiliser Glide, Coil ou Picasso si besoin. Ex :
            Glide.with(itemView).load(user.avatarURLAsString).into(avatarImageView)
            // Gestion du clic
            itemView.setOnClickListener {
                // On remonte l'EntourageUser par callback
                (itemView.context as? EventCommentActivity)?.insertMentionIntoEditText(user)
            }
        }
    }
}
