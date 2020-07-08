package social.entourage.android.onboarding

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.libraries.places.compat.ui.PlaceAutocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import kotlinx.android.synthetic.main.fragment_onboarding_place.*
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.location.LocationUtils.isLocationEnabled
import social.entourage.android.location.LocationUtils.isLocationPermissionGranted
import java.util.*

private const val ARG_PLACE = "place"
private const val ARG_SDF = "isSdf"
private const val ARG_2ND = "is2ndAddress"

open class OnboardingPlaceFragment : EntourageDialogFragment() {
    protected val PERMISSIONS_REQUEST_LOCATION = 1
    protected val REQUEST_LOCATION_RETURN = 1010

    //Location
    protected var mFusedLocationClient: FusedLocationProviderClient? = null
    protected val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            temporaryLocation = locationResult.lastLocation
            temporaryAddressPlace = null
            updateLocationText()
        }
    }

    protected var temporaryLocation:Location? = null
    protected var temporaryAddressName:String? = null
    protected var temporaryAddressPlace:User.Address? = null

    protected var userAddress:User.Address? = null
    protected var callback:OnboardingCallback? = null

    protected var isFromProfile = false
    protected var isSecondaryAddress = false
    protected var isSdf = false
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

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_place, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callback?.updateAddress(userAddress,isSecondaryAddress)

        setupViews()

        if (isFromProfile) {
            if (isSecondaryAddress) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_PROFILE_ACTION_ZONE2)
            }
            else {
                EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_PROFILE_ACTION_ZONE)
            }
        }
        else {
            if (!isSecondaryAddress) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_ONBOARDING_ACTION_ZONE)
                val _title = if (isSdf) R.string.onboard_place_title_sdf else R.string.onboard_place_title
                val _desc = if (isSdf) R.string.onboard_place_description_sdf else R.string.onboard_place_description

                ui_onboard_place_tv_title.text = getString(_title)
                ui_onboard_place_tv_info.text = getString(_desc)
            }
            else {
                EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_ONBOARDING_ACTION_ZONE2)
                val _title = if (isSdf) R.string.onboard_place_title2_sdf else R.string.onboard_place_title2
                val _desc = if (isSdf) R.string.onboard_place_description2_sdf else R.string.onboard_place_description2

                ui_onboard_place_tv_title.text = getString(_title)
                ui_onboard_place_tv_info.text = getString(_desc)
            }

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }

    //**********//**********//**********
    // Methods
    //**********//**********//**********

    protected fun setupViews() {
        ui_onboard_bt_location?.setOnClickListener {
            onCurrentLocationClicked()

            if (isFromProfile) {
                if (isSecondaryAddress) {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_PROFILE_SETACTION_ZONE2_GEOLOC)
                }
                else {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_PROFILE_SETACTION_ZONE_GEOLOC)
                }
            }
            else {
                if (isSecondaryAddress) {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_SETACTION_ZONE2_GEOLOC)
                }
                else {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_SETACTION_ZONE_GEOLOC)
                }
            }
        }

        ui_onboard_place_tv_location?.setOnClickListener {
            onSearchCalled()
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)

            if (isFromProfile) {
                if (isSecondaryAddress) {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_PROFILE_SETACTION_ZONE2_SEARCH)
                }
                else {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_PROFILE_SETACTION_ZONE_SEARCH)
                }
            }
            else {
                if (isSecondaryAddress) {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_SETACTION_ZONE2_SEARCH)
                }
                else {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_ONBOARDING_SETACTION_ZONE_SEARCH)
                }
            }
        }

        if (userAddress != null) {
            ui_onboard_place_tv_location?.text = userAddress?.displayAddress
        } else {
            ui_onboard_place_tv_location?.text = ""
            ui_onboard_place_tv_location?.hint = getString(R.string.onboard_place_placeholder)
        }
    }

    open fun updateCallback() {
        userAddress = null
        if (temporaryLocation != null) {
            userAddress = User.Address(temporaryLocation!!.latitude,temporaryLocation!!.longitude,temporaryAddressName)
        }
        else if (temporaryAddressPlace != null) {
            userAddress = temporaryAddressPlace!!
        }
        callback?.updateAddress(userAddress,isSecondaryAddress)
    }

    //**********//**********//**********
    // Locations Methods
    //**********//**********//**********

    fun onCurrentLocationClicked() {
        if (activity != null) {
            if (isLocationPermissionGranted()) {
                startRequestLocation()
            }
            else {
                requestPermissions(arrayOf(permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    protected fun startRequestLocation() {
        if (isLocationEnabled()) {
            val locationRequest = LocationRequest()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 60000
            locationRequest.fastestInterval = 60000
            locationRequest.numUpdates = 1

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            mFusedLocationClient?.requestLocationUpdates(
                    locationRequest, mLocationCallback,
                    Looper.myLooper()
            )
        }
        else {
            Toast.makeText(requireActivity(), "Activer la localisation", Toast.LENGTH_LONG).show()
//            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            startActivity(intent)
        }
    }

    fun updateLocationText() {
        val geocoder = Geocoder(activity, Locale.getDefault())
        val address = geocoder.getFromLocation(temporaryLocation!!.latitude,temporaryLocation!!.longitude,1)

        if (address.size > 0) {
            val street = address[0].thoroughfare
            val city = address[0].locality
            val cp = address[0].postalCode

            temporaryAddressName = "$street - $city - $cp"
            ui_onboard_place_tv_location?.text = temporaryAddressName
        }
        else {
            ui_onboard_place_tv_location?.text = ""
            ui_onboard_place_tv_location?.hint = getString(R.string.onboard_place_placeholder)
        }
        updateCallback()
    }

    //**********//**********//**********
    // Google place Methods
    //**********//**********//**********

    fun onSearchCalled() {

       val intent =  PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                .build(requireActivity())
        startActivityForResult(intent, REQUEST_LOCATION_RETURN)
    }

    fun updateFromPlace(placeId:String?,addressName:String?) {
        temporaryLocation = null
        if (placeId != null && addressName != null) {
            temporaryAddressPlace = User.Address(placeId)
            temporaryAddressPlace?.displayAddress = addressName
            ui_onboard_place_tv_location?.text = addressName
        }
        else {
            temporaryAddressPlace = null
            ui_onboard_place_tv_location?.text = ""
            ui_onboard_place_tv_location?.hint = getString(R.string.onboard_place_placeholder)
        }
        updateCallback()
    }

    //**********//**********//**********
    // Return methods
    //**********//**********//**********

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            for (index in permissions.indices) {
                if (permissions[index].equals(permission.ACCESS_FINE_LOCATION, ignoreCase = true)) {
                    val isGranted = grantResults[index] == PackageManager.PERMISSION_GRANTED

                    if (isGranted) {
                        startRequestLocation()
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

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
                    updateFromPlace(place.id,address)
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    if (this.activity == null) return
                    updateFromPlace(null,null)
                }
                AutocompleteActivity.RESULT_CANCELED -> {
                    updateFromPlace(null,null)
                }
            }
        }
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        @JvmStatic
        fun newInstance(googlePlaceAddress: User.Address?,is2ndAddress:Boolean,isSdf:Boolean) =
                OnboardingPlaceFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_PLACE, googlePlaceAddress)
                        putBoolean(ARG_SDF,isSdf)
                        putBoolean(ARG_2ND,is2ndAddress)
                    }
                }
    }
}