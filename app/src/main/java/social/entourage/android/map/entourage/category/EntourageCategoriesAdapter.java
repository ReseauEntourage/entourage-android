package social.entourage.android.map.entourage.category;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import social.entourage.android.R;

/**
 * Created by Mihai Ionescu on 21/09/2017.
 */

public class EntourageCategoriesAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> entourageTypeList;
    private HashMap<String, List<EntourageCategory>> entourageCategoryHashMap;

    public EntourageCategoriesAdapter(Context context, List<String> entourageTypeList, HashMap<String, List<EntourageCategory>> entourageCategoryHashMap) {
        this.context = context;
        this.entourageTypeList = entourageTypeList;
        this.entourageCategoryHashMap = entourageCategoryHashMap;
    }

    @Override
    public int getChildrenCount(final int groupPosition) {
        return this.entourageCategoryHashMap.get(this.entourageTypeList.get(groupPosition)).size();
    }

    @Override
    public Object getChild(final int groupPosition, final int childPosition) {
        return this.entourageCategoryHashMap.get(this.entourageTypeList.get(groupPosition))
                .get(childPosition);
    }

    @Override
    public long getChildId(final int groupPosition, final int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            // create the child view
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.layout_entourage_category_item, null);
        }
        // populate the child view
        EntourageCategory category = (EntourageCategory)getChild(groupPosition, childPosition);
        ImageView icon = (ImageView)convertView.findViewById(R.id.entourage_category_item_icon);
        TextView label = (TextView)convertView.findViewById(R.id.entourage_category_item_label);
        icon.setImageResource(category.getIconRes());
        icon.clearColorFilter();
        icon.setColorFilter(ContextCompat.getColor(context, category.getTypeColorRes()), PorterDuff.Mode.SRC_IN);
        label.setText(category.getTitle());

        return convertView;
    }

    @Override
    public int getGroupCount() {
        return this.entourageTypeList.size();
    }

    @Override
    public Object getGroup(final int groupPosition) {
        return this.entourageTypeList.get(groupPosition);
    }

    @Override
    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, final ViewGroup parent) {
        boolean _isExpanded = isExpanded;
        if (convertView == null) {
            // create the group view
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.layout_entourage_category_group, null);
            // expand it
            ((ExpandableListView)parent).expandGroup(groupPosition);
            _isExpanded = true;
        }
        // populate the group
        TextView label = (TextView)convertView.findViewById(R.id.entourage_category_group_label);
        ImageView arrow = (ImageView)convertView.findViewById(R.id.entourage_category_group_arrow);
        label.setText( EntourageCategory.getEntourageTypeDescription((String)getGroup(groupPosition)) );
        if (_isExpanded) {
            arrow.setRotation(-90.0f);
        } else {
            arrow.setRotation(0.0f);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(final int groupPosition, final int childPosition) {
        return true;
    }
}
