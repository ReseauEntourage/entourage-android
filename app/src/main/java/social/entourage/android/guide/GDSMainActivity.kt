package social.entourage.android.guide

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_g_d_s_main.*
import kotlinx.android.synthetic.main.fragment_guide_map.*
import social.entourage.android.BuildConfig
import social.entourage.android.R
import social.entourage.android.tools.view.WebViewFragment
import timber.log.Timber

class GDSMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_g_d_s_main)


        ui_bt_back?.setOnClickListener { onBackPressed() }
       val guideFg = GuideMapFragment()

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.ui_container, guideFg, GuideMapFragment.TAG)
       // fragmentTransaction.addToBackStack(GuideMapFragment.TAG)
        fragmentTransaction.commit()

    }

    override fun onBackPressed() {
        Timber.d("On backPressed **********")
        super.onBackPressed()
    }

    fun showWebView(url: String?) {
        val webViewFragment = WebViewFragment.newInstance(url)
        webViewFragment.show(supportFragmentManager, WebViewFragment.TAG)
    }

    fun showWebViewForLinkId(linkId: String) {
        val link = getLink(linkId)
        showWebView(link)
    }

    fun getLink(linkId: String): String {
        return getString(R.string.redirect_link_no_token_format, BuildConfig.ENTOURAGE_URL, linkId)
    }
}