package social.entourage.android.home.pedago

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.databinding.NewPedagoContentItemBinding
import social.entourage.android.databinding.NewPedagoSectionHeaderBinding
import social.entourage.android.api.model.Pedago
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.px

interface OnItemClick {
    fun onItemClick(pedagogicalContent: Pedago)
}

class PedagoListAdapter(
    private val context: Context,
    var sectionItemList: List<SectionHeader?>?,
    private var onItemClickListener: OnItemClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private sealed class Row {
        data class Section(val header: SectionHeader) : Row()
        data class Child(val sectionPosition: Int, val childPosition: Int, val item: Pedago) : Row()
    }

    companion object {
        private const val TYPE_SECTION = 0
        private const val TYPE_CHILD = 1
    }

    inner class SectionViewHolder(val binding: NewPedagoSectionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ChildViewHolder(val binding: NewPedagoContentItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var rows: List<Row> = buildRows(sectionItemList)

    private fun buildRows(sections: List<SectionHeader?>?): List<Row> {
        val result = mutableListOf<Row>()
        sections?.forEachIndexed { sectionPosition, section ->
            if (section != null) {
                result.add(Row.Section(section))
                section.childList.forEachIndexed { childPosition, child ->
                    result.add(Row.Child(sectionPosition, childPosition, child))
                }
            }
        }
        return result
    }

    fun notifyDataChanged(newSections: List<SectionHeader?>?) {
        sectionItemList = newSections
        rows = buildRows(newSections)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = when (rows[position]) {
        is Row.Section -> TYPE_SECTION
        is Row.Child -> TYPE_CHILD
    }

    override fun getItemCount() = rows.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SECTION -> SectionViewHolder(NewPedagoSectionHeaderBinding.inflate(inflater, parent, false))
            else -> ChildViewHolder(NewPedagoContentItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is Row.Section -> bindSection(holder as SectionViewHolder, row.header)
            is Row.Child -> bindChild(holder as ChildViewHolder, row.sectionPosition, row.childPosition, row.item)
        }
    }

    private fun bindSection(sectionViewHolder: SectionViewHolder, section: SectionHeader) {
        sectionViewHolder.binding.sectionName.text = section.sectionText
    }

    private fun bindChild(
        childViewHolder: ChildViewHolder,
        sectionPosition: Int,
        childPosition: Int,
        child: Pedago
    ) {
        childViewHolder.binding.title.text = child.name
        childViewHolder.binding.read.isVisible = child.watched == true
        childViewHolder.binding.root.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Pedago_View_card)
            onItemClickListener.onItemClick(child)
        }

        Glide.with(childViewHolder.itemView)
            .load(child.imageUrl)
            .placeholder(R.drawable.new_illu_empty_state_event)
            .apply(RequestOptions().override(77.px, 46.px))
            .transform(RoundedCorners(5.px))
            .into(childViewHolder.binding.image)

        val background = AppCompatResources.getDrawable(
            context,
            if (child.watched == true) R.drawable.new_bg_rounded_button_light_orange_stroke else R.drawable.new_bg_rounded_beige_unread_pedago
        )
        childViewHolder.binding.rootLayout.background = background
        childViewHolder.binding.root.updatePadding(
            bottom =
            if (sectionItemList?.get(sectionPosition)?.childList?.size?.minus(1) == childPosition) 15.px else 0.px
        )
        val backgroundShadow = AppCompatResources.getDrawable(
            context,
            if (sectionItemList?.get(sectionPosition)?.childList?.size?.minus(1) == childPosition)
                R.drawable.new_bg_footer_pedagogical
            else R.drawable.new_bg_pedagogical_item
        )
        childViewHolder.binding.root.background = backgroundShadow
    }
}
