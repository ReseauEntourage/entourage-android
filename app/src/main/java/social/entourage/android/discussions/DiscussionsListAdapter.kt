package social.entourage.android.discussions

import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.gson.Gson
import social.entourage.android.R
import social.entourage.android.api.model.Conversation
import social.entourage.android.databinding.LayoutConversationHomeItemBinding
import social.entourage.android.tools.utils.Utils
import timber.log.Timber

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
            }
            else {
                conversation.type?.let { type ->
                    if (type == "outing") {
                        Timber.d("type : $type")
                        if (conversation.imageUrl.isNullOrBlank()) {
                            Glide.with(binding.image.context)
                                .load(R.drawable.placeholder_my_event)
                                .into(binding.image)
                        } else {
                            Glide.with(binding.image.context)
                                .load(conversation.imageUrl)
                                .error(R.drawable.placeholder_my_event) // Si l'URL est invalide, charger le placeholder
                                .into(binding.image)
                        }
                    }else{
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

            if (conversation.memberCount > 2) {
                var namesText = ""
                namesText = conversation.members?.get(0)?.displayName +", " + conversation.members?.get(1)?.displayName + "..."
                binding.name.text = namesText
            } else {
                binding.name.text = conversation.title
            }

            if(conversation.type == "outing"){
               binding.name.text = conversation.title
                binding.date.visibility = View.GONE
            }

            if (conversation.getRolesWithPartnerFormated()?.isEmpty() == false) {
                binding.role.isVisible = true
                binding.role.text = conversation.getRolesWithPartnerFormated()
            }
            else {
                binding.role.isVisible = false
            }

            binding.date.text = conversation.dateFormattedString(binding.root.context)
            binding.detail.text = conversation.getLastMessage()

            if (conversation.hasUnread()) {
                binding.nbUnread.visibility = View.VISIBLE
                binding.nbUnread.text = "${conversation.numberUnreadMessages}"
                binding.date.setTextColor(binding.root.context.resources.getColor(R.color.orange))
                binding.detail.setTextColor(binding.root.context.resources.getColor(R.color.black))
                binding.detail.setTypeface(binding.detail.typeface,Typeface.BOLD)
            }
            else {
                binding.nbUnread.visibility = View.INVISIBLE
                binding.date.setTextColor(binding.root.context.resources.getColor(R.color.dark_grey_opacity_40))
                binding.detail.setTextColor(binding.root.context.resources.getColor(R.color.dark_grey_opacity_40))
                binding.detail.setTypeface(binding.detail.typeface,Typeface.NORMAL)
            }

            if (conversation.imBlocker()) {
                binding.detail.text = binding.detail.resources.getText(R.string.message_user_blocked_by_me_list)
                binding.detail.setTextColor(binding.detail.resources.getColor(R.color.red))
            }
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
