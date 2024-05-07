package social.entourage.android.tools.view

import android.annotation.TargetApi
import android.content.*
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.*
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_ON
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.databinding.FragmentWebviewBinding
import timber.log.Timber
import java.lang.reflect.Executable
import java.util.*

class WebViewFragment : BaseDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private lateinit var binding: FragmentWebviewBinding
    private lateinit var requestedUrl: String
    @IdRes private var shareMessageRes: Int = 0
    private var gestureDetectorCompat: GestureDetectorCompat? = null
    var bottomUpJumpAnimation: Animation? = null

    private var hasToSendRead = false

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentWebviewBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestedUrl = arguments?.getString(REQUESTED_URL) ?: return dismiss()
        shareMessageRes = arguments?.getInt(SHARE_MESSAGE, 0) ?: 0
        hasToSendRead = arguments?.getBoolean(SENDGETREAD, false) ?: false
        showAnimation()
        binding.webviewBackButton.setOnClickListener {onBackClicked()}
        binding.webviewMoreButton.setOnClickListener {toggleMenu()}
        binding.webviewBackground.setOnClickListener {dismiss()}
        binding.webviewMenuBrowser.setOnClickListener {onMenuBrowserClicked()}
        binding.webviewMenuCopy.setOnClickListener {onMenuCopyClicked()}
        binding.webviewMenuShare.setOnClickListener {onMenuShareClicked()}
        binding.webviewNavigationBarMenuBackground.setOnClickListener {toggleMenu()}

        if (hasToSendRead) {
            setWebUrlRead()
        }
    }

    override val slideStyle: Int
        get() = 0

    override fun dismiss() {
        binding.webview?.stopLoading()
        hideAnimation()
    }

    private  fun setWebUrlRead() {
        EntourageApplication.get().apiModule.homeRequest
            .markRecoWebUrlRead(requestedUrl).enqueue(object : Callback<okhttp3.ResponseBody> {
                override fun onResponse(
                    call: Call<okhttp3.ResponseBody>,
                    response: Response<okhttp3.ResponseBody>
                ) {
                }

                override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {

                }
            })
    }
    private fun showAnimation() {
        bottomUpJumpAnimation = AnimationUtils.loadAnimation(this.context, R.anim.bottom_up)
        bottomUpJumpAnimation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                initialiseView()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        binding.webviewAnimatedLayout?.startAnimation(bottomUpJumpAnimation)
    }

    private fun hideAnimation() {
        val bottomDownJumpAnimation = AnimationUtils.loadAnimation(this.context, R.anim.bottom_down)
        bottomDownJumpAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                super@WebViewFragment.dismiss()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        binding.webviewAnimatedLayout?.startAnimation(bottomDownJumpAnimation)
    }

    private fun initialiseView() {
        binding.webview?.settings?.javaScriptEnabled = true
        binding.webview?.settings?.domStorageEnabled = true
        binding.webview?.webViewClient = MyBrowser()
        binding.webview?.loadUrl(requestedUrl)

        // add a gesture detector to the navigation bar
        this.context?.let { context ->
            gestureDetectorCompat = GestureDetectorCompat(context, NavigationViewGestureListener())
            binding.webviewNavigationBar.setOnTouchListener { _, event ->
                if (gestureDetectorCompat?.onTouchEvent(event) == true || event.action != MotionEvent.ACTION_UP) true
                else onUp(event)
            }
        }
    }

    private fun onUp(event: MotionEvent): Boolean {
        binding.webviewAnimatedLayout?.translationY = 0f
        return true
    }

    // ----------------------------------
    // Click handling
    // ----------------------------------
    private fun onBackClicked() {
        if (binding.webview?.canGoBack() == true) {
            binding.webview?.goBack()
        } else {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val _model = ViewModelProvider(requireActivity()).get(
            CommunicationRecoWebUrlHandlerViewModel::class.java)
        _model.setValid()
    }

    private fun onMenuBrowserClicked() {
        binding.webview?.url?.let {
            val browseIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            try {
                startActivity(browseIntent)
            } catch (ex: Exception) {
                Toast.makeText(this.context, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
            }
            toggleMenu()
        }
    }

    private fun onMenuCopyClicked() {
        binding.webview?.url?.let {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(it, it)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, R.string.webview_copy_ok, Toast.LENGTH_SHORT).show()
            toggleMenu()
        }
    }

    private fun onMenuShareClicked() {
        binding.webview?.url?.let { url ->
            val sharingIntent = getSharingIntent(requireContext(), url, shareMessageRes)
            context?.startActivity(Intent.createChooser(sharingIntent, getString(R.string.entourage_share_intent_title)))
            toggleMenu()
        }
    }

    private fun toggleMenu() {
        binding.webviewNavigationBarMenu.visibility = if (binding.webviewNavigationBarMenu.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        binding.webviewNavigationBarMenuBackground.visibility = if (binding.webviewNavigationBarMenuBackground.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    // ----------------------------------
    // MyBrowser
    // ----------------------------------
    private inner class MyBrowser : WebViewClient() {
        private var loadedUrl = ""
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        //@ExperimentalStdlibApi
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (!loadedUrl.equals(url, ignoreCase = true)) {
                loadedUrl = url
                Uri.parse(url).host?.let { it->
                    var host = it.lowercase(Locale.ROOT)
                    if (host.startsWith("www.")) {
                        host = host.substring(4)
                    }
                    Log.wtf("wtf", "host: $host")
                    binding.webviewTitle.text = if (host.isNotEmpty()) host.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.ROOT
                        ) else it.toString()
                    } else ""

                } ?: run {
                    binding.webviewTitle.text = ""
                }
                binding.webviewProgressbar.visibility = View.VISIBLE
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            binding.webviewProgressbar.visibility = View.GONE
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            super.onReceivedError(view, request, error)
            Log.wtf("wtf", "error: ${error.description}")
            binding.webviewProgressbar?.visibility = View.GONE
        }

        //@SuppressWarnings("deprecation")
        @Deprecated("Deprecated in Java")
        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            binding.webviewProgressbar?.visibility = View.GONE
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
            super.onReceivedHttpError(view, request, errorResponse)
            binding.webviewProgressbar?.visibility = View.GONE
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            super.onReceivedSslError(view, handler, error)
            binding.webviewProgressbar?.visibility = View.GONE
        }
    }

    // ----------------------------------
    // Navigation gesture detector
    // ----------------------------------
    private inner class NavigationViewGestureListener : SimpleOnGestureListener() {
        private var handleFling = true
        override fun onDown(event: MotionEvent): Boolean {
            handleFling = true
            return true
        }

        override fun onFling(event1: MotionEvent?, event2: MotionEvent,
                             velocityX: Float, velocityY: Float): Boolean {
            // On fling down, dismiss the fragment
            if (event2.rawY - (event1?.rawY ?: 0.0f) > 0 && velocityY < 0 && handleFling) {
                dismiss()
                return true
            }
            return false
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            val translationY = binding.webviewAnimatedLayout?.translationY ?: return false
            val deltaY = e2.rawY - (e1?.rawY ?: 0.0f)
            if (deltaY > 0) {
                binding.webviewAnimatedLayout?.translationY = deltaY
            }
            if (translationY > deltaY) handleFling = false
            return translationY > deltaY
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        val TAG: String? = WebViewFragment::class.java.simpleName
        private const val REQUESTED_URL = "REQUESTED_URL"
        private const val SHARE_MESSAGE = "SHARE_MESSAGE"
        private const val SENDGETREAD = "sendRead"
        var customTabsPackages: ArrayList<ResolveInfo>? = null

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param requestedUrl Requested url as string.
         * @return A new instance of fragment WebViewFragment.
         */
        fun newInstance(requestedUrl: String?, @IdRes shareMessageRes: Int? = null, sendRead:Boolean): WebViewFragment {
            val fragment = WebViewFragment()
            val args = Bundle()
            args.putString(REQUESTED_URL, requestedUrl)
            args.putBoolean(SENDGETREAD,sendRead)
            shareMessageRes?.let {args.putInt(SHARE_MESSAGE, it) }
            fragment.arguments = args
            return fragment
        }

        /**
         * Returns a list of packages that support Custom Tabs.
         */
        fun getCustomTabsPackages(context: Context): ArrayList<ResolveInfo> {
            val pm = context.packageManager
            // Get default VIEW intent handler.
            val activityIntent = Intent()
                    .setAction(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.fromParts("http", "", null))

            // Get all apps that can handle VIEW intents.
            val packagesSupportingCustomTabs: ArrayList<ResolveInfo> = ArrayList()
            pm.queryIntentActivities(activityIntent, 0).forEach { info ->
                val serviceIntent = Intent().apply {
                    this.action = ACTION_CUSTOM_TABS_CONNECTION
                    this.setPackage(info.activityInfo.packageName)
                }
                // Check if this package also resolves the Custom Tabs service.
                if (pm.resolveService(serviceIntent, 0) != null) {
                    packagesSupportingCustomTabs.add(info)
                }
            }
            return packagesSupportingCustomTabs
        }

        private fun getSharingIntent(context: Context, url: String, shareMessageRes: Int): Intent {
            val shareString = if(shareMessageRes!=0) context.resources.getString(shareMessageRes, url) else  url
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareString)
            return sharingIntent
        }

        fun launchURL(context: Context, url: String, @IdRes shareMessageRes: Int = 0): Boolean {
            if(customTabsPackages == null) customTabsPackages = getCustomTabsPackages(context)
            if(customTabsPackages.isNullOrEmpty()) return false
            //Create a PendingIntent to your BroadCastReceiver implementation

            val schemeParams = CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(context.resources.getColor(R.color.accent))
                    .setSecondaryToolbarColor(context.resources.getColor(R.color.custom_button_accent_disabled))
                    .build()

            val customIntent = CustomTabsIntent.Builder()
                    .setStartAnimations(context, R.anim.bottom_up, R.anim.slide_out_to_right)
                    .setExitAnimations(context, R.anim.bottom_down, R.anim.slide_in_from_right)
                    .setDefaultColorSchemeParams(schemeParams)
                    .setShareState(SHARE_STATE_ON)
                    .build()

            try {
                customIntent.intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://" + context.packageName))
                customIntent.launchUrl(context, Uri.parse(url))
                return true
            }catch (e:Exception){
                //HERE Find true context


                return true
            }

        }
    }
}

class CommunicationRecoWebUrlHandlerViewModel : ViewModel() {
    val isValid:MutableLiveData<Boolean> = MutableLiveData()

    fun setValid() {
        isValid.value = true
    }
}