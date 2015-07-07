package social.entourage.android.map;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import social.entourage.android.common.Constants;

/**
 * Created by NTE on 03/07/15.
 */
public class FetchAddressIntentService extends IntentService {

    protected ResultReceiver receiver;
    String errorMessage = "";

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        receiver.send(resultCode, bundle);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // test de récupération de l'adresse actuelle
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException ioException) {
            errorMessage = "Service not available";
            Log.e("Error", errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = "Invalid location values";
            Log.e("Error", errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +  location.getLongitude(), illegalArgumentException);
        }

        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No address found";
                Log.e("Error", errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            System.out.println("ADRESSE COURANTE : " + address.getAddressLine(0));
            //deliverResultToReceiver(Constants.SUCCESS_RESULT, address.getAddressLine(0));
        }

    }
}
