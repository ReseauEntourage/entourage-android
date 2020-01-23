package social.entourage.android.guide.poi;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import social.entourage.android.api.model.guide.Poi;
import social.entourage.android.map.OnAddressClickListener;

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
        OnAddressClickListener listenerAddress = null;
        OnPhoneClickListener listenerPhone = null;
        if(poi.getAddress()!=null) {
            listenerAddress = new OnAddressClickListener(fragment.getActivity(), poi.getAddress());
        }
        if(poi.getPhone()!=null) {
            listenerPhone = new OnPhoneClickListener(poi.getPhone());
        }
        fragment.onDisplayedPoi(poi, listenerAddress, listenerPhone);
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
