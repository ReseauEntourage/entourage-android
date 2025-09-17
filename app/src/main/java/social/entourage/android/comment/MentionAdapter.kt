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

/**
 * Adapter pour afficher la liste de suggestions de mention (@) sous forme de RecyclerView.
 * La liste est limitée à 3 éléments.
 */
class MentionAdapter(
    private var users: List<EntourageUser>,
    private val onItemClick: (EntourageUser) -> Unit
) : RecyclerView.Adapter<MentionAdapter.MentionViewHolder>() {

    // Limite à 7 utilisateurs maximum
    //TODO Change here for number of mention
    override fun getItemCount(): Int = minOf(5, users.size)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mention_suggestion, parent, false)
        return MentionViewHolder(view)
    }

    override fun onBindViewHolder(holder: MentionViewHolder, position: Int) {
        holder.bind(users[position], position, onItemClick)
    }

    /**
     * Met à jour la liste des utilisateurs et notifie le changement.
     */
    fun updateList(newUsers: List<EntourageUser>) {
        users = newUsers
        notifyDataSetChanged()
    }

    class MentionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

        fun bind(user: EntourageUser, position: Int, onItemClick: (EntourageUser) -> Unit) {
            // Affiche le nom de l'utilisateur
            nameTextView.text = user.displayName ?: "Utilisateur ${user.userId}"
            val bgColorRes = R.color.beige_clair
            itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, bgColorRes))
            // Charge l'avatar en utilisant Glide avec transformation de cercle et placeholder
            Glide.with(itemView)
                .load(user.avatarURLAsString)
                .placeholder(R.drawable.placeholder_user)
                .error(R.drawable.placeholder_user)
                .apply(RequestOptions.circleCropTransform())
                .into(avatarImageView)
            // Gestion du clic : appelle le callback
            itemView.setOnClickListener {
                val bgColorRes = R.color.beige
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, bgColorRes))
                onItemClick(user)
            }
        }
    }
}
