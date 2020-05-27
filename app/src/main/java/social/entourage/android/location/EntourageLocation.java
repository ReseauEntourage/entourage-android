package social.entourage.android.location;

import android.location.Location;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import social.entourage.android.EntourageApplication;
import social.entourage.android.api.model.User;

public class EntourageLocation {

    private static final double INITIAL_LATITUDE = 48.841636;
    private static final double INITIAL_LONGITUDE = 2.335899;
    public static final float INITIAL_CAMERA_FACTOR = 15;
    public static final float INITIAL_CAMERA_FACTOR_ENTOURAGE_VIEW = 14;

    private static final EntourageLocation ourInstance = new EntourageLocation();

    private Location lastLocation;
    private Location currentLocation;
    private Location initialLocation;
    private CameraPosition lastCameraPosition;
    private CameraPosition currentCameraPosition;

    public static EntourageLocation getInstance() {
        return ourInstance;
    }

    private EntourageLocation() {
        User me = EntourageApplication.get().getEntourageComponent().getAuthenticationController().getUser();
        if (me != null) {
            User.Address address = me.getAddress();
            if (address != null) {
                lastCameraPosition = new CameraPosition(new LatLng(address.getLatitude(), address.getLongitude()), INITIAL_CAMERA_FACTOR, 0, 0);
                currentCameraPosition = new CameraPosition(new LatLng(address.getLatitude(), address.getLongitude()), INITIAL_CAMERA_FACTOR, 0, 0);
                return;
            }
        }
        lastCameraPosition = new CameraPosition(new LatLng(INITIAL_LATITUDE, INITIAL_LONGITUDE), INITIAL_CAMERA_FACTOR, 0, 0);
        currentCameraPosition = new CameraPosition(new LatLng(INITIAL_LATITUDE, INITIAL_LONGITUDE), INITIAL_CAMERA_FACTOR, 0, 0);
    }

    public void setInitialLocation(Location initialLocation) {
        this.initialLocation = initialLocation;
        saveCurrentLocation(initialLocation);
        currentCameraPosition = new CameraPosition(new LatLng(initialLocation.getLatitude(), initialLocation.getLongitude()), INITIAL_CAMERA_FACTOR, 0, 0);
        lastCameraPosition = currentCameraPosition;
    }

    public Location getInitialLocation() {
        return initialLocation;
    }

    public Location getLocation() {
        return lastLocation;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public CameraPosition getLastCameraPosition() {
        return lastCameraPosition;
    }

    public CameraPosition getCurrentCameraPosition() {
        return currentCameraPosition;
    }

    @NotNull
    public LatLng getLatLng() {
        return new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
    }

    @Nullable
    public LatLng getCurrentLatLng() {
        if(currentLocation!=null) return new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        else return null;
    }

    public void saveLocation(Location l) {
        lastLocation = l;
    }

    public void saveCurrentLocation(Location l) {
        currentLocation = l;
    }

    public void saveLastCameraPosition(CameraPosition newCameraPosition) {
        lastCameraPosition = newCameraPosition;
    }

    public void saveCurrentCameraPosition(CameraPosition newCameraPosition) {
        currentCameraPosition = newCameraPosition;
    }

    public static Location cameraPositionToLocation(String provider, CameraPosition cameraPosition) {
        Location location = new Location(provider);
        location.setLatitude(cameraPosition.target.latitude);
        location.setLongitude(cameraPosition.target.longitude);
        return location;
    }
}
