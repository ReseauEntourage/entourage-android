package social.entourage.android.user.edit.partner;

import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.Partner;

/**
 * Created by mihaiionescu on 16/01/2017.
 */

public class UserEditPartnerAdapter extends BaseAdapter {

    public static class PartnerViewHolder {

        public TextView mPartnerName;
        public ImageView mPartnerLogo;
        public CheckBox mCheckbox;

        public PartnerViewHolder(View v, OnCheckedChangeListener checkboxListener) {
            mPartnerName = (TextView) v.findViewById(R.id.partner_name);
            mPartnerLogo = (ImageView) v.findViewById(R.id.partner_logo);
            mCheckbox = (CheckBox) v.findViewById(R.id.partner_checkbox);

            mCheckbox.setOnCheckedChangeListener(checkboxListener);
        }

    }


    public int selectedPartnerPosition = -1;

    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener();

    private List<Partner> partnerList;

    public List<Partner> getPartnerList() {
        return partnerList;
    }

    public void setPartnerList(final List<Partner> partnerList) {
        this.partnerList = partnerList;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return partnerList == null ? 0 : partnerList.size();
    }

    @Override
    public View getView(final int position, View view, final ViewGroup viewGroup) {
        PartnerViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_edit_partner, viewGroup, false);
            viewHolder = new PartnerViewHolder(view, onCheckedChangeListener);
            view.setTag(viewHolder);
        } else {
            viewHolder = (PartnerViewHolder) view.getTag();
        }

        // Populate the view
        Partner partner = getItem(position);
        if (partner != null) {
            viewHolder.mPartnerName.setText(partner.getName());
            viewHolder.mPartnerName.setTypeface(null, partner.isDefault() ? Typeface.BOLD : Typeface.NORMAL);

            String partnerLogo = partner.getLargeLogoUrl();
            if (partnerLogo != null) {
                Picasso.with(viewGroup.getContext())
                        .load(Uri.parse(partnerLogo))
                        .placeholder(null)
                        .into(viewHolder.mPartnerLogo);
            } else {
                viewHolder.mPartnerLogo.setImageDrawable(null);
            }

            // set the tag to null so that oncheckedchangelistener exits when populating the view
            viewHolder.mCheckbox.setTag(null);
            // set the check state
            viewHolder.mCheckbox.setChecked(partner.isDefault());
            // set the tag to the item position
            viewHolder.mCheckbox.setTag(position);
        }

        return view;
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public Partner getItem(final int position) {
        if (partnerList == null || position < 0 || position >= partnerList.size()) {
            return null;
        }
        return partnerList.get(position);
    }

    private class OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
            // if no tag, exit
            if (compoundButton.getTag() == null) {
                return;
            }
            // flag to check if we need to refresh the list view
            boolean needsRefresh = false;
            // get the position
            int position = (Integer) compoundButton.getTag();
            // unset the previously selected partner, if different than the current
            if (UserEditPartnerAdapter.this.selectedPartnerPosition != position) {
                Partner oldPartner = UserEditPartnerAdapter.this.getItem(UserEditPartnerAdapter.this.selectedPartnerPosition);
                if (oldPartner != null) {
                    oldPartner.setDefault(false);
                    needsRefresh = true;
                }
            }

            // save the state
            Partner partner = UserEditPartnerAdapter.this.getItem(position);
            if (partner != null) {
                partner.setDefault(isChecked);
                UserEditPartnerAdapter.this.selectedPartnerPosition = position;
            }

            // refresh the listview, if needed
            if (needsRefresh) {
                UserEditPartnerAdapter.this.notifyDataSetChanged();
            }
        }
    }
}
