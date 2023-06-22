package social.entourage.android.base

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.deeplinks.UniversalLinkManager
import social.entourage.android.tools.view.WebViewFragment
import java.net.URL

/**
 * Base activity which set up a scoped graph and inject it
 */
abstract class BaseActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null
    val entApp: EntourageApplication?
        get()= (application as? EntourageApplication)
    private val universalLinkManager = UniversalLinkManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        entApp?.onActivityCreated(this)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        entApp?.onActivityDestroyed(this)
        super.onDestroy()
    }

    fun showWebView(url: String, shareMessageRes: Int = 0) {
        if(url.contains("www.entourage.social") || url.contains("preprod.entourage.social")){
            val uri = Uri.parse(url)
            universalLinkManager.handleUniversalLink(uri)
            return
        }
        if(shareMessageRes!=0 || !WebViewFragment.launchURL(this, url, shareMessageRes)) {
            WebViewFragment.newInstance(url, shareMessageRes, false)
                .show(supportFragmentManager, WebViewFragment.TAG)
        }
    }

    fun showWebViewForLinkId(linkId: String, shareMessageRes: Int = 0) {
        val link = getLink(linkId)
        showWebView(link, shareMessageRes)
    }

    open fun getLink(linkId: String): String {
        return getString(R.string.redirect_link_no_token_format, BuildConfig.ENTOURAGE_URL, linkId)
    }
}