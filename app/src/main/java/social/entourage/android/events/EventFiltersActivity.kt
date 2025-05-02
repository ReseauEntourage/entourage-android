package social.entourage.android.events

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.compat.ui.PlaceAutocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.api.model.Address
import social.entourage.android.api.model.EventActionLocationFilters
import social.entourage.android.api.model.EventFilterType
import social.entourage.android.base.location.LocationProvider
import social.entourage.android.base.location.LocationUtils
import social.entourage.android.databinding.ActivityEventFiltersBinding
import social.entourage.android.events.list.DiscoverEventsListFragment
import timber.log.Timber
import java.io.IOException
import java.util.Locale

class EventFiltersActivity : AppCompatActivity() {

    //Location
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            updateLocationText(locationResult.lastLocation)
        }
    }
    private val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if(permissions.entries.any {
                    it.value
                })  {
                RefreshController.shouldRefreshLocationPermission = true
                startRequestLocation()
            }
        }

    private val paddingRight = 20
    private val paddingRightLimit = 60
    private val progressLimit = 96

    private var currentFilters: EventActionLocationFilters? = null

    private lateinit var binding: ActivityEventFiltersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEventFiltersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentFilters = intent.getSerializableExtra(FILTERS) as? EventActionLocationFilters

        setupLocationChoice()
        initializeSeekBar()
        setupViews()
        setBackButton()
    }

    override fun onDestroy() {
        cancelGps()
        super.onDestroy()
    }

    private fun setupViews() {
        binding.seekbar.progress = currentFilters?.travel_distance() ?: 0
        binding.validate.button.setOnClickListener {
            onSaveFilters()
        }

        binding.seekbar.post {
            currentFilters?.travel_distance()?.let { setProgressThumb(it) }
        }

        binding.layoutPlace.setOnClickListener {
            onPlaceSearch()
        }
    }

    private fun onSaveFilters() {
        currentFilters?.modifyRadius(binding.seekbar.progress)

        if (currentFilters?.validate() == true) {
            binding.errorView.visibility = View.GONE
            val intent = Intent(this, DiscoverEventsListFragment::class.java)
            intent.putExtra(FILTERS, currentFilters)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        else {
            binding.errorView.visibility = View.VISIBLE
        }
    }

    private fun setupLocationChoice() {
        binding.errorView.visibility = View.INVISIBLE
        binding.layoutCustom.visibility = View.GONE
        binding.layoutPlace.visibility = View.GONE
        binding.layoutMe.visibility = View.GONE

        when(currentFilters?.filterType()) {
            EventFilterType.PROFILE -> {
                binding.typeChoice.check(R.id.profile_address)
                binding.addressName.text = currentFilters?.addressName()
                binding.layoutMe.visibility = View.VISIBLE
            }
            EventFilterType.GOOGLE -> {
                binding.typeChoice.check(R.id.place)
                binding.layoutPlace.visibility = View.VISIBLE
                binding.placeName.text = currentFilters?.addressName()
            }
            else -> {
                binding.typeChoice.check(R.id.gps)
                binding.layoutCustom.visibility = View.VISIBLE
                binding.locationName.text = currentFilters?.addressName()
            }
        }

        binding.typeChoice.setOnCheckedChangeListener { _, checkedId ->
            binding.errorView.visibility = View.GONE
            when (checkedId) {
                R.id.profile_address -> {
                    val myAddress = EntourageApplication.get().me()?.address
                    val address = Address(myAddress?.latitude ?: 0.0,myAddress?.longitude ?: 0.0,myAddress?.displayAddress ?: "-")
                    currentFilters?.modifyFilter(myAddress?.displayAddress,address,currentFilters?.travel_distance(),
                        EventFilterType.PROFILE)
                    binding.layoutCustom.visibility = View.GONE
                    binding.layoutPlace.visibility = View.GONE
                    binding.layoutMe.visibility = View.VISIBLE
                    binding.addressName.text = myAddress?.displayAddress
                    cancelGps()
                }
                R.id.place -> {
                    currentFilters?.modifyFilter(null,null,currentFilters?.travel_distance(),
                        EventFilterType.GOOGLE)
                    binding.layoutPlace.visibility = View.VISIBLE
                    binding.layoutCustom.visibility = View.GONE
                    binding.layoutMe.visibility = View.GONE
                    binding.placeName.text = getString(R.string.event_filter_google_placeholder)
                    cancelGps()
                }
                R.id.gps -> {
                    currentFilters?.modifyFilter(null,null,currentFilters?.travel_distance(),
                        EventFilterType.GPS)
                    binding.layoutCustom.visibility = View.VISIBLE
                    binding.layoutPlace.visibility = View.GONE
                    binding.layoutMe.visibility = View.GONE
                    binding.locationName.text = getString(R.string.event_filter_position_placeholder)
                    onCurrentLocationClicked()
                }
            }
        }
    }

    private fun cancelGps() {
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }

    /*
     * Thumbbar
     */
    private fun setProgressThumb(progress: Int) {
        binding.tvTrickleIndicator.text =
            String.format(
                getString(R.string.progress_km),
                progress.toString()
            )
        val bounds: Rect = binding.seekbar.thumb.dirtyBounds
        val paddingRight = if (progress > progressLimit) paddingRightLimit else paddingRight
        binding.tvTrickleIndicator.x =
            (binding.seekbar.left + bounds.left - paddingRight).toFloat()
    }

    private fun initializeSeekBar() {
        binding.seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressValue = if (progress == 0) 1 else progress
                setProgressThumb(progressValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    //Google Place
    private fun onPlaceSearch() {
        //val filter: AutocompleteFilter = AutocompleteFilter.Builder().setCountry("FR").build()
        val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
            .build(this)
        resultLauncher.launch(intent)
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data

        when (result.resultCode) {
            AutocompleteActivity.RESULT_OK -> {
                val place = PlaceAutocomplete.getPlace(this, data)
                if (place == null || place.address == null) return@registerForActivityResult
                var addressStr = place.address.toString()
                val lastCommaIndex = addressStr.lastIndexOf(',')
                if (lastCommaIndex > 0) {
                    //remove the last part, which is the country
                    addressStr = addressStr.substring(0, lastCommaIndex)
                }

                val address = Address(place.latLng.latitude,place.latLng.longitude,addressStr)
                currentFilters?.modifyAddress(address)
                currentFilters?.modifiyShortname(addressStr)
                binding.placeName.text = addressStr

                getCityNameFromPlace(place.latLng)
            }
            AutocompleteActivity.RESULT_ERROR -> {
                clearFilterFromPlace()
            }
            AutocompleteActivity.RESULT_CANCELED -> {
                clearFilterFromPlace()
            }
        }
    }

    private fun getCityNameFromPlace(latlng: LatLng) {
        this.let{ activity ->
            try {
                Geocoder(activity, Locale.getDefault()).getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1
                )?.let { address ->
                    if (address.size > 0) {
                        val city = address[0].locality
                        val cp = address[0].postalCode

                        currentFilters?.modifiyShortname("$city, $cp")
                    }
                    else {
                        clearFilterFromPlace()
                    }
                }
            } catch (e: IOException) {
                Timber.e(e)
                clearFilterFromPlace()
            }
        }
    }

    private fun clearFilterFromPlace() {
            currentFilters?.modifyAddress(null)
            currentFilters?.modifiyShortname(null)
            binding.placeName.text = getString(R.string.onboard_place_placeholder)
    }

    //Location
    private fun onCurrentLocationClicked() {
        if (LocationUtils.isLocationPermissionGranted()) {
            startRequestLocation()
        } else {
            requestPermissionsLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    fun updateLocationText(lastLocation: Location?) {
        binding.placeName.text = ""
        binding.placeName.hint = getString(R.string.onboard_place_placeholder)
        lastLocation?.let {
            this.let{ activity ->
                try {
                    Geocoder(activity, Locale.getDefault()).getFromLocation(
                        it.latitude,
                        it.longitude,
                        1
                    )?.let { address ->
                        if (address.size > 0) {
                            var street = address[0].thoroughfare
                            val city = address[0].locality
                            val cp = address[0].postalCode

                            street = if (street == null) "" else "$street, "

                            val addressName = "$street$city, $cp"
                            binding.locationName.text = addressName
                            val fullAddress = Address(it.latitude,it.longitude,addressName)
                            currentFilters?.modifyAddress(fullAddress)
                            currentFilters?.modifiyShortname("$city, $cp")
                            binding.errorView.visibility = View.INVISIBLE
                        }
                    }
                } catch (e: IOException) {
                    Timber.e(e)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startRequestLocation() {
        if (LocationUtils.isLocationPermissionGranted()) {
            binding.locationName.text =  getString(R.string.onboard_place_getting_current_location)
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            mFusedLocationClient?.requestLocationUpdates(
                LocationProvider.createLocationRequest(),
                mLocationCallback,
                null
            )
        } else {
            Toast.makeText(this, getString(R.string.activate_geoloc), Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val FILTERS = "filters"
    }
}