package social.entourage.android.guide.poi;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import java.util.Locale;

import social.entourage.android.api.model.map.Poi;

/**
 * Presenter controlling the ReadPoiFragment
 * @see ReadPoiFragment
 */
public class ReadPoiPresenter {
    private final ReadPoiFragment fragment;

    public ReadPoiPresenter(final ReadPoiFragment fragment) {
        this.fragment = fragment;
    }

    public void displayPoi(Poi poi) {
        fragment.displayPoi(poi, new OnAddressClickListener(poi.getAddress()), new OnPhoneClickListener(poi.getPhone()));
    }

    private void openExternalMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(fragment.getActivity().getPackageManager()) != null) {
            fragment.startActivity(intent);
        }
    }

    private void dial(Uri phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(phone);
        if (fragment.getContext() != null) {
            if (intent.resolveActivity(fragment.getContext().getPackageManager()) != null) {
                fragment.startActivity(intent);
            }
        }
    }


    public class OnAddressClickListener implements View.OnClickListener {

        private final String address;

        public OnAddressClickListener(final String address) {
            this.address = address;
        }

        @Override
        public void onClick(final View v) {
            Uri uri = Uri.parse(String.format(Locale.FRENCH, "geo:0,0?q=%s", address));
            openExternalMap(uri);
        }
    }

    public class OnPhoneClickListener implements  View.OnClickListener {

        private final String phone;

        public OnPhoneClickListener(final String phone) {
            this.phone = phone;
        }

        @Override
        public void onClick(final View v) {
            Uri uri = Uri.parse("tel:" + phone);
            dial(uri);
        }
    }
}
