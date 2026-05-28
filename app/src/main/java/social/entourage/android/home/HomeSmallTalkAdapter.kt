package social.entourage.android.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.UserSmallTalkRequest
import social.entourage.android.databinding.ItemHomeSmallTalkActiveBinding
import social.entourage.android.databinding.ItemHomeSmallTalkMatchBinding
import social.entourage.android.databinding.ItemHomeSmallTalkWaitingBinding

sealed class HomeSmallTalkItem {
    object MatchPossible : HomeSmallTalkItem()
    object Waiting : HomeSmallTalkItem()
    data class Active(
        val activeRequests: List<UserSmallTalkRequest>,
        val waitingCount: Int,
        val totalCount: Int
    ) : HomeSmallTalkItem()
}

class HomeSmallTalkAdapter(
    private val onStartClick: () -> Unit,
    private val onViewClick: () -> Unit,
    private val onMatchingClick: () -> Unit,
    private val onLaunchNewClick: () -> Unit,
    private val context: Context
) : ListAdapter<HomeSmallTalkItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private val TYPE_MATCH = R.layout.item_home_small_talk_match
        private val TYPE_WAITING = R.layout.item_home_small_talk_waiting
        private val TYPE_ACTIVE = R.layout.item_home_small_talk_active
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HomeSmallTalkItem.MatchPossible -> TYPE_MATCH
            is HomeSmallTalkItem.Waiting -> TYPE_WAITING
            is HomeSmallTalkItem.Active -> TYPE_ACTIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_MATCH -> MatchViewHolder(ItemHomeSmallTalkMatchBinding.inflate(inflater, parent, false))
            TYPE_WAITING -> WaitingViewHolder(ItemHomeSmallTalkWaitingBinding.inflate(inflater, parent, false))
            TYPE_ACTIVE -> ActiveViewHolder(ItemHomeSmallTalkActiveBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HomeSmallTalkItem.MatchPossible -> (holder as MatchViewHolder).bind()
            is HomeSmallTalkItem.Waiting -> (holder as WaitingViewHolder).bind()
            is HomeSmallTalkItem.Active -> (holder as ActiveViewHolder).bind(item)
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.layoutParams = holder.itemView.layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
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
            // we don't have the count here easily, but we can assume it's just 1 if we are in this state.
            // Or we could pass it down, but let's just keep the generic string without %d or pass 1.
            binding.tvHomeSmallTalkSubtitleWaiting.text = context.getString(R.string.home_small_talk_subtitle_waiting, 1)
            binding.layoutItem.setOnClickListener {
                onMatchingClick()
            }
        }
    }

    inner class ActiveViewHolder(private val binding: ItemHomeSmallTalkActiveBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeSmallTalkItem.Active) {
            val currentUserId = EntourageApplication.me(context)?.id

            // Prepare avatars
            val avatars = listOf(
                binding.ivHomeSmallTalkAvatar1,
                binding.ivHomeSmallTalkAvatar2
            )

            // Reset images
            avatars.forEach { imageView ->
                Glide.with(imageView.context).clear(imageView)
                imageView.setImageResource(R.drawable.placeholder_user)
                imageView.visibility = View.GONE
            }

            // Extract other members and sort them to prioritize those with an avatar
            val otherMembers = item.activeRequests.mapNotNull { request ->
                request.smallTalk?.members?.firstOrNull { it.id != currentUserId }
            }.sortedByDescending { !it.avatarUrl.isNullOrBlank() }

            // Display up to 2 avatars
            for (i in 0 until minOf(avatars.size, otherMembers.size)) {
                val member = otherMembers[i]
                val imageView = avatars[i]
                imageView.visibility = View.VISIBLE
                Glide.with(imageView.context)
                    .load(member.avatarUrl)
                    .placeholder(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(imageView)
            }

            // Calculate unread messages
            var totalUnread = 0
            for (request in item.activeRequests) {
                totalUnread += request.numberOfUnreadMessages ?: 0
            }

            // Unread messages badge
            if (totalUnread > 0) {
                binding.cardNewsGroup.visibility = View.VISIBLE
                binding.tvNewsGroup.text = totalUnread.toString()
            } else {
                binding.cardNewsGroup.visibility = View.GONE
            }

            // Subtitle
            val activeStr = if (item.activeRequests.size > 1) {
                context.getString(R.string.home_subtitle_small_talk_active, item.activeRequests.size)
            } else {
                context.getString(R.string.home_subtitle_small_talk_active_single, item.activeRequests.size)
            }
            binding.tvHomeSmallTalkSubtitleActive.text = activeStr

            // Matching text
            if (item.waitingCount > 0) {
                binding.tvHomeSmallTalkMatching.visibility = View.VISIBLE
                val matchStr = context.getString(R.string.home_small_talk_matching, item.waitingCount)
                binding.tvHomeSmallTalkMatching.text = matchStr
            } else {
                binding.tvHomeSmallTalkMatching.visibility = View.GONE
            }

            // Launch new button
            if (item.totalCount < 3 && item.waitingCount == 0) {
                binding.tvHomeSmallTalkLaunchNew.visibility = View.VISIBLE
                binding.tvHomeSmallTalkLaunchNew.text = context.getString(R.string.home_small_talk_launch_new, item.totalCount)
                binding.tvHomeSmallTalkLaunchNew.setOnClickListener {
                    onLaunchNewClick()
                }
            } else {
                binding.tvHomeSmallTalkLaunchNew.visibility = View.GONE
            }

            binding.buttonVoir.setOnClickListener {
                onViewClick()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HomeSmallTalkItem>() {
        override fun areItemsTheSame(oldItem: HomeSmallTalkItem, newItem: HomeSmallTalkItem): Boolean {
            return oldItem::class == newItem::class
        }

        override fun areContentsTheSame(oldItem: HomeSmallTalkItem, newItem: HomeSmallTalkItem): Boolean {
            return oldItem == newItem
        }
    }
}
