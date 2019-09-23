package social.entourage.android.api.tape;

import android.content.Context;
import android.content.Intent;

import com.squareup.tape.ObjectQueue;
import com.squareup.tape.TaskQueue;

import social.entourage.android.tour.encounter.CreateEncounterPresenter.EncounterUploadTask;

public class EncounterTapeTaskQueue extends TaskQueue<EncounterUploadTask> {

    private Context context;

    public EncounterTapeTaskQueue(ObjectQueue<EncounterUploadTask> delegate, Context context) {
        super(delegate);
        this.context = context;
    }

    public boolean start() {
        if (size() > 0) {
            context.startService(new Intent(context, EncounterTapeService.class));
            return true;
        }
        return false;
    }

    @Override
    public void add(EncounterUploadTask entry) {
        super.add(entry);
        start();
    }
}
