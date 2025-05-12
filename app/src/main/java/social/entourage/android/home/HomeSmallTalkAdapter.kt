package social.entourage.android.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import social.entourage.android.R
import social.entourage.android.api.model.UserSmallTalkRequest
import social.entourage.android.databinding.ItemHomeSmallTalkConversationBinding
import social.entourage.android.databinding.ItemHomeSmallTalkMatchBinding
import social.entourage.android.databinding.ItemHomeSmallTalkWaitingBinding
import timber.log.Timber

sealed class HomeSmallTalkItem {
    object MatchPossible : HomeSmallTalkItem()
    object Waiting : HomeSmallTalkItem()
    data class ConversationItem(val userSmallTalkRequest: UserSmallTalkRequest) : HomeSmallTalkItem()
}

class HomeSmallTalkAdapter(
    private val onStartClick: () -> Unit,
    private val onConversationClick: (UserSmallTalkRequest) -> Unit,
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
            TYPE_MATCH -> MatchViewHolder(ItemHomeSmallTalkMatchBinding.inflate(inflater, parent, false))
            TYPE_WAITING -> WaitingViewHolder(ItemHomeSmallTalkWaitingBinding.inflate(inflater, parent, false))
            TYPE_CONVERSATION -> ConversationViewHolder(ItemHomeSmallTalkConversationBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HomeSmallTalkItem.MatchPossible -> (holder as MatchViewHolder).bind()
            is HomeSmallTalkItem.Waiting -> (holder as WaitingViewHolder).bind()
            is HomeSmallTalkItem.ConversationItem -> (holder as ConversationViewHolder).bind(item.userSmallTalkRequest)
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

        fun bind(userSmallTalkRequest: UserSmallTalkRequest) {
            Timber.wtf("wtf userSmallTalkRequest : ${Gson().toJson(userSmallTalkRequest)}")

            val members = userSmallTalkRequest.smallTalk?.members?.take(3) ?: emptyList()

            val avatars = listOf(
                binding.ivHomeSmallTalkAvatar1,
                binding.ivHomeSmallTalkAvatar2,
                binding.ivHomeSmallTalkAvatar3
            )

            // Reset toutes les images avant de charger
            avatars.forEach { imageView ->
                Glide.with(imageView.context).clear(imageView)
                imageView.setImageResource(R.drawable.placeholder_user)
                imageView.visibility = View.GONE
            }

            // Charger les membres
            members.forEachIndexed { index, member ->
                avatars.getOrNull(index)?.let { imageView ->
                    imageView.visibility = View.VISIBLE
                    Glide.with(imageView.context)
                        .load(member.avatarUrl)
                        .placeholder(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(imageView)
                }
            }

            // Mettre les noms dans le TextView
            binding.tvHomeSmallTalkNames.text = when (members.size) {
                1 -> members[0].displayName.orEmpty()
                2 -> "${members[0].displayName.orEmpty()} et ${members[1].displayName.orEmpty()}"
                3 -> "${members[0].displayName.orEmpty()}, ${members[1].displayName.orEmpty()} et ${members[2].displayName.orEmpty()}"
                else -> ""
            }

            binding.root.setOnClickListener {
                onConversationClick(userSmallTalkRequest)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HomeSmallTalkItem>() {
        override fun areItemsTheSame(oldItem: HomeSmallTalkItem, newItem: HomeSmallTalkItem): Boolean {
            return when {
                oldItem is HomeSmallTalkItem.ConversationItem && newItem is HomeSmallTalkItem.ConversationItem ->
                    oldItem.userSmallTalkRequest.id == newItem.userSmallTalkRequest.id
                oldItem::class == newItem::class -> true
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: HomeSmallTalkItem, newItem: HomeSmallTalkItem): Boolean {
            return oldItem == newItem
        }
    }
}
