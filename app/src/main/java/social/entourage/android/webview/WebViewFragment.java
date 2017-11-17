package social.entourage.android.webview;


import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

    @BindView(R.id.webview_navigation_bar)
    View navigationView;

    @BindView(R.id.webview_title)
    TextView titleTextView;

    @BindView(R.id.webview)
    WebView webView;

    @BindView(R.id.webview_progressbar)
    ProgressBar progressBar;

    private String requestedUrl;

    private GestureDetectorCompat gestureDetectorCompat;

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

        // add a gesture detector to the navigation bar
        gestureDetectorCompat = new GestureDetectorCompat(this.getContext(), new NavigationViewGestureListener());
        navigationView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                gestureDetectorCompat.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    protected @StyleRes int getSlideStyle() {
        return R.style.CustomDialogFragmentSlide;
    }

    @Override
    public void dismiss() {
        webView.stopLoading();
        super.dismiss();
    }

    // ----------------------------------
    // Click handling
    // ----------------------------------

    @OnClick(R.id.webview_back_button)
    protected void onBackClicked() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            dismiss();
        }
    }

    @OnClick(R.id.webview_more_button)
    protected void onMoreClicked() {
        //TODO Toggle the menu view
    }

    @OnClick(R.id.webview_background)
    protected void onBackgroundClicked() {
        dismiss();
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

        @Override
        public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            String domain = "";
            if (host != null) {
                host = host.toLowerCase();
                if (host.startsWith("www.")) {
                    host = host.substring(4);
                }
                if (host.length() > 0) {
                    domain = host.substring(0, 1).toUpperCase() + host.substring(1);
                }
            }
            titleTextView.setText(domain);

            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(final WebView view, final WebResourceRequest request, final WebResourceError error) {
            super.onReceivedError(view, request, error);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedHttpError(final WebView view, final WebResourceRequest request, final WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedSslError(final WebView view, final SslErrorHandler handler, final SslError error) {
            super.onReceivedSslError(view, handler, error);
            progressBar.setVisibility(View.GONE);
        }
    }

    // ----------------------------------
    // Navigation gesture detector
    // ----------------------------------

    private class NavigationViewGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            // On fling down, dismiss the fragment
            if (event2.getAxisValue(MotionEvent.AXIS_Y) - event1.getAxisValue(MotionEvent.AXIS_Y) > 0) {
                dismiss();
            }
            return true;
        }
    }

}
