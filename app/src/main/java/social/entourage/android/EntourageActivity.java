package social.entourage.android;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.octo.appaloosasdk.Appaloosa;

import java.util.List;

import dagger.ObjectGraph;
import social.entourage.android.common.Constants;

/**
 * Base activity which set up a scoped graph and inject it
 */
public abstract class EntourageActivity extends ActionBarActivity {

    protected final String logTag = this.getClass().getSimpleName();

    private ObjectGraph activityGraph;

    private ProgressDialog progressDialog;

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

    public void showProgressDialog(int resId) {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setTitle(resId);
        } else {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(resId);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }
    }

    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    protected abstract List<Object> getScopedModules();

    public String getLogTag() {
        return logTag;
    }
}
