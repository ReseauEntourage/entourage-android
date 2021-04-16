package social.entourage.android.guide

import android.location.Location
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_g_d_s_main.*
import social.entourage.android.*
import social.entourage.android.base.BaseSecuredActivity
import timber.log.Timber

class GDSMainActivity : BaseSecuredActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_g_d_s_main)

        ui_bt_back?.setOnClickListener { onBackPressed() }
        val guideFg = GuideMapFragment()

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.ui_container, guideFg, GuideMapFragment.TAG)
        fragmentTransaction.commit()

        ui_bt_search?.setOnClickListener {
            guideFg.map?.let {map ->
                val region = map.projection.visibleRegion
                val result = floatArrayOf(0f)
                Location.distanceBetween(region.farLeft.latitude, region.farLeft.longitude, region.nearLeft.latitude, region.nearLeft.longitude, result)
                val distance: Float = result[0] / 1000.0f

                val cameraPosition =  map.cameraPosition

                GDSSearchFragment.newInstance(cameraPosition.target.latitude,cameraPosition.target.longitude,distance.toDouble()).show(supportFragmentManager, GDSSearchFragment.TAG)
            } ?: run {
                Timber.w("no map available for updating Guide")
            }
        }
    }

    override fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerGDSMainComponent.builder()
                .entourageComponent(entourageComponent)
                .build()
                .inject(this)
    }
}