package social.entourage.android.groups.details.members

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.ReactionType
import social.entourage.android.databinding.NewGroupMemberItemBinding
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.user.UserProfileActivity
import social.entourage.android.tools.utils.Const

interface OnItemShowListener {
    fun onShowConversation(userId: Int)
}

class MembersListAdapter(
    private val context:Context,
    private var membersList: List<EntourageUser>,
    private var reactionList: List<ReactionType>,
    private var onItemShowListener: OnItemShowListener,

    ) : RecyclerView.Adapter<MembersListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: NewGroupMemberItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewGroupMemberItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    fun resetData(membersList: List<EntourageUser>, reactionList: List<ReactionType>) {
        this.membersList = membersList
        this.reactionList = reactionList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(membersList[position]) {
                // Vérifier si le membre a le rôle "organizer"
                if (groupRole == "organizer") {
                    val isMe = EntourageApplication.get().me()?.id == userId

                    // Changer la couleur du fond du layout
                    binding.layout.background = ContextCompat.getDrawable(context, R.drawable.background_organizer)

                    // Obtenir le label approprié à partir des ressources
                    val roleLabel = if (!isMe) {
                        context.getString(R.string.organizer_label)
                    } else {
                        context.getString(R.string.animateur_label)
                    }

                    // Créer un SpannableString pour ajouter le texte avec le label dynamique
                    val nameText = displayName
                    val spannable = SpannableString(nameText + roleLabel)

                    spannable.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(context, R.color.organizer_text)),
                        nameText?.length ?: 0, // Start of the role label
                        spannable.length, // End of the text
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    binding.name.text = spannable
                } else {
                    // Réinitialiser la couleur du fond et du texte pour les autres membres
                    binding.layout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                    binding.name.text = displayName
                    binding.name.setTextColor(ContextCompat.getColor(context, R.color.black))
                }


                // Vérifier si le membre a une réaction et l'afficher
                if (reactionList.isNotEmpty()) {
                    binding.reaction.layoutItemReactionParent.visibility = View.VISIBLE
                    Glide.with(context)
                        .load(reactionList[position].imageUrl)
                        .into(binding.reaction.image)
                } else {
                    binding.reaction.layoutItemReactionParent.visibility = View.GONE
                }

                // Vérifier si le membre est l'utilisateur actuel pour masquer le bouton "Contact"
                val isMe = EntourageApplication.get().me()?.id == userId
                binding.contact.visibility = if (isMe) View.INVISIBLE else View.VISIBLE

                // Ajouter un label si la participation est confirmée
                if (membersList[position].confirmedAt != null) {
                    binding.name.text = "${binding.name.text} - Participation confirmée"
                }

                // Gérer les rôles de la communauté
                val roles = getCommunityRoleWithPartnerFormated()
                if (roles != null) {
                    binding.ambassador.visibility = View.VISIBLE
                    binding.ambassador.text = roles
                } else {
                    binding.ambassador.visibility = View.GONE
                }

                // Charger l'avatar ou afficher le placeholder par défaut
                avatarURLAsString?.let { avatarURL ->
                    Glide.with(holder.itemView.context)
                        .load(avatarURL)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(binding.picture)
                } ?: run {
                    Glide.with(holder.itemView.context)
                        .load(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(binding.picture)
                }

                // Gestion du clic pour afficher le profil de l'utilisateur
                binding.layout.setOnClickListener { view ->
                    ProfileFullActivity.isMe = false
                    ProfileFullActivity.userId = this.userId.toString()
                    (view.context as? Activity)?.startActivityForResult(
                        Intent(view.context, ProfileFullActivity::class.java).putExtra(
                            Const.USER_ID,
                            this.userId
                        ), 0
                    )
                }

                // Gestion du clic pour initier une conversation
                binding.contact.setOnClickListener {
                    onItemShowListener.onShowConversation(userId)
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return membersList.size
    }
}