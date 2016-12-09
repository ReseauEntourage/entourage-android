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

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;
import static com.google.android.gms.common.api.CommonStatusCodes.SUCCESS;
import static com.google.android.gms.location.LocationServices.FusedLocationApi;
import static com.google.android.gms.location.LocationServices.SettingsApi;

public class FusedLocationProvider {
    private final Context context;
    private final GoogleApiClient apiClient;
    private final UserType userType;
    private LocationListener locationListener;
    private ProviderStatusListener statusListener;

    public FusedLocationProvider(final Context context,
                                 final UserType userType) {
        this.context = context.getApplicationContext();
        this.apiClient = initializeGoogleApiClient(context.getApplicationContext());
        this.userType = userType;
    }

    public void start() {
        if (!apiClient.isConnected()) {
            apiClient.connect();
        } else {
            requestLocationUpdates();
        }
    }

    public void stop() {
        if (apiClient.isConnected()) {
            apiClient.disconnect();
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

    private void registerListener(final ProviderStatusListener statusListener) {
        if (!apiClient.isConnected()) {
            return;
        }

        SettingsApi
            .checkLocationSettings(apiClient,
                new LocationSettingsRequest.Builder()
                    .addLocationRequest(getLocationRequest())
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
        Location lastLocation = FusedLocationApi.getLastLocation(apiClient);
        System.out.println("LAST LOCATION = " + lastLocation);
        return lastLocation;
    }

    private void removeLocationUpdates() {
        if (locationListener == null) {
            return;
        }
        if (!apiClient.isConnected()) {
            return;
        }
        FusedLocationApi.removeLocationUpdates(apiClient, locationListener);
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
        if (!apiClient.isConnected()) {
            return;
        }
        FusedLocationApi.requestLocationUpdates(apiClient, getLocationRequest(), locationListener);
    }

    private LocationRequest getLocationRequest() {
        if (UserType.PRO.equals(userType)) {
            return createLocationRequestForProUsage();
        } else {
            return createLocationRequestForPublicUsage();
        }
    }

    private LocationRequest createLocationRequestForPublicUsage() {
        return new LocationRequest()
            .setInterval(300000)
            .setFastestInterval(60000)
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private LocationRequest createLocationRequestForProUsage() {
        return new LocationRequest()
            .setInterval(20000)
            .setFastestInterval(10000)
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
