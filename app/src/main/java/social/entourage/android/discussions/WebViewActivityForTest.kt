package social.entourage.android.discussions

import android.os.Bundle
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.WebviewActivitForTestBinding

class WebViewActivityForTest:BaseActivity() {

    private lateinit var binding:WebviewActivitForTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WebviewActivitForTestBinding.inflate(layoutInflater)
        setupWebView()
        loadUrl(EXTRA_URL)
        setContentView(binding.root)
    }

    private fun setupWebView() {
        // Enable JavaScript (if needed)
        binding.webview.settings.javaScriptEnabled = true
    }
    private fun loadUrl(url: String) {
        binding.webview.loadUrl(url)
    }

    companion object {
        var EXTRA_URL = ""
    }

    /*CODE TO PUT ON TO TEST WEBVIEW
    *   //TODO remove test Here test with launching WebViewActivityForTest activity
        WebViewActivityForTest.EXTRA_URL = url
        startActivity(Intent(context, WebViewActivityForTest::class.java))
        return
        *
    */

}