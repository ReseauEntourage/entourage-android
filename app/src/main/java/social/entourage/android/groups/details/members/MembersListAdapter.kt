package social.entourage.android.groups.details.members

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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
import social.entourage.android.home.HomeFragment
import social.entourage.android.members.MembersActivity
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.tools.utils.Const
import social.entourage.android.ui.ActionSheetFragment
import timber.log.Timber

interface OnItemShowListener {
    fun onShowConversation(userId: Int)
    fun onToggleParticipation(user: EntourageUser, isChecked: Boolean, photoAcceptance: Boolean?)

}

class MembersListAdapter(
    private val context:Context,
    private var membersList: List<EntourageUser>,
    private var reactionList: List<ReactionType>,
    private var onItemShowListener: OnItemShowListener,
    private var iAmOrganiser: Boolean? = false ,

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
        val b = holder.binding
        val item = membersList[position]

        // -------- Checkbox participation (visibilité + état + listener) --------
        val isMe = EntourageApplication.get().me()?.id == item.userId
        if (HomeFragment.signablePermission && ActionSheetFragment.isSignable && !isMe && !MembersActivity.isFromReact) {

            b.checkboxConfirmation.visibility = View.VISIBLE
            // état actuel : participe si participateAt ou confirmedAt non null
            val isParticipating = (item.participateAt != null) || (item.confirmedAt != null)

            b.checkboxConfirmation.setOnCheckedChangeListener(null)
            b.checkboxConfirmation.isChecked = isParticipating
            b.checkboxConfirmation.setOnCheckedChangeListener { _, isChecked ->
                // on délègue : Activity décidera d'appeler participate ou cancel_participation
                onItemShowListener.onToggleParticipation(item, isChecked, item.photoAcceptance)
            }
        } else {
            // important pour le recyclage : enlever le listener avant de cacher
            b.checkboxConfirmation.setOnCheckedChangeListener(null)
            b.checkboxConfirmation.visibility = View.GONE
        }

        // -------- Nom + badge "organizer" (Spannable coloré) --------
        val isOrganizer = item.groupRole == "organizer"
        if (isOrganizer) {
            b.layout.background = ContextCompat.getDrawable(context, R.drawable.background_organizer)

            val roleLabel = if (isMe) {
                context.getString(R.string.animateur_label)
            } else {
                context.getString(R.string.organizer_label)
            }

            val baseName = item.displayName ?: ""
            val full = baseName + roleLabel
            val spannable = SpannableString(full).apply {
                setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.organizer_text)),
                    baseName.length,
                    full.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            b.name.text = spannable
        } else {
            b.layout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            b.name.text = item.displayName
            b.name.setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        // Suffixe "Participation confirmée" si applicable (on garde la logique existante)
        if (item.confirmedAt != null) {
            b.name.text = "${b.name.text} - Participation confirmée"
            // idéalement : b.name.text = context.getString(R.string.participation_confirmed_suffix, b.name.text)
        }

        // -------- Rôles communauté --------
        val roles = item.getCommunityRoleWithPartnerFormated()
        if (roles != null) {
            b.ambassador.visibility = View.VISIBLE
            b.ambassador.text = roles
        } else {
            b.ambassador.visibility = View.GONE
        }

        // -------- Réaction (index safe) --------
        if (position < reactionList.size) {
            b.reaction.layoutItemReactionParent.visibility = View.VISIBLE
            Glide.with(context)
                .load(reactionList[position].imageUrl)
                .into(b.reaction.reactionImage)
        } else {
            b.reaction.layoutItemReactionParent.visibility = View.GONE
        }

        // -------- Avatar --------
        item.avatarURLAsString?.let { url ->
            Glide.with(holder.itemView.context)
                .load(url)
                .placeholder(R.drawable.placeholder_user)
                .error(R.drawable.placeholder_user)
                .circleCrop()
                .into(b.picture)
        } ?: run {
            Glide.with(holder.itemView.context)
                .load(R.drawable.placeholder_user)
                .circleCrop()
                .into(b.picture)
        }

        // -------- Contact (caché si moi) --------
        b.contact.visibility = if (isMe || iAmOrganiser == true) View.GONE else View.VISIBLE

        // -------- Click profil --------
        b.layout.setOnClickListener { view ->
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            ProfileFullActivity.isMe = false
            ProfileFullActivity.userId = item.userId.toString()
            (view.context as? Activity)?.startActivityForResult(
                Intent(view.context, ProfileFullActivity::class.java)
                    .putExtra(Const.USER_ID, item.userId),
                0
            )
        }

        // -------- Click conversation --------
        b.contact.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            onItemShowListener.onShowConversation(item.userId)
        }
    }



    override fun getItemCount(): Int {
        return membersList.size
    }
}