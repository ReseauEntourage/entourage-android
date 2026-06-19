package social.entourage.android.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import social.entourage.android.R
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.GroupMember
import social.entourage.android.databinding.HomeV2EventItemLayoutBinding
import social.entourage.android.events.EventsFragment
import social.entourage.android.events.details.feed.EventFeedActivity
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import java.text.SimpleDateFormat

class HomeEventAdapter(
    var context: Context
) : RecyclerView.Adapter<HomeEventAdapter.EventViewHolder>() {

    var events: MutableList<Events> = mutableListOf()

    fun addEvents(listEvents: List<Events>) {
        events.addAll(listEvents)
        notifyDataSetChanged()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = HomeV2EventItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun getItemCount(): Int = events.size

    override fun getItemViewType(position: Int): Int = 20

    fun getEventIds(): Set<Int> = events.mapNotNull { it.id }.toSet()

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        val isArabic = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            holder.binding.root.resources.configuration.locales[0].language == "ar"
        } else {
            holder.binding.root.resources.configuration.locale.language == "ar"
        }

        if (isArabic) {
            holder.binding.tvTitleEventItem.layoutDirection = View.LAYOUT_DIRECTION_RTL
            holder.binding.tvTitleEventItem.gravity = Gravity.END
            holder.binding.tvTitleEventItem.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.binding.tvTitleEventItem.textDirection = View.TEXT_DIRECTION_RTL

            holder.binding.tvPlaceHomeV2EventItem.layoutDirection = View.LAYOUT_DIRECTION_RTL
            holder.binding.tvPlaceHomeV2EventItem.gravity = Gravity.END
            holder.binding.tvPlaceHomeV2EventItem.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.binding.tvPlaceHomeV2EventItem.textDirection = View.TEXT_DIRECTION_RTL

            holder.binding.tvDateHomeV2EventItem.layoutDirection = View.LAYOUT_DIRECTION_RTL
            holder.binding.tvDateHomeV2EventItem.gravity = Gravity.END
            holder.binding.tvDateHomeV2EventItem.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.binding.tvDateHomeV2EventItem.textDirection = View.TEXT_DIRECTION_RTL
        } else {
            holder.binding.tvTitleEventItem.layoutDirection = View.LAYOUT_DIRECTION_LTR
            holder.binding.tvTitleEventItem.gravity = Gravity.START
            holder.binding.tvTitleEventItem.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.binding.tvTitleEventItem.textDirection = View.TEXT_DIRECTION_LTR

            holder.binding.tvPlaceHomeV2EventItem.layoutDirection = View.LAYOUT_DIRECTION_LTR
            holder.binding.tvPlaceHomeV2EventItem.gravity = Gravity.START
            holder.binding.tvPlaceHomeV2EventItem.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.binding.tvPlaceHomeV2EventItem.textDirection = View.TEXT_DIRECTION_LTR

            holder.binding.tvDateHomeV2EventItem.layoutDirection = View.LAYOUT_DIRECTION_LTR
            holder.binding.tvDateHomeV2EventItem.gravity = Gravity.START
            holder.binding.tvDateHomeV2EventItem.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.binding.tvDateHomeV2EventItem.textDirection = View.TEXT_DIRECTION_LTR
        }

        holder.binding.layoutItemHomeEvent.setOnClickListener { view ->
            EventsFragment.isFromDetails = true
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Event_Detail)
            EventFeedActivity.isFromMyEvent = true
            (view.context as? Activity)?.startActivityForResult(
                Intent(view.context, EventFeedActivity::class.java)
                    .putExtra(Const.EVENT_ID, event.id), 0
            )
        }

        event.metadata?.landscapeUrl?.let {
            Glide.with(holder.binding.root.context)
                .load(Uri.parse(it))
                .placeholder(R.drawable.ic_event_placeholder)
                .transform(CenterCrop())
                .error(R.drawable.ic_event_placeholder)
                .into(holder.binding.ivEventItem)
        } ?: run {
            Glide.with(holder.binding.root.context)
                .load(R.drawable.ic_event_placeholder)
                .transform(CenterCrop())
                .into(holder.binding.ivEventItem)
        }

        holder.binding.tvTitleEventItem.text = event.title

        event.metadata?.displayAddress?.let {
            holder.binding.tvPlaceHomeV2EventItem.text = it.split(",").lastOrNull()?.trim() ?: it
        }

        event.metadata?.startsAt?.let {
            holder.binding.tvDateHomeV2EventItem.text = Utils.formatEventDateWithTime(it, context)
        }

        // Urgency: show if fewer than 5 places remaining
        val placeLimit = event.metadata?.placeLimit
        val membersCount = event.membersCount ?: 0
        if (placeLimit != null) {
            val remaining = placeLimit - membersCount
            if (remaining in 1..5) {
                holder.binding.tvUrgencyHomeEvent.text =
                    if (remaining == 1) "Plus qu'une place" else "Plus que $remaining places"
                holder.binding.tvUrgencyHomeEvent.visibility = View.VISIBLE
            } else {
                holder.binding.tvUrgencyHomeEvent.visibility = View.GONE
            }
        } else {
            holder.binding.tvUrgencyHomeEvent.visibility = View.GONE
        }

        val isReservedFemale = event.metadata?.reserved_female == true
        val isEntourageEvent = event.author?.communityRoles?.let {
            it.contains("Équipe Entourage") || it.contains("Animateur Entourage")
        } == true

        holder.binding.tvTagFemaleHome.visibility = if (isReservedFemale) View.VISIBLE else View.GONE
        holder.binding.tvTagEntourageHome.visibility =
            if (!isReservedFemale && isEntourageEvent) View.VISIBLE else View.GONE

        bindParticipants(event, holder.binding)
    }

    private fun bindParticipants(event: Events, binding: HomeV2EventItemLayoutBinding) {
        val members = event.members ?: emptyList()
        val totalCount = event.membersCount ?: 0

        val avatarViews = listOf(binding.ivMember1Home, binding.ivMember2Home, binding.ivMember3Home)
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

        binding.tvParticipantsCountHome.text = binding.root.context.resources.getQuantityString(
            R.plurals.number_of_people, totalCount, totalCount
        )
    }

    class EventViewHolder(val binding: HomeV2EventItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}
