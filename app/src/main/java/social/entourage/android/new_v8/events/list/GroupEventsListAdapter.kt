package social.entourage.android.new_v8.events.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter
import social.entourage.android.databinding.NewEventItemBinding
import social.entourage.android.databinding.NewEventsListHeaderBinding
import social.entourage.android.new_v8.models.Events


class GroupEventsListAdapter(context: Context, var sectionItemList: List<SectionHeader?>?) :
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
        childViewHolder.binding.date.text = child.createdAt
        childViewHolder.binding.location.text = child.metadata?.displayAddress
        childViewHolder.binding.participants.text = child.membersCount.toString()
    }

    init {
        this.context = context
    }
}

