package social.entourage.android.guide

import android.location.Location
import android.os.Bundle
import social.entourage.android.R
import social.entourage.android.base.BaseSecuredActivity
import social.entourage.android.databinding.ActivityGDSMainBinding
import timber.log.Timber

class GDSMainActivity : BaseSecuredActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding:ActivityGDSMainBinding = ActivityGDSMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.uiBtBack.setOnClickListener { onBackPressed() }
        val guideFg = GuideMapFragment()

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.ui_container, guideFg, GuideMapFragment.TAG)
        fragmentTransaction.commit()

        binding.uiBtSearch.setOnClickListener {
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
}