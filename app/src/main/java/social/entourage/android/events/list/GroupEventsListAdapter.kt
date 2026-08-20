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
import social.entourage.android.databinding.NewEventsListHeaderBinding
import social.entourage.android.events.details.feed.EventFeedActivity
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.calculateIfEventPassed
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class GroupEventsListAdapter(
    private val context: Context,
    var sectionItemList: List<SectionHeader?>?,
    var userId: Int?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private sealed class Row {
        data class Section(val header: SectionHeader) : Row()
        data class Child(val item: Events) : Row()
    }

    companion object {
        private const val TYPE_SECTION = 0
        private const val TYPE_CHILD = 1
    }

    inner class SectionViewHolder(val binding: NewEventsListHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ChildViewHolder(val binding: NewEventItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var rows: List<Row> = buildRows(sectionItemList)

    private val eventDateFormatter: SimpleDateFormat by lazy {
        val locale = LanguageManager.getLocaleFromPreferences(context)
        SimpleDateFormat(context.getString(R.string.event_date_time), locale)
    }

    private fun buildRows(sections: List<SectionHeader?>?): List<Row> {
        val result = mutableListOf<Row>()
        sections?.forEach { section ->
            if (section != null) {
                result.add(Row.Section(section))
                section.childList.forEach { child -> result.add(Row.Child(child)) }
            }
        }
        return result
    }

    fun notifyDataChanged(newSections: List<SectionHeader?>?) {
        sectionItemList = newSections
        rows = buildRows(newSections)
        notifyDataSetChanged()
    }

    fun resetList() {}

    override fun getItemViewType(position: Int) = when (rows[position]) {
        is Row.Section -> TYPE_SECTION
        is Row.Child -> TYPE_CHILD
    }

    override fun getItemCount() = rows.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SECTION -> SectionViewHolder(NewEventsListHeaderBinding.inflate(inflater, parent, false))
            else -> ChildViewHolder(NewEventItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is Row.Section -> bindSection(holder as SectionViewHolder, row.header)
            is Row.Child -> bindChild(holder as ChildViewHolder, row.item)
        }
    }

    private fun bindSection(sectionViewHolder: SectionViewHolder, section: SectionHeader) {
        sectionViewHolder.binding.month.text = section.sectionText
    }

    private fun bindChild(childViewHolder: ChildViewHolder, child: Events) {
        childViewHolder.binding.layout.setOnClickListener { view ->
            (view.context as? Activity)?.startActivityForResult(
                Intent(view.context, EventFeedActivity::class.java)
                    .putExtra(Const.EVENT_ID, child.id), 0
            )
        }

        childViewHolder.binding.eventName.text = child.title

        child.metadata?.startsAt?.let { startsAt ->
            val dateStr = Utils.formatEventDateWithTime(startsAt, context)
            val durationStr = child.metadata.endsAt?.let { endsAt ->
                val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(endsAt.time - startsAt.time)
                formatDuration(diffMinutes)
            }
            childViewHolder.binding.date.text = if (durationStr != null) "$dateStr • $durationStr" else dateStr
        }

        childViewHolder.binding.location.text = child.metadata?.displayAddress

        val isReservedFemale = child.metadata?.reserved_female == true
        val isEntourageEvent = child.author?.communityRoles?.let {
            it.contains("Équipe Entourage") || it.contains("Animateur Entourage")
        } == true

        childViewHolder.binding.tvTagFemale.visibility = if (isReservedFemale) View.VISIBLE else View.GONE
        childViewHolder.binding.tvTagEntourage.visibility =
            if (!isReservedFemale && isEntourageEvent) View.VISIBLE else View.GONE

        childViewHolder.binding.tvSubscribed.isVisible = child.member

        bindParticipants(child, childViewHolder.binding)

        child.metadata?.landscapeUrl?.let {
            Glide.with(context)
                .load(Uri.parse(it))
                .placeholder(R.drawable.ic_event_placeholder)
                .error(R.drawable.ic_event_placeholder)
                .transform(CenterCrop())
                .into(childViewHolder.binding.image)
        } ?: run {
            Glide.with(context)
                .load(R.drawable.ic_event_placeholder)
                .transform(CenterCrop())
                .into(childViewHolder.binding.image)
        }

        childViewHolder.binding.star.isVisible = child.author?.userID == userId
        childViewHolder.binding.admin.isVisible = child.author?.userID == userId
        childViewHolder.binding.canceled.isVisible = child.status == Status.CLOSED
        childViewHolder.binding.ivCanceled.isVisible = child.status == Status.CLOSED

        if (child.calculateIfEventPassed()) {
            childViewHolder.binding.eventName.setTextColor(ContextCompat.getColor(context, R.color.grey))
            childViewHolder.binding.blackLayout.visibility = View.VISIBLE
        } else {
            childViewHolder.binding.eventName.setTextColor(
                ContextCompat.getColor(context, if (child.status == Status.CLOSED) R.color.grey else R.color.black)
            )
            childViewHolder.binding.blackLayout.visibility = View.GONE
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
}
