package social.entourage.android.guide.filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.layout_filter_item.view.*
import social.entourage.android.R
import social.entourage.android.guide.filter.GuideFilter.Companion.instance
import social.entourage.android.guide.poi.PoiRenderer.CategoryType
import java.util.*

/**
 * Created by mihaiionescu on 28/03/2017.
 */
class GuideFilterAdapter : BaseAdapter() {
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
        val categoryType = item.categoryType
        val displayName: String = if (categoryType.categoryId == CategoryType.PARTNERS.categoryId) {
            view.context?.getString(R.string.guide_display_partners) ?: categoryType.displayName
        } else categoryType.displayName
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