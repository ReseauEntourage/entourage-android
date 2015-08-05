package social.entourage.android;

import android.location.Location;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class EntourageLocation {

    private static final double INITIAL_LATITUDE = 48.841636;
    private static final double INITIAL_LONGITUDE = 2.335899;
    private static final float INITIAL_CAMERA_FACTOR = 15;

    private static final EntourageLocation ourInstance = new EntourageLocation();

    private Location lastLocation;
    private CameraPosition lastCameraPosition;
    private CameraPosition currentCameraPosition;

    public static EntourageLocation getInstance() {
        return ourInstance;
    }

    private EntourageLocation() {
        lastCameraPosition = new CameraPosition(new LatLng(INITIAL_LATITUDE, INITIAL_LONGITUDE), INITIAL_CAMERA_FACTOR,0, 0);
        currentCameraPosition = new CameraPosition(new LatLng(INITIAL_LATITUDE, INITIAL_LONGITUDE), INITIAL_CAMERA_FACTOR,0, 0);
        currentCameraPosition = null;
    }

    public Location getLocation() {
        return lastLocation;
    }

    public CameraPosition getLastCameraPosition() {
        return lastCameraPosition;
    }

    public CameraPosition getCurrentCameraPosition() {
        return currentCameraPosition;
    }

    public LatLng getLatLng() {
        return new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
    }

    public void saveLastCameraPosition(CameraPosition newCameraPosition) {
        lastCameraPosition = newCameraPosition;
    }

    public void saveCurrentCameraPosition(CameraPosition newCameraPosition) {
        currentCameraPosition = newCameraPosition;
    }

    public void saveLocation(Location l) {
        lastLocation = l;
    }
}
