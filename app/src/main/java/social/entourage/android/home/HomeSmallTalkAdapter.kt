package social.entourage.android.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.api.model.Conversation

sealed class HomeSmallTalkItem {
    object MatchPossible : HomeSmallTalkItem()
    object Waiting : HomeSmallTalkItem()
    data class ConversationItem(val conversation: Conversation) : HomeSmallTalkItem()
}

class HomeSmallTalkAdapter(
    private val onStartClick: () -> Unit,
    private val onConversationClick: (Conversation) -> Unit
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
        return when (viewType) {
            TYPE_MATCH -> MatchViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_home_small_talk_match, parent, false)
            )
            TYPE_WAITING -> WaitingViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_home_small_talk_waiting, parent, false)
            )
            TYPE_CONVERSATION -> ConversationViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_home_small_talk_conversation, parent, false)
            )
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

    inner class MatchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            itemView.findViewById<Button>(R.id.button_start).setOnClickListener {
                onStartClick()
            }
        }
    }

    inner class WaitingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            // No action needed for now
        }
    }

    inner class ConversationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val avatar1: ImageView = view.findViewById(R.id.iv_home_small_talk_avatar_1)
        private val avatar2: ImageView = view.findViewById(R.id.iv_home_small_talk_avatar_2)
        private val avatar3: ImageView = view.findViewById(R.id.iv_home_small_talk_avatar_3)
        private val names: TextView = view.findViewById(R.id.tv_home_small_talk_names)

        fun bind(conversation: Conversation) {
            val members = conversation.members?.take(3) ?: listOf()
            val avatars = listOf(avatar1, avatar2, avatar3)

            members.forEachIndexed { index, member ->
                avatars.getOrNull(index)?.let { imageView ->
                    Glide.with(imageView.context)
                        .load(member.avatarUrl)
                        .placeholder(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(imageView)
                }
            }

            val displayNames = members.joinToString(", ") { it.displayName ?: "" }
            names.text = displayNames

            itemView.setOnClickListener {
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
