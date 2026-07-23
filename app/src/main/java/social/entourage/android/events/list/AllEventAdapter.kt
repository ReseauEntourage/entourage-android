package social.entourage.android.events.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import social.entourage.android.R

import social.entourage.android.api.model.Events
import social.entourage.android.api.model.GroupMember
import social.entourage.android.api.model.Status
import social.entourage.android.databinding.NewEventItemBinding
import social.entourage.android.events.EventsFragment
import social.entourage.android.events.details.feed.EventFeedActivity
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.calculateIfEventPassed
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import java.util.concurrent.TimeUnit

class AllEventAdapter(var userId: Int?, var context: Context) :
    RecyclerView.Adapter<AllEventAdapter.EventViewHolder>() {

    private val TYPE_EVENT = 1
    var events: MutableList<Events> = mutableListOf()

    override fun getItemCount(): Int {
        return events.size
    }

    fun resetData(events: MutableList<Events>) {
        this.events.clear()
        this.events.addAll(events)
        notifyDataSetChanged()
    }

    fun clearList() {
        this.events.clear()
        notifyDataSetChanged()
    }

    fun addData(newEvents: List<Events>) {
        val startPosition = events.size
        events.addAll(newEvents)
        notifyItemRangeInserted(startPosition, newEvents.size)
    }

    override fun getItemViewType(position: Int): Int {
        return TYPE_EVENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = NewEventItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        if (position < events.size) {
            val event = events[position]
            holder.binding.layout.setOnClickListener { view ->
                EventsFragment.isFromDetails = true
                EventFeedActivity.isFromMyEvent = false
                (view.context as? Activity)?.startActivityForResult(
                    Intent(
                        view.context,
                        EventFeedActivity::class.java
                    ).putExtra(
                        Const.EVENT_ID,
                        event.id
                    ), 0
                )
            }
            holder.binding.eventName.text = event.title

            event.metadata?.startsAt?.let { startsAt ->
                val dateStr = Utils.formatEventDateWithTime(startsAt, context)
                val durationStr = event.metadata.endsAt?.let { endsAt ->
                    val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(endsAt.time - startsAt.time)
                    formatDuration(diffMinutes)
                }
                holder.binding.date.text = if (durationStr != null) "$dateStr • $durationStr" else dateStr
            }

            holder.binding.location.text = event.metadata?.displayAddress

            val isReservedFemale = event.metadata?.reserved_female == true
            val isEntourageEvent = event.author?.communityRoles?.let {
                it.contains("Équipe Entourage") || it.contains("Animateur Entourage")
            } == true

            holder.binding.tvTagFemale.visibility = if (isReservedFemale) View.VISIBLE else View.GONE
            holder.binding.tvTagEntourage.visibility =
                if (!isReservedFemale && isEntourageEvent) View.VISIBLE else View.GONE

            holder.binding.tvSubscribed.isVisible = event.member

            bindParticipants(event, holder.binding)

            event.metadata?.landscapeUrl?.let {
                Glide.with(holder.binding.root.context)
                    .load(Uri.parse(it))
                    .placeholder(R.drawable.ic_event_placeholder)
                    .error(R.drawable.ic_event_placeholder)
                    .transform(CenterCrop())
                    .into(holder.binding.image)
            } ?: run {
                Glide.with(holder.binding.root.context)
                    .load(R.drawable.ic_event_placeholder)
                    .transform(CenterCrop())
                    .into(holder.binding.image)
            }

            holder.binding.star.isVisible = event.author?.userID == userId
            holder.binding.admin.isVisible = event.author?.userID == userId
            holder.binding.canceled.isVisible = event.status == Status.CLOSED
            holder.binding.ivCanceled.isVisible = event.status == Status.CLOSED

            if (event.calculateIfEventPassed()) {
                holder.binding.eventName.setTextColor(ContextCompat.getColor(holder.binding.root.context, R.color.grey))
                holder.binding.blackLayout.visibility = View.VISIBLE
            } else {
                holder.binding.eventName.setTextColor(
                    ContextCompat.getColor(
                        holder.binding.root.context,
                        if (event.status == Status.CLOSED) R.color.grey else R.color.black
                    )
                )
                holder.binding.blackLayout.visibility = View.GONE
            }
        }
    }

    private fun bindParticipants(event: Events, binding: NewEventItemBinding) {
        val members = event.members ?: emptyList()
        val totalCount = event.membersCount ?: 0

        if (totalCount == 0) {
            binding.layoutParticipantsRow.visibility = View.GONE
            return
        }

        binding.layoutParticipantsRow.visibility = View.VISIBLE

        val avatarViews = listOf(binding.ivMember1, binding.ivMember2, binding.ivMember3)
        for (i in avatarViews.indices) {
            val member = members.getOrNull(i)
            if (member != null) {
                avatarViews[i].visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(member.avatarUrl)
                    .placeholder(R.drawable.placeholder_user)
                    .error(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(avatarViews[i])
            } else {
                avatarViews[i].visibility = View.GONE
            }
        }

        binding.participants.text = buildParticipantText(members, totalCount)
    }

    private fun buildParticipantText(members: List<GroupMember>, totalCount: Int): String {
        val names = members.take(2).mapNotNull { it.displayName?.split(" ")?.firstOrNull() }
        val otherCount = (totalCount - names.size).coerceAtLeast(0)
        return when {
            names.isEmpty() -> context.resources.getQuantityString(R.plurals.number_of_people, totalCount, totalCount)
            names.size == 1 && otherCount == 0 -> "${names[0]} y va"
            names.size == 1 -> "${names[0]} et $otherCount autre${if (otherCount > 1) "s" else ""} y ${if (otherCount > 1) "vont" else "va"}"
            otherCount == 0 -> "${names[0]}, ${names[1]} y vont"
            else -> "${names[0]}, ${names[1]} et $otherCount autre${if (otherCount > 1) "s" else ""} y vont"
        }
    }

    private fun formatDuration(minutes: Long): String {
        return when {
            minutes <= 0 -> ""
            minutes < 60 -> "${minutes} min"
            minutes % 60 == 0L -> "${minutes / 60}h"
            else -> "${minutes / 60}h${minutes % 60}"
        }
    }

    class EventViewHolder(val binding: NewEventItemBinding) : RecyclerView.ViewHolder(binding.root)
}
