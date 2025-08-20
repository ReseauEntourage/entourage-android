package social.entourage.android.guide.filter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import social.entourage.android.R
import social.entourage.android.databinding.LayoutFilterItemBinding
import social.entourage.android.guide.filter.GuideFilter.Companion.instance
import social.entourage.android.guide.poi.PoiRenderer.CategoryType

/**
 * Created by mihaiionescu on 28/03/2017.
 */
class GuideFilterItemAdapter(var context: Context) : BaseAdapter() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private val items: MutableList<GuideFilterItem> = ArrayList()

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

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View {
        var binding = (view?.tag as? GuideFilterViewHolder)?.binding
        if (binding == null) {
            binding = LayoutFilterItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            binding.root.tag = GuideFilterViewHolder(binding)
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
            binding.root.context?.getString(R.string.guide_display_partners) ?: categoryDisplayName
        } else categoryDisplayName
        binding.filterItemText.text = displayName
        binding.filterItemImage.setImageResource(categoryType.filterId)
        // set the switch
        binding.filterItemSwitch.isChecked = item.isChecked
        binding.filterItemSwitch.tag = position
        // separator is visible unless last element
        binding.filterItemSeparator.visibility = if (position == count - 1) View.GONE else View.VISIBLE
        return binding.root
    }

    // ----------------------------------
    // View Holder
    // ----------------------------------
    inner class GuideFilterViewHolder(val binding: LayoutFilterItemBinding) {
        init {
            if (!isHelpOnly) {
                binding.filterItemSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                    // if no tag, exit
                    if (buttonView.tag != null) {
                        getItem(buttonView.tag as Int).isChecked = isChecked
                    }
                }
            }
            else {
                binding.filterItemSwitch.visibility = View.INVISIBLE
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
        for (i in 1 until CategoryType.entries.size) {
            val categoryType = CategoryType.entries[i]
            if (categoryType != CategoryType.PARTNERS) {
                items.add(GuideFilterItem(categoryType, guideFilter.valueForCategoryId(categoryType.categoryId)))
            }
        }
    }
}