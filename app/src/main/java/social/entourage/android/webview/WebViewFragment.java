package social.entourage.android.webview;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;

/**
 *
 */

public class WebViewFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = WebViewFragment.class.getSimpleName();

    private static final String REQUESTED_URL = "REQUESTED_URL";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.webview)
    WebView webView;

    private String requestedUrl;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public WebViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param requestedUrl Requested url as string.
     * @return A new instance of fragment WebViewFragment.
     */
    public static WebViewFragment newInstance(String requestedUrl) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(REQUESTED_URL, requestedUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            requestedUrl = getArguments().getString(REQUESTED_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_webview, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyBrowser());
        webView.loadUrl(requestedUrl);
    }

    // ----------------------------------
    // MyBrowser
    // ----------------------------------

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
