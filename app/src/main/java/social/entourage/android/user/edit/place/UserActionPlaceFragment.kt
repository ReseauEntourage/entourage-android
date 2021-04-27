package social.entourage.android.user.edit.place

import android.Manifest
import android.annotation.SuppressLint
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
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.location.LocationUtils
import timber.log.Timber
import java.io.IOException
import java.util.*

open class UserActionPlaceFragment : BaseDialogFragment() {
    //Location
    protected var mFusedLocationClient: FusedLocationProviderClient? = null
    protected val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            temporaryLocation = locationResult.lastLocation
            temporaryAddressPlace = null
            updateLocationText()
        }
    }

    protected var temporaryLocation: Location? = null
    protected var temporaryAddressName:String? = null
    protected var temporaryAddressPlace: User.Address? = null

    protected var userAddress: User.Address? = null

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

        setupViews()
    }

    override fun onDetach() {
        super.onDetach()
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }

    //**********//**********//**********
    // Methods
    //**********//**********//**********

    open fun setupViews() {
        ui_onboard_bt_location?.setOnClickListener {
            onCurrentLocationClicked()
        }

        ui_onboard_place_tv_location?.setOnClickListener {
            onSearchCalled()
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)

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
        temporaryLocation?.let { tempLocation ->
            userAddress = User.Address(tempLocation.latitude, tempLocation.longitude, temporaryAddressName)
        } ?: run {
            temporaryAddressPlace?.let { tempAddressPlace ->
                userAddress = tempAddressPlace
            }
        }
    }

    //**********//**********//**********
    // Locations Methods
    //**********//**********//**********

    open fun onCurrentLocationClicked() {
        if (activity != null) {
            if (LocationUtils.isLocationPermissionGranted()) {
                startRequestLocation()
            }
            else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    protected fun startRequestLocation() {
        if (LocationUtils.isLocationEnabled()) {
            val locationRequest = LocationRequest()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 60000
            locationRequest.fastestInterval = 60000
            locationRequest.numUpdates = 1

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            ui_onboard_place_tv_location?.text = ""
            ui_onboard_place_tv_location?.hint = getString(R.string.onboard_place_getting_current_location)
            mFusedLocationClient?.requestLocationUpdates(
                    locationRequest, mLocationCallback,
                    Looper.myLooper()
            )
        }
        else {
            Toast.makeText(requireActivity(), "Activez la localisation", Toast.LENGTH_LONG).show()
//            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            startActivity(intent)
        }
    }

    fun updateLocationText() {
        ui_onboard_place_tv_location?.text = ""
        ui_onboard_place_tv_location?.hint = getString(R.string.onboard_place_placeholder)
        temporaryLocation?.let {
            try {
                val address = Geocoder(activity, Locale.getDefault()).getFromLocation(it.latitude,it.longitude,1)
                if (address != null && address.size > 0) {
                    val street = address[0].thoroughfare
                    val city = address[0].locality
                    val cp = address[0].postalCode

                    temporaryAddressName = "$street - $city - $cp"
                    ui_onboard_place_tv_location?.text = temporaryAddressName
                }
            } catch (e: IOException) {
                Timber.e(e)
            }
        }

        updateCallback()
    }

    //**********//**********//**********
    // Google place Methods
    //**********//**********//**********

    open fun onSearchCalled() {
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
                if (permissions[index].equals(Manifest.permission.ACCESS_FINE_LOCATION, ignoreCase = true)) {
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

    companion object {
        const val ARG_PLACE = "place"
        const val ARG_SDF = "isSdf"
        const val ARG_2ND = "is2ndAddress"

        const  val PERMISSIONS_REQUEST_LOCATION = 1
        const  val REQUEST_LOCATION_RETURN = 1010
    }
}