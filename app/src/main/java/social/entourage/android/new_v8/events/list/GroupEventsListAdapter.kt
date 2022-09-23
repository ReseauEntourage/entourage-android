package social.entourage.android.new_v8.events.list

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter
import social.entourage.android.R
import social.entourage.android.databinding.NewEventItemBinding
import social.entourage.android.databinding.NewEventsListHeaderBinding
import social.entourage.android.new_v8.events.details.feed.FeedActivity
import social.entourage.android.new_v8.models.Events
import social.entourage.android.new_v8.models.Status
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.px
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class GroupEventsListAdapter(
    context: Context,
    var sectionItemList: List<SectionHeader?>?,
    var userId: Int?
) :
    SectionRecyclerViewAdapter<SectionHeader, Events, GroupEventsListAdapter.SectionViewHolder, GroupEventsListAdapter.ChildViewHolder>(
        context,
        sectionItemList
    ) {

    inner class SectionViewHolder(val binding: NewEventsListHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ChildViewHolder(val binding: NewEventItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    var context: Context

    override fun onCreateSectionViewHolder(
        sectionViewGroup: ViewGroup,
        viewType: Int
    ): SectionViewHolder {
        val binding = NewEventsListHeaderBinding.inflate(
            LayoutInflater.from(sectionViewGroup.context),
            sectionViewGroup,
            false
        )
        return SectionViewHolder(binding)
    }

    override fun onCreateChildViewHolder(
        childViewGroup: ViewGroup,
        viewType: Int
    ): ChildViewHolder {
        val binding = NewEventItemBinding.inflate(
            LayoutInflater.from(childViewGroup.context),
            childViewGroup,
            false
        )
        return ChildViewHolder(binding)
    }


    override fun onBindSectionViewHolder(
        sectionViewHolder: SectionViewHolder,
        sectionPosition: Int,
        section: SectionHeader
    ) {
        sectionViewHolder.binding.month.text = section.sectionText
    }

    override fun onBindChildViewHolder(
        childViewHolder: ChildViewHolder,
        sectionPosition: Int,
        childPosition: Int,
        child: Events
    ) {
        childViewHolder.binding.layout.setOnClickListener {
            context.startActivity(
                Intent(
                    context,
                    FeedActivity::class.java
                ).putExtra(
                    Const.EVENT_ID,
                    child.id
                )
            )
        }
        childViewHolder.binding.eventName.text = child.title
        child.metadata?.startsAt?.let {
            childViewHolder.binding.date.text = SimpleDateFormat(
                childViewHolder.itemView.context.getString(R.string.event_date_time),
                Locale.FRANCE
            ).format(
                it
            )
        }
        childViewHolder.binding.location.text = child.metadata?.displayAddress
        childViewHolder.binding.participants.text = child.membersCount.toString()

        val participantsCount = child.membersCount ?: 0

        childViewHolder.binding.participants.text =
            context.resources.getQuantityString(
                R.plurals.number_of_people,
                participantsCount,
                participantsCount
            )

        child.metadata?.landscapeThumbnailUrl?.let {
            Glide.with(context)
                .load(Uri.parse(child.metadata?.landscapeThumbnailUrl))
                .placeholder(R.drawable.ic_event_placeholder)
                .error(R.drawable.ic_event_placeholder)
                .apply(RequestOptions().override(90.px, 90.px))
                .transform(CenterCrop(), RoundedCorners(20.px))
                .into(childViewHolder.binding.image)
        } ?: run {
            Glide.with(context)
                .load(R.drawable.ic_event_placeholder)
                .apply(RequestOptions().override(90.px, 90.px))
                .transform(CenterCrop(), RoundedCorners(20.px))
                .into(childViewHolder.binding.image)
        }

        childViewHolder.binding.star.isVisible = child.author?.userID == userId
        childViewHolder.binding.admin.isVisible = child.author?.userID == userId

        childViewHolder.binding.canceled.isVisible = child.status == Status.CLOSED
        childViewHolder.binding.ivCanceled.isVisible = child.status == Status.CLOSED
        childViewHolder.binding.eventName.setTextColor(context.getColor(if (child.status == Status.CLOSED) R.color.grey else R.color.black))

/*
        childViewHolder.binding.divider.isVisible =
            sectionItemList?.get(sectionPosition)?.childList?.size?.minus(1) != childPosition

 */
    }

    init {
        this.context = context
    }
}
