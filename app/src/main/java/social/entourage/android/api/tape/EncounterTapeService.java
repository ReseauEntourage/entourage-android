package social.entourage.android.api.tape;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import social.entourage.android.EntourageApplication;
import social.entourage.android.api.tape.Events.*;
import social.entourage.android.map.encounter.CreateEncounterPresenter.EncounterUploadCallback;
import social.entourage.android.map.encounter.CreateEncounterPresenter.EncounterUploadTask;
import social.entourage.android.tools.BusProvider;

public class EncounterTapeService extends Service implements EncounterUploadCallback {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    EncounterTapeTaskQueue queue;
    private static boolean running;
    private boolean connected;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public void onCreate() {
        super.onCreate();
        EntourageApplication.get(this).getEntourageComponent().inject(this);
        connected = isConnected();
        BusProvider.getInstance().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (queue != null) {
            executeNext();
        } else {
            stopService();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ----------------------------------
    // METHODS
    // ----------------------------------

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.isConnected();
    }

    private void stopService() {
        BusProvider.getInstance().unregister(this);
        stopSelf();
    }

    private void executeNext() {
        if (connected) {
            if (running) {
                return;
            }
            EncounterUploadTask task = queue.peek();
            if (task != null) {
                running = true;
                task.execute(this);
            } else {
                stopService();
            }
        }
    }

    @Override
    public void onSuccess() {
        running = false;
        queue.remove();
        executeNext();
    }

    @Override
    public void onFailure() {
        running = false;
        executeNext();
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void onConnectionChanged(OnConnectionChangedEvent event) {
        connected = event.isConnected();
        if (connected) executeNext();
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public static class ConnectionChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            BusProvider.getInstance().register(this);
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetInfo != null && activeNetInfo.isConnected()) {
                BusProvider.getInstance().post(new OnConnectionChangedEvent(true));
            } else {
                BusProvider.getInstance().post(new OnConnectionChangedEvent(false));
            }
            BusProvider.getInstance().unregister(this);
        }

    }
}
