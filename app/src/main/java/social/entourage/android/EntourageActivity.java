package social.entourage.android;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.octo.appaloosasdk.Appaloosa;

import java.util.List;

import dagger.ObjectGraph;

/**
 * Base activity which set up a scoped graph and inject it
 */
public abstract class EntourageActivity extends ActionBarActivity {

    private ObjectGraph activityGraph;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityGraph = EntourageApplication.get(this).getApplicationGraph().plus(getScopedModules().toArray());
        inject(this);

        if (BuildConfig.APPALOOSA_AUTO_UPDATE) {
            Appaloosa.getInstance().autoUpdate(
                    this,
                    BuildConfig.APPALOOSA_STORE_ID,
                    BuildConfig.APPALOOSA_STORE_TOKEN);
        }
    }

    @Override
    protected void onDestroy() {
        activityGraph = null;
        super.onDestroy();
    }

    public void inject(Object o) {
        activityGraph.inject(o);
    }

    protected abstract List<Object> getScopedModules();

    public void createEncounterFail(String msg) {
        Toast.makeText(this, getString(R.string.create_encounter_fail) + ": " + msg, Toast.LENGTH_SHORT).show();
    }

    public void createEncounterSuccess() {
        Toast.makeText(this, getString(R.string.create_encounter_success), Toast.LENGTH_SHORT).show();
        finish();
    }

}
