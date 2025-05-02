package social.entourage.android.user.edit.place

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.compat.ui.PlaceAutocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.api.model.User
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.base.location.LocationProvider
import social.entourage.android.base.location.LocationUtils
import social.entourage.android.databinding.FragmentSelectPlaceBinding
import timber.log.Timber
import java.io.IOException
import java.util.*

open class UserActionPlaceFragment : BaseDialogFragment() {
    private var _binding: FragmentSelectPlaceBinding? = null
    val binding: FragmentSelectPlaceBinding get() = _binding!!

    //Location
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            updateLocationText(locationResult.lastLocation)
        }
    }

    private var temporaryLocation: Location? = null
    private var temporaryAddressName: String? = null
    private var temporaryAddressPlace: User.Address? = null

    protected var userAddress: User.Address? = null

    protected var isSecondaryAddress = false
    protected var isSdf = false

    private val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if(permissions.entries.any {
                    it.value == true
                })  {
                    RefreshController.shouldRefreshLocationPermission = true
                    startRequestLocation()
            }
        }

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userAddress = it.getSerializable(ARG_PLACE) as? User.Address
            isSdf = it.getBoolean(ARG_SDF)
            isSecondaryAddress = it.getBoolean(ARG_2ND)
        }

        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelectPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    override fun onDetach() {
        super.onDetach()
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_LOCATION_RETURN) {
            when (resultCode) {
                AutocompleteActivity.RESULT_OK -> {
                    if (this.activity == null) return
                    val place = PlaceAutocomplete.getPlace(this.activity, intent)
                    if (place == null || place.address == null) return
                    var address = place.address.toString()
                    val lastCommaIndex = address.lastIndexOf(',')
                    if (lastCommaIndex > 0) {
                        //remove the last part, which is the country
                        address = address.substring(0, lastCommaIndex)
                    }
                    updateFromPlace(place.id, address, place.latLng)
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    if (this.activity == null) return
                    updateFromPlace(null, null,null)
                }
                AutocompleteActivity.RESULT_CANCELED -> {
                    updateFromPlace(null, null,null)
                }
            }
        }
    }

    //**********//**********//**********
    // Methods
    //**********//**********//**********

    open fun setupViews() {
        binding.uiOnboardBtLocation.setOnClickListener {
            onCurrentLocationClicked()
            binding.uiOnboardBtLocation.visibility = View.GONE
        }

        binding.uiOnboardPlaceTvLocation.setOnClickListener {
            onSearchCalled()
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
            binding.uiOnboardBtLocation.visibility = View.VISIBLE

        }

        if (userAddress != null) {
            binding.uiOnboardPlaceTvLocation.text = userAddress?.displayAddress
        } else {
            binding.uiOnboardPlaceTvLocation.text = ""
            binding.uiOnboardPlaceTvLocation.hint = getString(R.string.onboard_place_placeholder)
        }
    }

    open fun updateCallback() {
        userAddress = temporaryLocation?.let { tempLocation ->
                User.Address(tempLocation.latitude, tempLocation.longitude, temporaryAddressName)
            } ?: temporaryAddressPlace
    }

    //**********//**********//**********
    // Locations Methods
    //**********//**********//**********

    open fun onCurrentLocationClicked() {
        if (activity != null) {
            if (LocationUtils.isLocationPermissionGranted()) {
                startRequestLocation()
            } else {
                requestPermissionsLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    protected fun startRequestLocation() {
        if (LocationUtils.isLocationPermissionGranted()) {
            //if(mFusedLocationClient==null) {
                binding.uiOnboardPlaceTvLocation.text = ""
                binding.uiOnboardPlaceTvLocation.hint =
                    getString(R.string.onboard_place_getting_current_location)
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                mFusedLocationClient?.requestLocationUpdates(
                    LocationProvider.createLocationRequest(),
                    mLocationCallback,
                    null
                )
            /* else {
                if(mFusedLocationClient?.lastLocation?.isComplete == false) {
                    Toast.makeText(requireActivity(), "...", Toast.LENGTH_LONG).show()
                    Thread.sleep(15000)
                }
                if(mFusedLocationClient?.lastLocation?.isComplete == true) {
                    updateLocationText(mFusedLocationClient?.lastLocation?.result)
                } else {
                    Toast.makeText(requireActivity(), "En attente de localisation", Toast.LENGTH_LONG).show()
                }
            //}*/
        } else {
            Toast.makeText(requireActivity(), "Activez la localisation", Toast.LENGTH_LONG).show()
        }
    }

    fun updateLocationText(lastLocation: Location?) {
        binding.uiOnboardPlaceTvLocation.text = ""
        binding.uiOnboardPlaceTvLocation.hint = getString(R.string.onboard_place_placeholder)
        lastLocation?.let {
            activity?.let{ activity ->
                try {
                    temporaryLocation = lastLocation
                    temporaryAddressPlace = null
                    Geocoder(activity, Locale.getDefault()).getFromLocation(
                        it.latitude,
                        it.longitude,
                        1
                    )?.let { address ->
                        if (address.size > 0) {
                            val street = address[0].thoroughfare
                            val city = address[0].locality
                            val cp = address[0].postalCode

                            temporaryAddressName = "$city - $cp"
                            binding.uiOnboardPlaceTvLocation.text = temporaryAddressName
                        }
                    }
                } catch (e: IOException) {
                    Timber.e(e)
                }
            }
        }

        updateCallback()
    }

    //**********//**********//**********
    // Google place Methods
    //**********//**********//**********

    open fun onSearchCalled() {
        val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
            .build(requireActivity())
        startActivityForResult(intent, REQUEST_LOCATION_RETURN)
    }

    private fun updateFromPlace(placeId: String?, addressName: String?, latLng: com.google.android.gms.maps.model.LatLng?) {
        temporaryLocation = null
        if (placeId != null && addressName != null) {
            temporaryAddressPlace = User.Address(placeId)
            temporaryAddressPlace?.displayAddress = addressName
            binding.uiOnboardPlaceTvLocation.text = addressName
            latLng?.let {
                temporaryAddressPlace?.latitude = it.latitude
                temporaryAddressPlace?.longitude = it.longitude
            }
        } else {
            temporaryAddressPlace = null
            binding.uiOnboardPlaceTvLocation.text = ""
            binding.uiOnboardPlaceTvLocation.hint = getString(R.string.onboard_place_placeholder)
        }
        updateCallback()
    }

    companion object {
        const val ARG_PLACE = "place"
        const val ARG_SDF = "isSdf"
        const val ARG_2ND = "is2ndAddress"

        const val REQUEST_LOCATION_RETURN = 1010
    }
}