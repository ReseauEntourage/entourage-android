package social.entourage.android.guide.filter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.layout_filter_item.view.*
import social.entourage.android.R
import social.entourage.android.guide.filter.GuideFilter.Companion.instance
import social.entourage.android.guide.poi.PoiRenderer.CategoryType

/**
 * Created by mihaiionescu on 28/03/2017.
 */
class GuideFilterAdapter(var context: Context) : BaseAdapter() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    val items: MutableList<GuideFilterItem> = ArrayList()

    var isHelpOnly = false

    fun setHelpOnly() {
        isHelpOnly = true
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        // We need to ignore the category at index zero
        return items.size
    }

    override fun getItem(position: Int): GuideFilterItem {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        //??
        return 0
    }

    override fun getView(position: Int, view2: View?, viewGroup: ViewGroup): View {
        var view = view2
        val viewHolder: GuideFilterViewHolder
        if (view == null) {
            view = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_filter_item, viewGroup, false)
            viewHolder = GuideFilterViewHolder(view as View)
            view.tag = viewHolder
        }
        // Populate the view
        val item = getItem(position)
        val categoryDisplayName = when (item.categoryType.displayName) {
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


        val categoryType = item.categoryType
        val displayName: String = if (categoryType.categoryId == CategoryType.PARTNERS.categoryId) {
            view.context?.getString(R.string.guide_display_partners) ?: categoryDisplayName
        } else categoryDisplayName
        view.filter_item_text?.text = displayName
        view.filter_item_image?.setImageResource(categoryType.filterId)
        // set the switch
        view.filter_item_switch?.isChecked = item.isChecked
        view.filter_item_switch?.tag = position
        // separator is visible unless last element
        view.filter_item_separator?.visibility = if (position == count - 1) View.GONE else View.VISIBLE
        return view
    }

    // ----------------------------------
    // View Holder
    // ----------------------------------
    inner class GuideFilterViewHolder(v: View) {
        init {
            if (!isHelpOnly) {
                v.filter_item_switch?.setOnCheckedChangeListener { buttonView, isChecked ->
                    // if no tag, exit
                    if (buttonView.tag != null) {
                        getItem(buttonView.tag as Int).isChecked = isChecked
                    }
                }
            }
            else {
                v.filter_item_switch?.visibility = View.INVISIBLE
            }
        }
    }

    // ----------------------------------
    // Filter Item
    // ----------------------------------
    class GuideFilterItem(var categoryType: CategoryType, var isChecked: Boolean)

    // ----------------------------------
    // Implementation
    // ----------------------------------
    init {
        val guideFilter = instance
        for (i in 1 until CategoryType.values().size) {
            val categoryType = CategoryType.values()[i]
            if (categoryType != CategoryType.PARTNERS) {
                items.add(GuideFilterItem(categoryType, guideFilter.valueForCategoryId(categoryType.categoryId)))
            }
        }
    }
}