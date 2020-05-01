package social.entourage.android.entourage.category;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.map.BaseEntourage;

/**
 * Entourage categories adapter, with the group at index zero acting as an empty header.<br/>
 * Created by Mihai Ionescu on 21/09/2017.
 */

public class EntourageCategoriesAdapter extends BaseExpandableListAdapter {

    public static class EntourageCategoryViewHolder {

        ImageView mIcon;
        TextView mLabel;
        CheckBox mCheckbox;

        public EntourageCategoryViewHolder(View v, final OnCheckedChangeListener checkboxListener) {
            mIcon = v.findViewById(R.id.entourage_category_item_icon);
            mLabel = v.findViewById(R.id.entourage_category_item_label);
            mCheckbox = v.findViewById(R.id.entourage_category_item_checkbox);

            mCheckbox.setOnCheckedChangeListener(checkboxListener);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (checkboxListener != null) {
                        mCheckbox.setChecked(!mCheckbox.isChecked());
                        checkboxListener.onCheckedChanged(mCheckbox, mCheckbox.isChecked());
                    }
                }
            });

        }

    }

    private class OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
            // if no tag, exit
            if (compoundButton.getTag() == null) {
                return;
            }
            // get the category
            EntourageCategory category = (EntourageCategory) compoundButton.getTag();
            // unset the previously selected partner, if different than the current
            if (EntourageCategoriesAdapter.this.selectedCategory != category) {
                if (EntourageCategoriesAdapter.this.selectedCategory != null) {
                    EntourageCategoriesAdapter.this.selectedCategory.setSelected(false);
                }
            }

            // save the state
            category.setSelected(isChecked);
            if (EntourageCategoriesAdapter.this.selectedCategory != category) {
                EntourageCategoriesAdapter.this.selectedCategory = category;
                EntourageCategoriesAdapter.this.selectedCategory.setNewlyCreated(false);
            }
            else {
                if (category.isSelected()) {
                    EntourageCategoriesAdapter.this.selectedCategory.setNewlyCreated(false);
                }
                else {
                    EntourageCategoriesAdapter.this.selectedCategory.setNewlyCreated(true);
                }
            }

            // refresh the list view
            EntourageCategoriesAdapter.this.notifyDataSetChanged();
        }
    }

    private static final int GROUP_TYPE_EMPTY = 0;
    private static final int GROUP_TYPE_CATEGORY = 1;

    private Context context;
    private List<String> groupTypeList;
    private HashMap<String, List<EntourageCategory>> groupCategoryHashMap;

    public EntourageCategory selectedCategory;
    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener();

    private boolean isDemand;

    public EntourageCategoriesAdapter(Context context, List<String> groupTypeList, HashMap<String, List<EntourageCategory>> entourageCategoryHashMap, EntourageCategory selectedCategory,boolean isDemand) {
        this.context = context;
        this.groupTypeList = groupTypeList;
        this.groupCategoryHashMap = entourageCategoryHashMap;
        this.selectedCategory = selectedCategory;
        this.isDemand = isDemand;
    }

    @Override
    public int getChildrenCount(final int groupPosition) {
        if (groupPosition == 0) return 0;
       return isDemand ? this.groupCategoryHashMap.get(BaseEntourage.GROUPTYPE_ACTION_DEMAND).size() : this.groupCategoryHashMap.get(BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION).size();
    }

    @Override
    public Object getChild(final int groupPosition, final int childPosition) {
        if (groupPosition == 0) return null;
        return isDemand ? this.groupCategoryHashMap.get(BaseEntourage.GROUPTYPE_ACTION_DEMAND).get(childPosition) : this.groupCategoryHashMap.get(BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION).get(childPosition);
    }

    @Override
    public long getChildId(final int groupPosition, final int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild, View convertView, final ViewGroup parent) {
        EntourageCategoryViewHolder viewHolder;
        if (convertView == null) {
            // create the child view
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.layout_entourage_category_item, null);
            viewHolder = new EntourageCategoryViewHolder(convertView, onCheckedChangeListener);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (EntourageCategoryViewHolder)convertView.getTag();
        }
        // populate the child view
        EntourageCategory category = (EntourageCategory)getChild(groupPosition, childPosition);
        if (category != null) {
            viewHolder.mIcon.setImageResource(category.getIconRes());
            viewHolder.mIcon.clearColorFilter();
            viewHolder.mIcon.setColorFilter(ContextCompat.getColor(context, category.getTypeColorRes()), PorterDuff.Mode.SRC_IN);

            viewHolder.mLabel.setText(category.title);
            if (category.isSelected()) {
                viewHolder.mLabel.setTypeface(null, Typeface.BOLD);
            } else {
                viewHolder.mLabel.setTypeface(null, Typeface.NORMAL);
            }

            // set the tag to null so that oncheckedchangelistener isn't fired when populating the view
            viewHolder.mCheckbox.setTag(null);
            // set the check state
            viewHolder.mCheckbox.setChecked(category.isSelected());
            // set the tag to the item position
            viewHolder.mCheckbox.setTag(category);
        }

        return convertView;
    }

    @Override
    public int getGroupCount() {
        return 2; // Header + list
    }

    @Override
    public Object getGroup(final int groupPosition) {
        if (groupPosition == 0) return null;
        return this.groupTypeList.get(groupPosition - 1);
    }

    @Override
    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            // create the group view
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (groupPosition == 0) {
                convertView = layoutInflater.inflate(R.layout.layout_entourage_category_header, null);
            } else {
                convertView = layoutInflater.inflate(R.layout.layout_entourage_category_group, null);
            }
        }
        // populate the group
        if (groupPosition != 0) {
            TextView label = convertView.findViewById(R.id.entourage_category_group_label);
            if (label != null) {
                String titleKey = isDemand ? this.groupTypeList.get(0) : this.groupTypeList.get(1);
                label.setText(EntourageCategoryManager.getGroupTypeDescription(titleKey));
            }
        }

        return convertView;
    }

    @Override
    public int getGroupTypeCount() {
        return 2;
    }

    @Override
    public int getGroupType(final int groupPosition) {
        return groupPosition == 0 ? GROUP_TYPE_EMPTY : GROUP_TYPE_CATEGORY;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(final int groupPosition, final int childPosition) {
        return true;
    }
}
