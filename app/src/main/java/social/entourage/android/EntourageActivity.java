package social.entourage.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import social.entourage.android.webview.WebViewFragment;

/**
 * Base activity which set up a scoped graph and inject it
 */
public abstract class EntourageActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        EntourageApplication application = (EntourageApplication)getApplication();
        application.onActivityCreated(this);

        super.onCreate(savedInstanceState);
        setupComponent(EntourageApplication.get(this).getEntourageComponent());
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        EntourageApplication application = (EntourageApplication)getApplication();
        application.onActivityDestroyed(this);
        super.onDestroy();
    }

    public void showProgressDialog(int resId) {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setTitle(resId);
        } else {
            progressDialog = new ProgressDialog(this);
            if (resId != 0) {
                progressDialog.setTitle(resId);
            }
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
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

    protected void setupComponent(EntourageComponent entourageComponent) {

    }

    protected void showKeyboard(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm!=null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    protected void hideKeyboard() {
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null && imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showWebView(String url) {
        WebViewFragment webViewFragment = WebViewFragment.newInstance(url);
        webViewFragment.show(getSupportFragmentManager(), WebViewFragment.TAG);
    }

    public void showWebViewForLinkId(String linkId) {
        String link = getLink(linkId);
        showWebView(link);
    }

    public String getLink(String linkId) {
        return getString(R.string.redirect_link_no_token_format, BuildConfig.ENTOURAGE_URL, linkId);
    }
}
