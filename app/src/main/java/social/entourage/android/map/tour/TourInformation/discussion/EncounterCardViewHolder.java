package social.entourage.android.map.tour.TourInformation.discussion;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.Encounter;

/**
 * Encounter Card View
 */
public class EncounterCardViewHolder extends BaseCardViewHolder {

    private TextView mStreetPersonNameView;
    private TextView mMessageView;

    private Context context;

    private boolean addressRetrieved = false;

    public EncounterCardViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {

        context = itemView.getContext();

        mStreetPersonNameView = (TextView) itemView.findViewById(R.id.tic_encounter_street_name);
        mMessageView = (TextView) itemView.findViewById(R.id.tic_encounter_message);
    }

    @Override
    public void populate(final TimestampedObject data) {
        populate((Encounter)data);
    }

    public void populate(Encounter encounter) {
        String location = "";
        Address address = encounter.getAddress();
        if (address != null) {
            if (address.getMaxAddressLineIndex() > 0) {
                location = address.getAddressLine(0);
            }
        }
        else {
            if (!addressRetrieved) {
                new GeocoderTask().execute(encounter);
            }
        }
        String encounterDate = "";
        if (encounter.getCreationDate() != null) {
            encounterDate = DateFormat.getDateFormat(context).format(encounter.getCreationDate());
        }
        String encounterLocation = itemView.getResources().getString(R.string.encounter_read_location,
                encounter.getUserName(),
                encounter.getStreetPersonName(),
                location,
                encounterDate);
        mStreetPersonNameView.setText(encounterLocation);
        mMessageView.setText(encounter.getMessage());
    }

    public static int getLayoutResource() {
        return R.layout.tour_information_encounter_card_view;
    }

    private class GeocoderTask extends AsyncTask<Encounter, Void, Encounter> {

        @Override
        protected Encounter doInBackground(final Encounter... params) {
            try {
                Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
                Encounter encounter = params[0];
                List<Address> addresses = geoCoder.getFromLocation(encounter.getLatitude(), encounter.getLongitude(), 1);
                if (addresses.size() > 0) {
                    encounter.setAddress(addresses.get(0));
                }
                return encounter;
            }
            catch (IOException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(final Encounter encounter) {
            addressRetrieved = true;
            populate(encounter);
        }
    }
}
