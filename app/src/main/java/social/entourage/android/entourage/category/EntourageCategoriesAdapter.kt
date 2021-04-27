package social.entourage.android.entourage.category

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import social.entourage.android.R
import java.util.*

/**
 * Entourage categories adapter, with the group at index zero acting as an empty header.<br></br>
 * Created by Mihai Ionescu on 21/09/2017.
 */
class EntourageCategoriesAdapter(
        private val context: Context,
        private val categories: HashMap<String,  List<EntourageCategory>>,
        var selectedCategory: EntourageCategory, val listener:() -> Unit) : BaseExpandableListAdapter() {

    class EntourageCategoryViewHolder(v: View, checkboxListener: OnCheckedChangeListener?) {
        var mIcon: ImageView = v.findViewById(R.id.entourage_category_item_icon)
        var mLabel: TextView = v.findViewById(R.id.entourage_category_item_label)
        var mCheckbox: CheckBox = v.findViewById(R.id.entourage_category_item_checkbox)

        init {
            mCheckbox.setOnCheckedChangeListener(checkboxListener)
            v.setOnClickListener {
                if (checkboxListener != null) {
                    mCheckbox.isChecked = !mCheckbox.isChecked
                    checkboxListener.onCheckedChanged(mCheckbox, mCheckbox.isChecked)
                }
            }
        }
    }

    inner class OnCheckedChangeListener : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
            (compoundButton.tag as? EntourageCategory)?.let { category ->
                // unset the previously selected partner, if different than the current
                if (selectedCategory != category) {
                    selectedCategory.isSelected = false
                }

                // save the state
                category.isSelected = isChecked
                if (selectedCategory != category) {
                    selectedCategory = category
                    selectedCategory.isNewlyCreated = false
                } else {
                    selectedCategory.isNewlyCreated = !category.isSelected
                }

                listener()
                // refresh the list view
                notifyDataSetChanged()
            }
        }
    }

    private val onCheckedChangeListener = OnCheckedChangeListener()

    override fun getChildrenCount(groupPosition: Int): Int {
        if (groupPosition == 0) return 0
        return categories[selectedCategory.groupType]?.size ?:0
    }

    override fun getChild(groupPosition: Int, childPosition: Int): EntourageCategory? {
        if (groupPosition == 0) return null
        return categories[selectedCategory.groupType]?.get(childPosition)
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        lateinit var childConvertView: View
        convertView?.let {
            childConvertView = it
        } ?: run {
            // create the child view
            val layoutInflater = context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            childConvertView = layoutInflater.inflate(R.layout.layout_entourage_category_item, null)
            childConvertView.tag = EntourageCategoryViewHolder(childConvertView, onCheckedChangeListener)
        }
        // populate the child view
        getChild(groupPosition, childPosition)?.let { category:EntourageCategory->
            val viewHolder = childConvertView.tag as EntourageCategoryViewHolder? ?: return@let
            viewHolder.mIcon.setImageResource(category.iconRes)
            viewHolder.mIcon.clearColorFilter()
            viewHolder.mIcon.setColorFilter(ContextCompat.getColor(context, category.typeColorRes), PorterDuff.Mode.SRC_IN)
            viewHolder.mLabel.text = category.title
            if (category.isSelected) {
                viewHolder.mLabel.setTypeface(null, Typeface.BOLD)
            } else {
                viewHolder.mLabel.setTypeface(null, Typeface.NORMAL)
            }

            // set the tag to null so that oncheckedchangelistener isn't fired when populating the view
            viewHolder.mCheckbox.tag = null
            // set the check state
            viewHolder.mCheckbox.isChecked = category.isSelected
            // set the tag to the item position
            viewHolder.mCheckbox.tag = category
        }
        return childConvertView
    }

    override fun getGroupCount(): Int {
        return 2 // Header + specific list
    }

    override fun getGroup(groupPosition: Int): String? {
        return if (groupPosition == 0) null else selectedCategory.groupType
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        lateinit var groupView: View
        convertView?.let {
            groupView = it
        } ?: run {
            // create the group view
            val layoutInflater = context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            groupView = if (groupPosition == 0) {
                layoutInflater.inflate(R.layout.layout_entourage_category_header, null)
            } else {
                layoutInflater.inflate(R.layout.layout_entourage_category_group, null)
            }
        }
        // populate the group
        if (groupPosition != 0) {
            selectedCategory.groupType?.let {
                groupView.findViewById<TextView>(R.id.entourage_category_group_label)?.setText(
                        EntourageCategoryManager.getGroupTypeDescription(it))
            }
        }
        return groupView
    }

    override fun getGroupTypeCount(): Int {
        return 2
    }

    override fun getGroupType(groupPosition: Int): Int {
        return if (groupPosition == 0) GROUP_TYPE_EMPTY else GROUP_TYPE_CATEGORY
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    companion object {
        private const val GROUP_TYPE_EMPTY = 0
        private const val GROUP_TYPE_CATEGORY = 1
    }

}