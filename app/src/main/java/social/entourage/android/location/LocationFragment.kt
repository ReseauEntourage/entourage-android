package social.entourage.android.location

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.compat.Place
import com.google.android.libraries.places.compat.ui.PlaceSelectionListener
import com.google.android.libraries.places.compat.ui.SupportPlaceAutocompleteFragment
import kotlinx.android.synthetic.main.fragment_entourage_location.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.EntourageSecuredActivity
import social.entourage.android.R
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.tools.BusProvider
import timber.log.Timber
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

/**
 * Fragment to choose the location of an entourage
 * Activities that contain this fragment must implement the
 * [LocationFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [LocationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LocationFragment  : EntourageDialogFragment() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private var originalLocation: LatLng? = null
    private var originalAddress: String? = null
    private var useGooglePlacesOnly = false
    private var location: LatLng? = null
    private var selectedPlace: Place? = null
    private var mListener: OnFragmentInteractionListener? = null
    private var mapFragment: SupportMapFragment? = null
    private var map: GoogleMap? = null
    private var autocompleteFragment: SupportPlaceAutocompleteFragment? = null
    private var geocoderAddressTask: GeocoderAddressTask? = null
    private var handler: Handler? = null
    private var timerTask: TimerTask? = null
    private var fromPlaceSelected = false
    private var pin: Marker? = null

    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            originalLocation = it.getParcelable(KEY_ENTOURAGE_LOCATION)
            originalLocation?.let { loc ->
                location = LatLng(loc.latitude, loc.longitude)
            }
            originalAddress = it.getString(KEY_ENTOURAGE_ADDRESS)
            useGooglePlacesOnly = it.getBoolean(KEY_USE_GOOGLE_PLACES_ONLY, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        val toReturn = inflater.inflate(R.layout.fragment_entourage_location, container, false)
        toReturn?.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                toReturn.let {
                    it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    dialog?.window?.let { window ->
                        val layoutParams = window.attributes
                        layoutParams.height = it.height
                        window.attributes = layoutParams
                    }
                }
            }
        })
        return toReturn
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
        cancelTimer()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            for (index in permissions.indices) {
                if (permissions[index].equals(permission.ACCESS_FINE_LOCATION, ignoreCase = true)) {
                    val isGranted = grantResults[index] == PackageManager.PERMISSION_GRANTED
                    BusProvider.instance.post(OnLocationPermissionGranted(isGranted))
                    try {
                        map?.isMyLocationEnabled = isGranted
                    } catch (ex: SecurityException) {
                        Timber.e(ex)
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // ----------------------------------
    // INTERFACE CALLBACKS
    // ----------------------------------
    fun onBackClicked() {
        dismiss()
    }

    fun onValidateClicked() {
        mListener?.onEntourageLocationChosen(location, entourage_location_address?.text.toString(), selectedPlace)
        cancelTimer()
        dismiss()
    }

    private fun onCurrentLocationClicked() {
        (activity as EntourageSecuredActivity?)?.let {
            if (LocationUtils.isLocationPermissionGranted()) {
                EntourageLocation.currentLocation?.let { currentLocation ->
                    map?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(currentLocation.latitude, currentLocation.longitude)))
                }
            } else {
                requestPermissions(arrayOf(permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_LOCATION)
            }
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun initializeView() {
        title_close_button.setOnClickListener { onBackClicked() }
        title_action_button.setOnClickListener { onValidateClicked() }
        entourage_location_current_position.setOnClickListener { onCurrentLocationClicked() }
        // Initialize map
        initializeMap()
        initializePlaces()
        initializeTimer()

        // Initialize address
        originalAddress?.let { setAddress(it, false) }
    }

    @SuppressLint("MissingPermission")
    private fun initializeMap() {
        if (mapFragment == null) {
            val googleMapOptions = GoogleMapOptions()
            googleMapOptions.zOrderOnTop(true)
            mapFragment = SupportMapFragment.newInstance(googleMapOptions)
        }
        mapFragment?.let { frag ->
            childFragmentManager.beginTransaction().replace(R.id.entourage_location_map_layout, frag).commit()
            frag.getMapAsync { googleMap ->
                if (activity != null) {
                    if (LocationUtils.isLocationPermissionGranted()) {
                        googleMap.isMyLocationEnabled = true
                    }
                }
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        activity, R.raw.map_styles_json))
                googleMap.uiSettings.isMyLocationButtonEnabled = false
                googleMap.uiSettings.isMapToolbarEnabled = false
                pin = if (originalLocation != null) {
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(originalLocation, 15f)
                    googleMap.moveCamera(cameraUpdate)
                    val markerOptions = MarkerOptions()
                            .position(originalLocation!!)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_orange))
                    googleMap.addMarker(markerOptions)
                } else {
                    val markerOptions = MarkerOptions()
                            .position(googleMap.cameraPosition.target)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_orange))
                    googleMap.addMarker(markerOptions)
                }
                googleMap.setOnCameraMoveStartedListener { i ->
                    if (i == OnCameraMoveStartedListener.REASON_GESTURE) {
                        cancelTimer()
                    }
                }
                googleMap.setOnCameraIdleListener {
                    googleMap.cameraPosition.target?.let { loc ->
                        location = loc
                        pin?.position = loc
                        //cancelling old ones if exists
                        geocoderAddressTask?.cancel(true)
                        //starts new task
                        GeocoderAddressTask(this@LocationFragment, fromPlaceSelected).let { task ->
                            geocoderAddressTask = task
                            task.execute(location)
                        }
                        fromPlaceSelected = false
                        autocompleteFragment?.setBoundsBias(LatLngBounds(
                                LatLng(loc.latitude - LOCATION_SEARCH_RADIUS, loc.longitude - LOCATION_SEARCH_RADIUS),
                                LatLng(loc.latitude + LOCATION_SEARCH_RADIUS, loc.longitude + LOCATION_SEARCH_RADIUS)
                        ))
                    }
                }
                googleMap.setOnMapClickListener { latLng ->
                    location = latLng
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    pin?.position = latLng
                    hideKeyboard()
                }
                map = googleMap
            }
        }
    }

    private fun initializePlaces() {
        if (autocompleteFragment == null) {
            autocompleteFragment = SupportPlaceAutocompleteFragment()
        }
        autocompleteFragment?.let { frag ->
            childFragmentManager.beginTransaction().replace(R.id.entourage_location_places, frag).commit()

            originalLocation?.let { loc ->
                frag.setBoundsBias(LatLngBounds(
                        LatLng(loc.latitude - LOCATION_SEARCH_RADIUS, loc.longitude - LOCATION_SEARCH_RADIUS),
                        LatLng(loc.latitude + LOCATION_SEARCH_RADIUS, loc.longitude + LOCATION_SEARCH_RADIUS)
                ))
            } ?: run  {
                frag.setBoundsBias(LatLngBounds(LatLng(42.0, -5.0), LatLng(51.0, 9.0)))
            }
            frag.setOnPlaceSelectedListener(object : PlaceSelectionListener {
                override fun onPlaceSelected(place: Place) {
                    place.latLng?.let { lat ->
                        fromPlaceSelected = true
                        map?.moveCamera(CameraUpdateFactory.newLatLng(lat))
                        pin?.position = lat
                        entourage_location_address?.text = place.address
                    }
                    selectedPlace = place
                }

                override fun onError(status: Status) {
                    activity?.let { Toast.makeText(it, R.string.entourage_location_address_not_found, Toast.LENGTH_SHORT).show() }
                }
            })
        }
    }

    private fun initializeTimer() {
        handler = Handler(Looper.getMainLooper())
        timerTask = object : TimerTask() {
            override fun run() {
                val autocompleteView = autocompleteFragment?.view ?: return
                val autocompleteEditText = autocompleteView.findViewById<TextView>(com.google.android.libraries.places.R.id.places_autocomplete_search_input) ?: return
                if (autocompleteEditText.text.isBlank()) return
                val autocompleteSearchView = autocompleteView.findViewById<View>(com.google.android.libraries.places.R.id.places_autocomplete_search_button)
                autocompleteSearchView?.performClick()
            }
        }
    }

    private fun cancelTimer() {
        timerTask?.let {handler?.removeCallbacks(it)}
    }

    private fun setAddress(address: String, fromPlaceSelected: Boolean) {
        entourage_location_address?.text = address
        if (useGooglePlacesOnly && !fromPlaceSelected) {
            autocompleteFragment?.view?.findViewById<TextView>(com.google.android.libraries.places.R.id.places_autocomplete_search_input)?.let { autocompleteEditText ->
                cancelTimer()
                timerTask?.let { handler?.postDelayed(it, SEARCH_DELAY)}
                autocompleteEditText.text = address
            }
        }
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    private class GeocoderAddressTask(locationFragment: LocationFragment, var fromPlaceSelected: Boolean) : AsyncTask<LatLng?, Void?, String?>() {
        var locationFragmentWeakReference: WeakReference<LocationFragment>? = WeakReference(locationFragment)
        override fun doInBackground(vararg params: LatLng?): String? {
            try {
                params[0]?.let { location ->
                    locationFragmentWeakReference?.get()?.activity?.let { activity ->
                        val addresses = Geocoder(activity, Locale.getDefault())
                                .getFromLocation(location.latitude, location.longitude, 1)
                        if (addresses.size > 0) {
                            addresses?.first()?.let { address ->
                                if (address.maxAddressLineIndex >= 0) {
                                    return address.getAddressLine(0)
                                }
                            }
                        }
                        return ""
                    }
                }
            } catch (ignored: IOException) {
            }
            return null
        }

        override fun onPostExecute(address: String?) {
            if (address == null) return
            locationFragmentWeakReference?.get()?.let { locationFragment ->
                if (locationFragment.geocoderAddressTask !== this) return
                locationFragment.setAddress(address, fromPlaceSelected)
                locationFragment.geocoderAddressTask = null
            }
        }
    }

    interface OnFragmentInteractionListener {
        fun onEntourageLocationChosen(location: LatLng?, address: String?, place: Place?)
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        const val TAG = "social.entourage.android.entourage.location"
        private const val KEY_ENTOURAGE_LOCATION = "social.entourage.android.KEY_ENTOURAGE_LOCATION"
        private const val KEY_ENTOURAGE_ADDRESS = "social.entourage.android.KEY_ENTOURAGE_ADDRESS"
        private const val KEY_USE_GOOGLE_PLACES_ONLY = "social.entourage.android.KEY_USE_GOOGLE_PLACES_ONLY"
        private const val LOCATION_MOVE_DELTA = 50f //meters
        private const val LOCATION_SEARCH_RADIUS = 0.18f // 20 kilometers in lat/long degrees
        private const val PERMISSIONS_REQUEST_LOCATION = 1
        private const val SEARCH_DELAY = 1000L

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param location The initial location.
         * @param address The initial address as string
         * @param listener The listener
         * @return A new instance of fragment LocationFragment.
         */
        fun newInstance(location: LatLng?, address: String?, listener: OnFragmentInteractionListener?): LocationFragment {
            val args = Bundle()
            args.putParcelable(KEY_ENTOURAGE_LOCATION, location)
            args.putString(KEY_ENTOURAGE_ADDRESS, address)
            val fragment = LocationFragment()
            fragment.mListener = listener
            fragment.arguments = args
            return fragment
        }

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param location The initial location.
         * @param address The initial address as string
         * @param useGooglePlacesOnly true if the fragment will use only Google Places to fetch an address
         * @param listener The listener
         * @return A new instance of fragment LocationFragment.
         */
        @JvmStatic
        fun newInstance(location: LatLng?, address: String?, useGooglePlacesOnly: Boolean, listener: OnFragmentInteractionListener?): LocationFragment {
            val args = Bundle()
            args.putParcelable(KEY_ENTOURAGE_LOCATION, location)
            args.putString(KEY_ENTOURAGE_ADDRESS, address)
            args.putBoolean(KEY_USE_GOOGLE_PLACES_ONLY, useGooglePlacesOnly)
            val fragment = LocationFragment()
            fragment.mListener = listener
            fragment.arguments = args
            return fragment
        }
    }
}