package social.entourage.android.discussions

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.api.model.Conversation
import social.entourage.android.databinding.LayoutConversationHomeItemBinding
import java.util.*

class DiscussionsListAdapter(
    private val messagesList: MutableList<Conversation>
) : RecyclerView.Adapter<DiscussionsListAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int, conversation: Conversation)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun resetData() {
        messagesList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutConversationHomeItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messagesList[position])
    }

    override fun getItemCount(): Int = messagesList.size

    inner class ViewHolder(private val binding: LayoutConversationHomeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            // Gestion du clic
            binding.layout.setOnClickListener {
                onItemClickListener?.onItemClick(adapterPosition, conversation)
            }

            // === Affichage de l'image/avatar ===
            if (conversation.isOneToOne()) {
                binding.imagePicto.visibility = View.GONE
                conversation.user?.imageUrl?.let { url ->
                    Glide.with(binding.image.context)
                        .load(url)
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
                        conversation.user?.imageUrl?.let { url ->
                            Glide.with(binding.image.context)
                                .load(url)
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

            // === Titre et sous-infos ===
            binding.name.text = if (conversation.memberCount > 2) {
                "${conversation.title} et ${conversation.memberCount} membres"
            } else {
                conversation.title
            }

            if (conversation.type == "outing") {
                binding.date.text = conversation.subname
                binding.date.visibility = View.VISIBLE
            } else {
                binding.date.visibility = View.GONE
            }

            // === Rôles ===
            if (!conversation.getRolesWithPartnerFormated().isNullOrEmpty()) {
                binding.role.visibility = View.VISIBLE
                binding.role.text = conversation.getRolesWithPartnerFormated()
            } else {
                binding.role.visibility = View.GONE
            }

            // === Dernier message ===
            binding.detail.text = conversation.getLastMessage()

            // === État "lu/non lu" ===
            if (conversation.hasUnread()) {
                binding.nbUnread.visibility = View.VISIBLE
                binding.nbUnread.text = conversation.numberUnreadMessages.toString()
                binding.date.setTextColor(binding.root.context.resources.getColor(R.color.orange))
                binding.detail.setTextColor(binding.root.context.resources.getColor(R.color.black))
                binding.detail.setTypeface(binding.detail.typeface, Typeface.BOLD)
            } else {
                binding.nbUnread.visibility = View.INVISIBLE
                binding.date.setTextColor(binding.root.context.resources.getColor(R.color.dark_grey_opacity_40))
                binding.detail.setTextColor(binding.root.context.resources.getColor(R.color.dark_grey_opacity_40))
                if (!isLastMessageToday(conversation)) {
                    binding.detail.setTypeface(binding.detail.typeface, Typeface.BOLD)
                } else {
                    binding.detail.setTypeface(binding.detail.typeface, Typeface.NORMAL)
                }
            }

            // === Info de blocage ===
            if (conversation.imBlocker()) {
                binding.detail.text = binding.root.resources.getText(R.string.message_user_blocked_by_me_list)
                binding.detail.setTextColor(binding.root.resources.getColor(R.color.red))
                binding.detail.setTypeface(binding.detail.typeface, Typeface.NORMAL)
            }
        }

        private fun isLastMessageToday(conversation: Conversation): Boolean {
            val date = conversation.lastMessage?.date ?: return false
            val timeZone = TimeZone.getDefault()
            val calMsg = Calendar.getInstance(timeZone).apply { time = date }
            val calNow = Calendar.getInstance(timeZone)
            return calMsg.get(Calendar.ERA) == calNow.get(Calendar.ERA) &&
                    calMsg.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                    calMsg.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)
        }
    }
}
