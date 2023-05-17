package social.entourage.android.welcome

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebViewClient
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityLayoutWelcomeOneBinding
import social.entourage.android.home.pedago.PedagoContentDetailsFragment
import android.webkit.WebView
import java.lang.ref.WeakReference

class WelcomeOneActivity:BaseActivity(), OnVideoLoaCallback {

    private lateinit var binding: ActivityLayoutWelcomeOneBinding
    private var videoLink = "https://www.youtube.com/watch?v=IYUo5WAZxXs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutWelcomeOneBinding.inflate(layoutInflater)

        val webSettings = binding.wvContent.settings
        webSettings?.javaScriptEnabled = true
        binding.wvContent.webChromeClient = WebChrome(this)
        setVideo(videoLink)
        handleCloseButton()
        binding.wvContent.webViewClient = CustomWebViewClient(this)
        setContentView(binding.root)
    }



    fun formatYoutubeUrl(url: String): String {
        return url.replace("watch?v=", "embed/")
    }

    private fun setVideo(videoUrl: String) {
        val formattedUrl = formatYoutubeUrl(videoUrl)
        binding.wvContent.loadUrl(formattedUrl)
    }

    fun handleCloseButton(){
        binding.closeButton.setOnClickListener {
            this.onBackPressed()
        }
    }


    override fun videoLoaded() {
        binding.progressBar.visibility = View.GONE
    }

    class WebChrome(activity: Activity) : WebChromeClient() {

        private val activityRef = WeakReference(activity)

        private var customView: View? = null
        private var customViewCallback: CustomViewCallback? = null

        private var originalOrientation = 0
        private var originalSystemUiVisibility = 0

        override fun getDefaultVideoPoster(): Bitmap? {
            return activityRef.get()?.run {
                BitmapFactory.decodeResource(applicationContext.resources, 2130837573)
            }
        }

        override fun onHideCustomView() {
            activityRef.get()?.run {
                (window.decorView as ViewGroup).removeView(customView)
                customView = null
                window.decorView.systemUiVisibility = originalSystemUiVisibility
                requestedOrientation = originalOrientation
            }
            customViewCallback?.onCustomViewHidden()
            customViewCallback = null
        }

        override fun onShowCustomView(view: View?, viewCallback: CustomViewCallback?) {
            if (customView != null) {
                onHideCustomView()
                return
            }
            customView = view
            activityRef.get()?.run {
                originalSystemUiVisibility = window.decorView.systemUiVisibility
                originalOrientation = requestedOrientation
                customViewCallback = viewCallback
                (window.decorView as ViewGroup).addView(
                    customView,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                window.decorView.systemUiVisibility = 3846
            }
        }
    }
}

class CustomWebViewClient(var callback:OnVideoLoaCallback) : WebViewClient() {


    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        callback.videoLoaded()

    }
}

interface OnVideoLoaCallback{
    fun videoLoaded()
}