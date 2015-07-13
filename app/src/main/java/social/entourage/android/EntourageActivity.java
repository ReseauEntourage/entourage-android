package social.entourage.android;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.octo.appaloosasdk.Appaloosa;

/**
 * Base activity which set up a scoped graph and inject it
 */
public class EntourageActivity extends ActionBarActivity {

    protected final String logTag = this.getClass().getSimpleName();

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupComponent(EntourageApplication.get(this).getEntourageComponent());

        if (BuildConfig.APPALOOSA_AUTO_UPDATE) {
            Appaloosa.getInstance().autoUpdate(
                    this,
                    BuildConfig.APPALOOSA_STORE_ID,
                    BuildConfig.APPALOOSA_STORE_TOKEN);
        }
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

    public String getLogTag() {
        return logTag;
    }

    protected void setupComponent(EntourageComponent entourageComponent) {

    }
}
