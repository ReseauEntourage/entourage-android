package social.entourage.android.guide

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_g_d_s_main.*
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

        ui_bt_search?.setOnClickListener {
            val distance: Float
            if (guideFg.map != null) {
                val region = guideFg.map!!.projection.visibleRegion
                val result = floatArrayOf(0f)
                Location.distanceBetween(region.farLeft.latitude, region.farLeft.longitude, region.nearLeft.latitude, region.nearLeft.longitude, result)
                distance = result[0] / 1000.0f

              val cameraPosition =  guideFg.map!!.cameraPosition

                GDSSearchFragment.newInstance(cameraPosition.target.latitude,cameraPosition.target.longitude,distance.toDouble()).show(supportFragmentManager, GDSSearchFragment.TAG)
            } else {
                Timber.w("no map available for updating Guide")
            }
        }
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