package social.entourage.android.location;

import android.location.Address;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import social.entourage.android.R;

/**
 * Created by Mihai Ionescu on 23/11/2017.
 */

public class LocationSearchAdapter extends BaseAdapter {

    private List<Address> addressList = new ArrayList<>();

    @Override
    public int getCount() {
        return addressList.size();
    }

    @Override
    public Address getItem(final int position) {
        return addressList.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, final ViewGroup parent) {
        LocationSearchViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_location_search_result_item, parent, false);
            viewHolder = new LocationSearchViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (LocationSearchViewHolder)view.getTag();
        }
        // populate the view
        Address address = getItem(position);
        StringBuilder addressString = new StringBuilder();
        if (address != null) {
            if (address.getMaxAddressLineIndex() >= 0) {
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    String addressLine = address.getAddressLine(i);
                    if (addressLine != null && addressLine.length() > 0) {
                        addressString.append(addressLine);
                        addressString.append("\n");
                    }
                }
                if (addressString.length() > 0) {
                    addressString.deleteCharAt(addressString.length() - 1);
                }
            }
        }
        viewHolder.addressView.setText(addressString.toString());
        return view;
    }

    public void setAddressList(final List<Address> addressList) {
        this.addressList = addressList;
        notifyDataSetChanged();
    }

    // ----------------------------------
    // View Holder
    // ----------------------------------

    private static class LocationSearchViewHolder {

        public TextView addressView;

        public LocationSearchViewHolder(View view) {
            addressView = view.findViewById(R.id.location_address);
        }

    }

}
