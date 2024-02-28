package social.entourage.android.guide.filter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.LayoutFilterItemBinding
import social.entourage.android.databinding.LayoutFilterTopBinding

/**
 * Created by Jr (MJ-DEVS) on 12/10/2020.
 */
class GuideFilterRVAdapter(val context: Context, private var myDataset: ArrayList<GuideFilterItemAdapter.GuideFilterItem>,
                           var isPartnerSelected:Boolean,
                           var isDonatedSelected:Boolean,
                           var isVolunteerSelected:Boolean,
                           var isAllActive:Boolean,
                           val listener:(position:Int) -> Unit,
                           val listenerTop:(position:Int) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun updateData(items: ArrayList<GuideFilterItemAdapter.GuideFilterItem>, isPartnerSelected:Boolean,
                   isDonatedSelected:Boolean,
                   isVolunteerSelected:Boolean, isAllActive:Boolean) {
        this.myDataset = items
        this.isDonatedSelected = isDonatedSelected
        this.isVolunteerSelected = isVolunteerSelected
        this.isPartnerSelected = isPartnerSelected
        this.isAllActive = isAllActive

        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return VIEW_TOP
        }
        return VIEW_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        if (viewType == VIEW_TOP) {
            val v = LayoutFilterTopBinding.inflate(inflater, parent, false)
            return ViewTopVH(v)
        }
        val v = LayoutFilterItemBinding.inflate(inflater, parent, false)
        return ImageVH(v)
    }

    override fun getItemCount(): Int {
        return myDataset.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TOP) {
            holder as ViewTopVH
            holder.bind()
            return
        }
        holder as ImageVH
        holder.bind(position - 1)
    }

    inner class ImageVH(val binding: LayoutFilterItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val item = myDataset[position]

            var isOnTxt = if(item.isChecked) " ✔" else ""

            if (item.isChecked && !isAllActive ) {
                binding.filterItemText.setTypeface(null, Typeface.BOLD)
            }
            else {
                isOnTxt = ""
                binding.filterItemText.setTypeface(null, Typeface.NORMAL)
            }
            val displayName = when (item.categoryType.displayName) {
                "Other" -> context.getString(R.string.category_other)
                "Se nourrir" -> context.getString(R.string.category_food)
                "Se loger" -> context.getString(R.string.category_housing)
                "Se soigner" -> context.getString(R.string.category_medical)
                "S'orienter" -> context.getString(R.string.category_orientation)
                "Se réinsérer" -> context.getString(R.string.category_insertion)
                "Partenaires" -> context.getString(R.string.category_partners)
                "Toilettes" -> context.getString(R.string.category_toilettes)
                "Fontaines" -> context.getString(R.string.category_fontaines)
                "Douches" -> context.getString(R.string.category_douches)
                "Laveries" -> context.getString(R.string.category_laverlinge)
                "Bien-être & activités" -> context.getString(R.string.category_self_care)
                "Vêtements & matériels" -> context.getString(R.string.category_vetements)
                "Bagageries" -> context.getString(R.string.category_bagages)
                "Boîtes à dons & lire" -> context.getString(R.string.category_boitesdons)
                else -> item.categoryType.displayName
            }

            binding.filterItemText.text = displayName + isOnTxt

            binding.filterItemImage.setImageResource(item.categoryType.filterId)

            binding.filterItemSwitch.visibility = View.INVISIBLE
            binding.filterItemSeparator.visibility = View.INVISIBLE

            itemView.setOnClickListener {
                listener(position)
            }
        }
    }

    inner class ViewTopVH(val binding: LayoutFilterTopBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.uiLayoutAssos.setOnClickListener {
                listenerTop(0)
            }
            binding.filterLayoutDonate.setOnClickListener {
                listenerTop(1)
            }
            binding.filterLayoutVolunteer.setOnClickListener {
                listenerTop(2)
            }

            val isOnTxt = if(isAllActive) "" else  " ✔"

            if (isPartnerSelected && !isAllActive) {
                binding.filterItemTextPartner.setTypeface(null, Typeface.BOLD)
            }
            else {
                binding.filterItemTextPartner.setTypeface(null, Typeface.NORMAL)
            }
            val _addTxt = if(isPartnerSelected)  isOnTxt else ""
            binding.filterItemTextPartner.text = context.getString(R.string.guide_display_partners) + _addTxt

            if (isVolunteerSelected&& !isAllActive) {
                binding.filterItemTextVolunteer.setTypeface(null, Typeface.BOLD)
            }
            else {
                binding.filterItemTextVolunteer.setTypeface(null, Typeface.NORMAL)
            }
            val _addTxt1 = if(isVolunteerSelected)  isOnTxt else ""
            binding.filterItemTextVolunteer.text = context.getString(R.string.guide_filter_volunteer) + _addTxt1

            if (isDonatedSelected && !isAllActive) {
                binding.filterItemTextDonate.setTypeface(null, Typeface.BOLD)
            }
            else {
                binding.filterItemTextDonate.setTypeface(null, Typeface.NORMAL)
            }
            val _addTxt2 = if(isDonatedSelected)  isOnTxt else ""
            binding.filterItemTextDonate.text = context.getString(R.string.guide_filter_donate) + _addTxt2
        }
    }

    companion object {
        const val VIEW_TOP = 11
        const val VIEW_OTHER = 10
    }
}