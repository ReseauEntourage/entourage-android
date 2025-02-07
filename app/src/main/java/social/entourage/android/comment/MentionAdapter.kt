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
import social.entourage.android.events.details.feed.EventCommentActivity
import social.entourage.android.groups.details.feed.GroupCommentActivity

/**
 * Adapter pour afficher la liste de suggestions de mention (@) sous forme de RecyclerView,
 * avec un maximum de 7 items et un callback de clic.
 */
class MentionAdapter(
    private val users: List<EntourageUser>,
    private val onItemClick: (EntourageUser) -> Unit
) : RecyclerView.Adapter<MentionAdapter.MentionViewHolder>() {

    // On limite à 7 utilisateurs max
    override fun getItemCount(): Int = minOf(7, users.size)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mention_suggestion, parent, false)
        return MentionViewHolder(view)
    }

    override fun onBindViewHolder(holder: MentionViewHolder, position: Int) {
        // S'il y a plus de 7 users, position < 7
        holder.bind(users[position], position, onItemClick)
    }

    class MentionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

        fun bind(user: EntourageUser, position: Int, onItemClick: (EntourageUser) -> Unit) {
            // Nom affiché
            nameTextView.text = user.displayName ?: "Utilisateur ${user.userId}"

            // Couleur de fond alternée : une ligne sur deux
            val isEvenRow = (position % 2 == 0)
            val bgColorRes = if (isEvenRow) R.color.beige_clair else R.color.beige
            itemView.setBackgroundColor(
                ContextCompat.getColor(itemView.context, bgColorRes)
            )

            // Chargement de l'avatar en rond + placeholder
            Glide.with(itemView)
                .load(user.avatarURLAsString)
                .placeholder(R.drawable.placeholder_user)
                .error(R.drawable.placeholder_user)
                .apply(RequestOptions.circleCropTransform())
                .into(avatarImageView)

            // Gestion du clic
            itemView.setOnClickListener {
                // Appel du callback si besoin
                onItemClick(user)

                // OU, si tu veux vraiment qu'on insère la mention directement :
                val ctx = itemView.context
                when (ctx) {
                    is EventCommentActivity -> ctx.insertMentionIntoEditText(user)
                    is GroupCommentActivity -> ctx.insertMentionIntoEditText(user)
                    else -> {
                        // Pas géré
                    }
                }
            }
        }
    }
}
