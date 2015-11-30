package social.entourage.android.api.tape;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import javax.inject.Inject;

import social.entourage.android.EntourageApplication;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.map.encounter.CreateEncounterPresenter.EncounterUploadCallback;
import social.entourage.android.map.encounter.CreateEncounterPresenter.EncounterUploadTask;

public class EncounterTapeService extends Service implements EncounterUploadCallback {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    EncounterTapeTaskQueue queue;
    private static boolean running;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public void onCreate() {
        super.onCreate();
        EntourageApplication.get(this).getEntourageComponent().inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (queue != null) {
            executeNext();
        } else {
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void executeNext() {
        if (running) {
            return;
        }
        EncounterUploadTask task = queue.peek();
        if (task != null) {
            running = true;
            task.execute(this);
        } else {
            stopSelf();
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
        //stopSelf();
    }
}
