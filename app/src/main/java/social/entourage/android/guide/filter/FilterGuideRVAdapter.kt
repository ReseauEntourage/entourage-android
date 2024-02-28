package social.entourage.android.guide.filter

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_filter_item.view.*
import kotlinx.android.synthetic.main.layout_filter_top.view.*
import social.entourage.android.R

/**
 * Created by Jr (MJ-DEVS) on 12/10/2020.
 */
class FilterGuideRVAdapter(val context: Context, private var myDataset: ArrayList<GuideFilterAdapter.GuideFilterItem>,
                           var isPartnerSelected:Boolean,
                           var isDonatedSelected:Boolean,
                           var isVolunteerSelected:Boolean,
                           var isAllActive:Boolean,
                           val listener:(position:Int) -> Unit,
                           val listenerTop:(position:Int) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val VIEW_TOP = 11
    val VIEW_OTHER = 10

    fun updateDatas(items: ArrayList<GuideFilterAdapter.GuideFilterItem>, isPartnerSelected:Boolean,
                     isDonatedSelected:Boolean,
                     isVolunteerSelected:Boolean,isAllActive:Boolean) {
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
            val v = inflater.inflate(R.layout.layout_filter_top,parent, false)
            return ViewTopVH(v)
        }
        val v = inflater.inflate(R.layout.layout_filter_item,parent, false)
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

    inner class ImageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val item = myDataset[position]

            var isOnTxt = if(item.isChecked) " ✔" else ""

            if (item.isChecked && !isAllActive ) {
                itemView.filter_item_text.setTypeface(null, Typeface.BOLD)
            }
            else {
                isOnTxt = ""
                itemView.filter_item_text.setTypeface(null, Typeface.NORMAL)
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

            itemView.filter_item_text?.text = displayName + isOnTxt

            itemView.filter_item_image?.setImageResource(item.categoryType.filterId)

            itemView.filter_item_switch.visibility = View.INVISIBLE
            itemView.filter_item_separator.visibility = View.INVISIBLE

            itemView.setOnClickListener {
                listener(position)
            }
        }
    }

    inner class ViewTopVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.ui_layout_assos?.setOnClickListener {
                listenerTop(0)
            }
            itemView.filter_layout_donate?.setOnClickListener {
                listenerTop(1)
            }
            itemView.filter_layout_volunteer?.setOnClickListener {
                listenerTop(2)
            }

            val isOnTxt = if(isAllActive) "" else  " ✔"

            if (isPartnerSelected && !isAllActive) {
                itemView.filter_item_text_partner.setTypeface(null, Typeface.BOLD)
            }
            else {
                itemView.filter_item_text_partner.setTypeface(null, Typeface.NORMAL)
            }
            val _addTxt = if(isPartnerSelected)  isOnTxt else ""
            itemView.filter_item_text_partner.text = context.getString(R.string.guide_display_partners) + _addTxt

            if (isVolunteerSelected&& !isAllActive) {
                itemView.filter_item_text_volunteer.setTypeface(null, Typeface.BOLD)
            }
            else {
                itemView.filter_item_text_volunteer.setTypeface(null, Typeface.NORMAL)
            }
            val _addTxt1 = if(isVolunteerSelected)  isOnTxt else ""
            itemView.filter_item_text_volunteer.text = context.getString(R.string.guide_filter_volunteer) + _addTxt1

            if (isDonatedSelected && !isAllActive) {
                itemView.filter_item_text_donate.setTypeface(null, Typeface.BOLD)
            }
            else {
                itemView.filter_item_text_donate.setTypeface(null, Typeface.NORMAL)
            }
            val _addTxt2 = if(isDonatedSelected)  isOnTxt else ""
            itemView.filter_item_text_donate.text = context.getString(R.string.guide_filter_donate) + _addTxt2
        }
    }
}