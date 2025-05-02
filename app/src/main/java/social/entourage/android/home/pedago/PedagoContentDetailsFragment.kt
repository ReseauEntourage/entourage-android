package social.entourage.android.home.pedago

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import social.entourage.android.R
import social.entourage.android.api.model.Pedago
import social.entourage.android.databinding.NewFragmentPedagoContentDetailsBinding
import social.entourage.android.home.HomePresenter
import java.lang.ref.WeakReference

class PedagoContentDetailsFragment : Fragment() {

    private var _binding: NewFragmentPedagoContentDetailsBinding? = null
    val binding: NewFragmentPedagoContentDetailsBinding get() = _binding!!

    private val homePresenter: HomePresenter by lazy { HomePresenter() }

    private val args: PedagoContentDetailsFragmentArgs by navArgs()

    private var isFromNotifs = false
    private var htmlContent:String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentPedagoContentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackButton()
        isFromNotifs = args.isFromNotif
        htmlContent = args.htmlContent

        setView()
        homePresenter.pedagolSingle.observe(requireActivity(), ::updateContent)
        homePresenter.getPedagogicalResource(args.id)
    }

    private fun updateContent(pedago: Pedago) {
        pedago.html?.let {
            htmlContent = it
            setView()
        }
    }

    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            if(isFromNotifs) {
                activity?.onBackPressed()
                return@setOnClickListener
            }
            findNavController().popBackStack()
        }
    }

    private fun setView() {
        if (isAdded) {
            binding.header.headerTitle.text = getString(R.string.pedago_details)
            with(binding.content) {
                webViewClient = WebViewClient()
                webChromeClient = WebChrome(requireActivity())
                setBackgroundColor(Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                loadDataWithBaseURL(
                    null, htmlContent, "text/html", "utf-8", null
                )
            }
        }
    }

    // WebChromeClient used to enable full screen for the embedded youtube video
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