package social.entourage.android.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import social.entourage.android.api.model.Conversation
import social.entourage.android.databinding.ItemHomeSmallTalkConversationBinding
import social.entourage.android.databinding.ItemHomeSmallTalkMatchBinding
import social.entourage.android.databinding.ItemHomeSmallTalkWaitingBinding
import timber.log.Timber

sealed class HomeSmallTalkItem {
    object MatchPossible : HomeSmallTalkItem()
    object Waiting : HomeSmallTalkItem()
    data class ConversationItem(val conversation: Conversation) : HomeSmallTalkItem()
}

class HomeSmallTalkAdapter(
    private val onStartClick: () -> Unit,
    private val onConversationClick: (Conversation) -> Unit,
    private val onMatchingClick: () -> Unit
) : ListAdapter<HomeSmallTalkItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_MATCH = 0
        private const val TYPE_WAITING = 1
        private const val TYPE_CONVERSATION = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HomeSmallTalkItem.MatchPossible -> TYPE_MATCH
            is HomeSmallTalkItem.Waiting -> TYPE_WAITING
            is HomeSmallTalkItem.ConversationItem -> TYPE_CONVERSATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_MATCH -> {
                val binding = ItemHomeSmallTalkMatchBinding.inflate(inflater, parent, false)
                MatchViewHolder(binding)
            }
            TYPE_WAITING -> {
                val binding = ItemHomeSmallTalkWaitingBinding.inflate(inflater, parent, false)
                WaitingViewHolder(binding)
            }
            TYPE_CONVERSATION -> {
                val binding = ItemHomeSmallTalkConversationBinding.inflate(inflater, parent, false)
                ConversationViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HomeSmallTalkItem.MatchPossible -> (holder as MatchViewHolder).bind()
            is HomeSmallTalkItem.Waiting -> (holder as WaitingViewHolder).bind()
            is HomeSmallTalkItem.ConversationItem -> (holder as ConversationViewHolder).bind(item.conversation)
        }
    }

    inner class MatchViewHolder(private val binding: ItemHomeSmallTalkMatchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.buttonStart.setOnClickListener {
                onStartClick()
            }
        }
    }

    inner class WaitingViewHolder(private val binding: ItemHomeSmallTalkWaitingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.layoutItem.setOnClickListener {
                onMatchingClick()
            }
        }
    }

    inner class ConversationViewHolder(private val binding: ItemHomeSmallTalkConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            Timber.wtf("wtf conversation:  " + Gson().toJson(conversation))
            val members = conversation.members?.take(3) ?: listOf()
            val avatars = listOf(binding.ivHomeSmallTalkAvatar1, binding.ivHomeSmallTalkAvatar2, binding.ivHomeSmallTalkAvatar3)

            members.forEachIndexed { index, member ->
                avatars.getOrNull(index)?.let { imageView ->
                    Glide.with(imageView.context)
                        .load(member.avatarUrl)
                        .placeholder(social.entourage.android.R.drawable.placeholder_user)
                        .circleCrop()
                        .into(imageView)
                }
            }

            binding.tvHomeSmallTalkNames.text = members.joinToString(", ") { it.displayName ?: "" }

            binding.root.setOnClickListener {
                onConversationClick(conversation)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HomeSmallTalkItem>() {
        override fun areItemsTheSame(oldItem: HomeSmallTalkItem, newItem: HomeSmallTalkItem): Boolean {
            return when {
                oldItem is HomeSmallTalkItem.ConversationItem && newItem is HomeSmallTalkItem.ConversationItem ->
                    oldItem.conversation.id == newItem.conversation.id
                oldItem::class == newItem::class -> true
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: HomeSmallTalkItem, newItem: HomeSmallTalkItem): Boolean {
            return oldItem == newItem
        }
    }
}
