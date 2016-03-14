package social.entourage.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Base activity which set up a scoped graph and inject it
 */
public abstract class EntourageActivity extends AppCompatActivity {

    protected final String logTag = this.getClass().getSimpleName();

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupComponent(EntourageApplication.get(this).getEntourageComponent());
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

    protected void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    protected void setupComponent(EntourageComponent entourageComponent) {

    }

    protected void showKeyboard(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    protected void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
