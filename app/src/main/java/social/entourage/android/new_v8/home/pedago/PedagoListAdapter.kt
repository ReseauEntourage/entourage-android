package social.entourage.android.new_v8.home.pedago

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
import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter
import social.entourage.android.R
import social.entourage.android.databinding.NewPedagoContentItemBinding
import social.entourage.android.databinding.NewPedagoSectionHeaderBinding
import social.entourage.android.new_v8.models.Pedago
import social.entourage.android.new_v8.utils.px

interface OnItemClick {
    fun onItemClick(pedagogicalContent: Pedago)
}

class PedagoListAdapter(
    context: Context,
    var sectionItemList: List<SectionHeader?>?,
    private var onItemClickListener: OnItemClick
) :
    SectionRecyclerViewAdapter<SectionHeader, Pedago, PedagoListAdapter.SectionViewHolder, PedagoListAdapter.ChildViewHolder>(
        context,
        sectionItemList
    ) {

    inner class SectionViewHolder(val binding: NewPedagoSectionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ChildViewHolder(val binding: NewPedagoContentItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    var context: Context

    override fun onCreateSectionViewHolder(
        sectionViewGroup: ViewGroup,
        viewType: Int
    ): SectionViewHolder {
        val binding = NewPedagoSectionHeaderBinding.inflate(
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
        val binding = NewPedagoContentItemBinding.inflate(
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
        sectionViewHolder.binding.sectionName.text = section.sectionText
    }

    override fun onBindChildViewHolder(
        childViewHolder: ChildViewHolder,
        sectionPosition: Int,
        childPosition: Int,
        child: Pedago
    ) {
        childViewHolder.binding.title.text = child.name
        childViewHolder.binding.read.isVisible = child.watched == true
        childViewHolder.binding.root.setOnClickListener {
            onItemClickListener.onItemClick(child)
        }

        Glide.with(context)
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

    init {
        this.context = context
    }
}

