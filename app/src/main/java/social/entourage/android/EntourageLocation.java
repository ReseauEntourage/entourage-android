package social.entourage.android;

import android.location.Location;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Francois on 02/06/2015.
 */
public class EntourageLocation {
    private static EntourageLocation ourInstance = new EntourageLocation();

    public static EntourageLocation getInstance() {
        return ourInstance;
    }

    private EntourageLocation() {
        lastCameraPosition = new CameraPosition(new LatLng(INITIAL_LATITUDE, INITIAL_LONGITUDE), INITIAL_CAMERA_FACTOR,0, 0);
        lastLocation = null;
    }

    private static final double INITIAL_LATITUDE = 48.841636;
    private static final double INITIAL_LONGITUDE = 2.335899;
    private static final float INITIAL_CAMERA_FACTOR = 15;

    private Location lastLocation;
    private CameraPosition lastCameraPosition;
    public CameraPosition getLastCameraPosition() {
        return lastCameraPosition;
    }

    public void saveCameraPosition(CameraPosition newCameraPosition) {
        lastCameraPosition = newCameraPosition;
    }

    public Location getLocation() {
        return lastLocation;
    }

    public void saveLocation(Location l) {
        lastLocation = l;
    }

    public LatLng getLatLng() {
        return new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
    }
}
