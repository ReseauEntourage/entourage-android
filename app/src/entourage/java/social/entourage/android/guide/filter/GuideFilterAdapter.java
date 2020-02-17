package social.entourage.android.guide.filter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import social.entourage.android.R;
import social.entourage.android.guide.PoiRenderer;

/**
 * Created by mihaiionescu on 28/03/2017.
 */

public class GuideFilterAdapter extends BaseAdapter {

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private List<GuideFilterItem> items = new ArrayList<>();

    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener();

    // ----------------------------------
    // Implementation
    // ----------------------------------


    public GuideFilterAdapter() {
        GuideFilter guideFilter = GuideFilter.getInstance();
        for (int i = 1; i < PoiRenderer.CategoryType.values().length; i++) {
            PoiRenderer.CategoryType categoryType = PoiRenderer.CategoryType.values()[i];
            items.add(new GuideFilterItem(categoryType, guideFilter.valueForCategoryId(categoryType.getCategoryId())));
        }
    }

    @Override
    public int getCount() {
        // We need to ignore the category at index zero
        return items.size();
    }

    @Override
    public GuideFilterItem getItem(final int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, final ViewGroup viewGroup) {
        GuideFilterViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_filter_item, viewGroup, false);
            viewHolder = new GuideFilterViewHolder(view, onCheckedChangeListener);
            view.setTag(viewHolder);
        } else {
            viewHolder = (GuideFilterViewHolder) view.getTag();
        }

        // Populate the view
        GuideFilterItem item = getItem(position);
        if (item != null && item.categoryType != null) {
            PoiRenderer.CategoryType categoryType = item.categoryType;

            viewHolder.mFilterName.setText(categoryType.getName());
            viewHolder.mFilterImage.setImageResource(categoryType.getResourceId());

            // set the switch
            viewHolder.mFilterSwitch.setChecked(item.isChecked);
            viewHolder.mFilterSwitch.setTag(position);

            // separator is visible unless last element
            viewHolder.mSeparatorView.setVisibility(position == getCount()-1 ? View.GONE : View.VISIBLE);

            // set the category id
            viewHolder.categoryId = categoryType.getCategoryId();
        }

        return view;
    }

    // ----------------------------------
    // View Holder
    // ----------------------------------

    public static class GuideFilterViewHolder {

        public TextView mFilterName;
        public ImageView mFilterImage;
        public Switch mFilterSwitch;
        public View mSeparatorView;

        public long categoryId = 0;

        public GuideFilterViewHolder(View v, OnCheckedChangeListener onCheckedChangeListener) {
            mFilterName = v.findViewById(R.id.filter_item_text);
            mFilterImage = v.findViewById(R.id.filter_item_image);
            mFilterSwitch = v.findViewById(R.id.filter_item_switch);
            mSeparatorView = v.findViewById(R.id.filter_item_separator);

            mFilterSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
        }

    }

    // ----------------------------------
    // Filter Item
    // ----------------------------------

    public static class GuideFilterItem {

        public PoiRenderer.CategoryType categoryType;
        public boolean isChecked;

        public GuideFilterItem(PoiRenderer.CategoryType categoryType, boolean isChecked) {
            this.categoryType = categoryType;
            this.isChecked = isChecked;
        }

    }

    private class OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
            // if no tag, exit
            if (compoundButton.getTag() == null) {
                return;
            }
            // get the position
            int position = (Integer) compoundButton.getTag();

            GuideFilterItem item = GuideFilterAdapter.this.getItem(position);
            if (item == null) {
                return;
            }

            item.isChecked = isChecked;
        }
    }
}
