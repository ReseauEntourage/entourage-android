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
import kotlinx.android.synthetic.main.new_conversation_home_item.view.*
import social.entourage.android.R
import social.entourage.android.api.model.Conversation
import timber.log.Timber

interface OnItemClick {
    fun onItemClick(position: Int)
}

class DiscussionsListAdapter(
    var messagesList: List<Conversation>,
    private var onItemClickListener: OnItemClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.new_conversation_home_item, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(val binding: View) :
        RecyclerView.ViewHolder(binding) {
        fun bind(conversation: Conversation, position: Int) {

            binding.layout.setOnClickListener {
                onItemClickListener.onItemClick(position)
            }
            if (conversation.isOneToOne()) {
                binding.image_picto.isVisible = false
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
                binding.image_picto.isVisible = true
                Glide.with(binding.image.context)
                    .load(R.drawable.new_circle_fill_beige)
                    .transform(CenterCrop(), CircleCrop())
                    .into(binding.image)
                binding.image_picto.setImageResource(conversation.getPictoTypeFromSection())
            }

            binding.name.text = conversation.title
            if (conversation.getRolesWithPartnerFormated()?.isEmpty() == false) {
                binding.role.isVisible = true
                binding.role.text = conversation.getRolesWithPartnerFormated()
            }
            else {
                binding.role.isVisible = false
            }

            binding.date.text = conversation.dateFormattedString(binding.context)
            binding.detail.text = conversation.getLastMessage()

            if (conversation.hasUnread()) {
                binding.nb_unread.visibility = View.VISIBLE
                binding.nb_unread.text = "${conversation.numberUnreadMessages}"
                binding.date.setTextColor(binding.context.resources.getColor(R.color.orange))
                binding.detail.setTextColor(binding.context.resources.getColor(R.color.black))
                binding.detail.setTypeface(binding.detail.typeface,Typeface.BOLD)
            }
            else {
                binding.nb_unread.visibility = View.INVISIBLE
                binding.date.setTextColor(binding.context.resources.getColor(R.color.dark_grey_opacity_40))
                binding.detail.setTextColor(binding.context.resources.getColor(R.color.dark_grey_opacity_40))
                binding.detail.setTypeface(binding.detail.typeface,Typeface.NORMAL)
            }

            if (conversation.imBlocker()) {
                binding.detail.text = binding.detail.resources.getText(R.string.message_user_blocked_by_me_list)
                binding.detail.setTextColor(binding.detail.resources.getColor(R.color.red))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? ViewHolder)?.bind(messagesList[position], position)
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }
}
