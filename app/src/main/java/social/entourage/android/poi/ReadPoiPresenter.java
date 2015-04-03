package social.entourage.android.poi;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import java.util.Locale;

import social.entourage.android.api.model.map.Poi;

/**
 * Presenter controlling the main activity
 */
public class ReadPoiPresenter {
    private final ReadPoiActivity activity;

    public ReadPoiPresenter(final ReadPoiActivity activity) {
        this.activity = activity;
    }

    public void displayPoi(Poi poi) {
        activity.displayPoi(poi, new OnAddressClickListener(poi.getAdress()));
    }

    public void openExternalMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }


    public class OnAddressClickListener implements View.OnClickListener {

        private String address;

        public OnAddressClickListener(final String address) {
            this.address = address;
        }

        @Override
        public void onClick(final View v) {
            Uri uri = Uri.parse(String.format(Locale.FRENCH, "geo:0,0?q=%s", address));
            openExternalMap(uri);
        }
    }
}
