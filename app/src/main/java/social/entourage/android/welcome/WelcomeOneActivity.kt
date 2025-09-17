package social.entourage.android.welcome

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.text.style.TypefaceSpan
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityLayoutWelcomeOneBinding
import social.entourage.android.home.pedago.PedagoListActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import java.lang.ref.WeakReference

class WelcomeOneActivity:BaseActivity(), OnVideoLoaCallback {

    private lateinit var binding: ActivityLayoutWelcomeOneBinding
    //TODO: replace with a resource
    private var videoLink = "https://www.youtube.com/watch?v=IYUo5WAZxXs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.updateLanguage()
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutWelcomeOneBinding.inflate(layoutInflater)

        val webSettings = binding.wvContent.settings
        webSettings?.javaScriptEnabled = true
        binding.wvContent.webChromeClient = WebChrome(this)
        setVideo(videoLink)
        handleCloseButton()
        binding.wvContent.webViewClient = CustomWebViewClient(this)
        setTitle()
        onLinkClickedGoPedago()
        AnalyticsEvents.logEvent("View_WelcomeOfferHelp_Day1")

        setContentView(binding.root)
        updatePaddingTopForEdgeToEdge(binding.layoutContent)
    }

    fun setTitle(){
        val text = getString(R.string.welcome_one_title)
        val spannable = SpannableString(text)
        val wordToChangeFont = "vraiment"
        val start = text.indexOf(wordToChangeFont)
        val end = start + wordToChangeFont.length

        if (start != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val typeface = resources.getFont(R.font.quicksand_bold)
                val typefaceSpan = TypefaceSpan(typeface)
                spannable.setSpan(typefaceSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                val typefaceSpan = object : MetricAffectingSpan() {
                    private val typeface = Typeface.createFromAsset(assets, "quicksand_bold.ttf")

                    override fun updateMeasureState(textPaint: TextPaint) {
                        textPaint.typeface = typeface
                    }

                    override fun updateDrawState(tp: TextPaint) {
                        tp.typeface = typeface
                    }
                }
                spannable.setSpan(typefaceSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            binding.titleWelcomeOne.text = spannable
        } else {
            // Le mot "vraiment" n'est pas trouvé dans le texte, vous pourriez afficher un message d'erreur ici.
        }
    }

    fun onLinkClickedGoPedago(){
        AnalyticsEvents.logEvent("Action_WelcomeOfferHelp_Day1")
        binding.tvEndLine.setOnClickListener {
            finish()
            startActivityForResult(
                Intent(
                    applicationContext,
                    PedagoListActivity::class.java
                ), 0
            )
        }
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

    @Deprecated("Deprecated in kt 1.9.0")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        this.startActivity(intent)
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