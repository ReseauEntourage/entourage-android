package social.entourage.android.map.tour;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;
import static com.google.android.gms.common.api.CommonStatusCodes.SUCCESS;
import static com.google.android.gms.location.LocationServices.FusedLocationApi;
import static com.google.android.gms.location.LocationServices.SettingsApi;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class FusedLocationProvider {
    private final Context context;
    private final GoogleApiClient googleApiClient;
    private final UserType userType;
    private UserType locationUpdateUserType;
    private LocationListener locationListener;
    private ProviderStatusListener statusListener;

    public FusedLocationProvider(final Context context,
                                 final UserType userType) {
        this.context = context.getApplicationContext();
        this.googleApiClient = initializeGoogleApiClient(context.getApplicationContext());
        this.userType = userType;
        this.locationUpdateUserType = UserType.PUBLIC;
    }

    public void start() {
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        } else {
            requestLocationUpdates();
        }
    }

    public void stop() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
            removeLocationUpdates();
        }
    }

    public void setLocationListener(LocationListener listener) {
        this.locationListener = listener;
    }

    public void setStatusListener(final ProviderStatusListener statusListener) {
        this.statusListener = statusListener;
        registerListener(statusListener);
    }

    public void setLocationUpdateUserType(final UserType locationUpdateUserType) {
        if (this.locationUpdateUserType != locationUpdateUserType) {
            this.locationUpdateUserType = locationUpdateUserType;
            requestLocationUpdates();
        }
    }

    private void registerListener(final ProviderStatusListener statusListener) {
        if (!googleApiClient.isConnected()) {
            return;
        }

        SettingsApi
            .checkLocationSettings(googleApiClient,
                new LocationSettingsRequest.Builder()
                    .addAllLocationRequests(getAllLocationRequests())
                    .build())
            .setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult result) {
                    if (result.getStatus().getStatusCode() == SUCCESS) {
                        statusListener.onProviderEnabled();
                    } else {
                        statusListener.onProviderDisabled();
                    }
                }
            });
    }

    @SuppressWarnings("MissingPermission")
    public Location getLastKnownLocation() {
        if (geolocationPermissionIsNotGranted()) {
            return null;
        }
        Location lastLocation = FusedLocationApi.getLastLocation(googleApiClient);
        System.out.println("LAST LOCATION = " + lastLocation);
        return lastLocation;
    }

    private void removeLocationUpdates() {
        if (locationListener == null) {
            return;
        }
        if (!googleApiClient.isConnected()) {
            return;
        }
        FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
    }

    private GoogleApiClient initializeGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
            .addConnectionCallbacks(new FusedLocationConnectionCallbacks(this))
            .addOnConnectionFailedListener(new FusedLocationConnectionFailedListener())
            .addApi(LocationServices.API)
            .build();
    }

    private void onFusedLocationConnected() {
        if (statusListener != null) {
            registerListener(statusListener);
        }
        dispatchLastKnownLocation();
        requestLocationUpdates();
    }

    private void dispatchLastKnownLocation() {
        if (locationListener == null) {
            return;
        }

        Location lastKnownLocation = getLastKnownLocation();
        if (lastKnownLocation != null) {
            locationListener.onLocationChanged(lastKnownLocation);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void requestLocationUpdates() {
        if (geolocationPermissionIsNotGranted()) {
            return;
        }
        if (!googleApiClient.isConnected()) {
            return;
        }
        FusedLocationApi.requestLocationUpdates(googleApiClient, getLocationRequest(), locationListener);
    }

    private List<LocationRequest> getAllLocationRequests() {
        List<LocationRequest> locationRequestList = new ArrayList<>();

        if (UserType.PRO.equals(userType)) {
            locationRequestList.add(createLocationRequestForProUsage());
        }
        locationRequestList.add(createLocationRequestForPublicUsage());

        return locationRequestList;
    }

    private LocationRequest getLocationRequest() {
        if (UserType.PRO.equals(locationUpdateUserType)) {
            return createLocationRequestForProUsage();
        } else {
            return createLocationRequestForPublicUsage();
        }
    }

    private LocationRequest createLocationRequestForPublicUsage() {
        return new LocationRequest()
            .setInterval(MINUTES.toMillis(5))
            .setFastestInterval(MINUTES.toMillis(1))
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private LocationRequest createLocationRequestForProUsage() {
        return new LocationRequest()
            .setInterval(SECONDS.toMillis(20))
            .setFastestInterval(SECONDS.toMillis(10))
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private boolean geolocationPermissionIsNotGranted() {
        return checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
            || checkSelfPermission(context, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED;
    }

    public enum UserType {
        PUBLIC, PRO
    }

    public interface ProviderStatusListener {
        void onProviderEnabled();

        void onProviderDisabled();
    }

    private static class FusedLocationConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
        private final FusedLocationProvider provider;

        private FusedLocationConnectionCallbacks(FusedLocationProvider provider) {
            this.provider = provider;
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            provider.onFusedLocationConnected();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    }

    private static class FusedLocationConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.e("Entourage", "Cannot connect to Google API Client " + connectionResult);
        }
    }
}
