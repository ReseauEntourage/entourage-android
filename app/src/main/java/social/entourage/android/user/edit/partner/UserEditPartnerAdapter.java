package social.entourage.android.user.edit.partner;

import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
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

        public PartnerViewHolder(View v) {
            mPartnerName = (TextView) v.findViewById(R.id.partner_name);
            mPartnerLogo = (ImageView) v.findViewById(R.id.partner_logo);
            mCheckbox = (CheckBox) v.findViewById(R.id.partner_checkbox);
        }

    }


    public int selectedPartnerPosition = -1;

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
            viewHolder = new PartnerViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (PartnerViewHolder) view.getTag();
        }

        // Populate the view
        Partner partner = getItem(position);
        if (partner != null) {
            viewHolder.mPartnerName.setText(partner.getName());
            viewHolder.mPartnerName.setTypeface(viewHolder.mPartnerName.getTypeface(), partner.isDefault() ? Typeface.BOLD : Typeface.NORMAL);

            String partnerLogo = partner.getLargeLogoUrl();
            if (partnerLogo != null) {
                Picasso.with(viewGroup.getContext())
                        .load(Uri.parse(partnerLogo))
                        .into(viewHolder.mPartnerLogo);
            } else {
                viewHolder.mPartnerLogo.setImageResource(0);
            }
            
            viewHolder.mCheckbox.setChecked(partner.isDefault());
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
}
