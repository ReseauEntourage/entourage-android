package social.entourage.android.user.edit.association;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.Organization;

/**
 * Created by mihaiionescu on 16/01/2017.
 */

public class UserEditAssociationAdapter extends BaseAdapter {

    public static class AssociationViewHolder {

        public TextView mAssociationName;
        public ImageView mAssociationLogo;
        public CheckBox mCheckbox;

        public AssociationViewHolder(View v) {
            mAssociationName = (TextView) v.findViewById(R.id.association_name);
            mAssociationLogo = (ImageView) v.findViewById(R.id.association_logo);
            mCheckbox = (CheckBox) v.findViewById(R.id.association_checkbox);
        }

    }

    private List<Organization> organizationList;

    public void setOrganizationList(final List<Organization> organizationList) {
        this.organizationList = organizationList;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return organizationList == null ? 0 : organizationList.size();
    }

    @Override
    public View getView(final int position, View view, final ViewGroup viewGroup) {
        AssociationViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_edit_association, viewGroup, false);
            viewHolder = new AssociationViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (AssociationViewHolder) view.getTag();
        }

        // Populate the view
        Organization organization = getItem(position);
        if (organization != null) {

        }

        return view;
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public Organization getItem(final int position) {
        if (organizationList == null || position < 0 || position >= organizationList.size()) {
            return null;
        }
        return organizationList.get(position);
    }
}
