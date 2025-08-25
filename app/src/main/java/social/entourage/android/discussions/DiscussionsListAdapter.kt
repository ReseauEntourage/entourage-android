package social.entourage.android.discussions

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.api.model.Conversation
import social.entourage.android.databinding.LayoutConversationHomeItemBinding
import timber.log.Timber
import java.util.Calendar
import java.util.TimeZone

interface OnItemClick {
    fun onItemClick(position: Int)
}

class DiscussionsListAdapter(
    private var messagesList: List<Conversation>,
    private var onItemClickListener: OnItemClick
) : RecyclerView.Adapter<DiscussionsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionsListAdapter.ViewHolder {
        val view = LayoutConversationHomeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(val binding: LayoutConversationHomeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation, position: Int) {

            binding.layout.setOnClickListener {
                onItemClickListener.onItemClick(position)
            }

            // === Image / avatar ===
            if (conversation.isOneToOne()) {
                binding.imagePicto.isVisible = false
                conversation.user?.imageUrl?.let {
                    Glide.with(binding.image.context)
                        .load(it)
                        .error(R.drawable.placeholder_user)
                        .transform(CenterCrop(), CircleCrop())
                        .into(binding.image)
                } ?: run {
                    Glide.with(binding.image.context)
                        .load(R.drawable.placeholder_user)
                        .transform(CenterCrop(), CircleCrop())
                        .into(binding.image)
                }
            } else {
                conversation.type?.let { type ->
                    if (type == "outing") {
                        Timber.d("type : $type")
                        if (conversation.imageUrl.isNullOrBlank()) {
                            Glide.with(binding.image.context)
                                .load(R.drawable.placeholder_my_event)
                                .transform(CenterCrop(), RoundedCorners(10))
                                .into(binding.image)
                        } else {
                            Glide.with(binding.image.context)
                                .load(conversation.imageUrl)
                                .transform(CenterCrop(), RoundedCorners(10))
                                .error(R.drawable.placeholder_my_event)
                                .into(binding.image)
                        }
                    } else {
                        conversation.user?.imageUrl?.let {
                            Glide.with(binding.image.context)
                                .load(it)
                                .error(R.drawable.placeholder_user)
                                .transform(CenterCrop(), CircleCrop())
                                .into(binding.image)
                        } ?: run {
                            Glide.with(binding.image.context)
                                .load(R.drawable.placeholder_user)
                                .transform(CenterCrop(), CircleCrop())
                                .into(binding.image)
                        }
                    }
                }
            }

            // === Titre / sous-infos ===
            binding.name.text = conversation.title
            if (conversation.memberCount > 2) {
                binding.name.text = conversation.title + " et ${conversation.memberCount}" + " membres"
            }

            if (conversation.type == "outing") {
                Timber.wtf("wtf date " + conversation.subname)
                binding.date.text = conversation.subname
                binding.date.visibility = View.VISIBLE
            } else {
                binding.date.visibility = View.GONE
            }

            // Rôles
            if (conversation.getRolesWithPartnerFormated()?.isEmpty() == false) {
                binding.role.isVisible = true
                binding.role.text = conversation.getRolesWithPartnerFormated()
            } else {
                binding.role.isVisible = false
            }

            // Dernier message (texte)
            binding.detail.text = conversation.getLastMessage()

            // Non-lus : style existant conservé
            if (conversation.hasUnread()) {
                binding.nbUnread.visibility = View.VISIBLE
                binding.nbUnread.text = "${conversation.numberUnreadMessages}"
                binding.date.setTextColor(binding.root.context.resources.getColor(R.color.orange))
                binding.detail.setTextColor(binding.root.context.resources.getColor(R.color.black))
                binding.detail.setTypeface(binding.detail.typeface, Typeface.BOLD)
            } else {
                binding.nbUnread.visibility = View.INVISIBLE
                binding.date.setTextColor(binding.root.context.resources.getColor(R.color.dark_grey_opacity_40))
                binding.detail.setTextColor(binding.root.context.resources.getColor(R.color.dark_grey_opacity_40))
                binding.detail.setTypeface(binding.detail.typeface, Typeface.NORMAL)
            }

            // Block info
            if (conversation.imBlocker()) {
                binding.detail.text = binding.detail.resources.getText(R.string.message_user_blocked_by_me_list)
                binding.detail.setTextColor(binding.detail.resources.getColor(R.color.red))
            }

            // === AJOUT : mettre en gras si le dernier message n'est PAS aujourd'hui ===
            val notToday = !isLastMessageToday(conversation)

            // Essaye d'utiliser la vraie fonte quicksand_bold si elle existe, sinon fallback BOLD
            val ctx = binding.root.context
            val quicksandBold = try {
                ResourcesCompat.getFont(ctx, R.font.quicksand_bold)
            } catch (_: Exception) {
                null
            }

            // Titre
            if (notToday) {
                if (quicksandBold != null) {
                    binding.name.typeface = quicksandBold
                } else {
                    binding.name.setTypeface(binding.name.typeface, Typeface.BOLD)
                }
            } else {
                binding.name.setTypeface(binding.name.typeface, Typeface.NORMAL)
            }

            // (Optionnel) La date (visible pour outings) suit la même règle
            if (binding.date.visibility == View.VISIBLE) {
                if (notToday) {
                    if (quicksandBold != null) {
                        binding.date.typeface = quicksandBold
                    } else {
                        binding.date.setTypeface(binding.date.typeface, Typeface.BOLD)
                    }
                } else {
                    binding.date.setTypeface(binding.date.typeface, Typeface.NORMAL)
                }
            }
        }

        /**
         * Retourne true si la date du dernier message de la conversation est "aujourd'hui" (en TZ locale).
         * Si la date est absente → retourne false (=> on affichera en gras comme demandé).
         */
        private fun isLastMessageToday(conversation: Conversation): Boolean {
            val d = conversation.lastMessage?.date ?: return false
            val tz = TimeZone.getDefault()
            val calMsg = Calendar.getInstance(tz).apply { time = d }
            val calNow = Calendar.getInstance(tz)
            return calMsg.get(Calendar.ERA) == calNow.get(Calendar.ERA) &&
                    calMsg.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                    calMsg.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)
        }
    }

    override fun onBindViewHolder(holder: DiscussionsListAdapter.ViewHolder, position: Int) {
        if (position >= 0 && position < messagesList.size) {
            holder.bind(messagesList[position], position)
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }
}
