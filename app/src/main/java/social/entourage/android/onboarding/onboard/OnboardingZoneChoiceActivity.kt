package social.entourage.android.onboarding.onboard

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlin.math.cos
import social.entourage.android.R
import social.entourage.android.databinding.ActivityOnboardingZoneChoiceBinding
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge

class OnboardingZoneChoiceActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityOnboardingZoneChoiceBinding
    private lateinit var placesClient: PlacesClient
    private var map: GoogleMap? = null

    private var predictions: List<AutocompletePrediction> = emptyList()

    private var marker: Marker? = null
    private var circle: Circle? = null
    private var currentLatLng: LatLng? = null
    private var radiusKm: Int = 20 // valeur par défaut

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingZoneChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Places
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_api_key))
        }
        placesClient = Places.createClient(this)

        // MapFragment -> utilise bien l'id de TON layout: map_fragment
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_fragment, mapFragment)
            .commit()
        mapFragment.getMapAsync(this)

        setupUi()
        setupAutocomplete()
        setupSeekbar()
        setupButtons()
    }

    // ---------------- MAP ----------------
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap.apply {
            uiSettings.isMapToolbarEnabled = false
            uiSettings.isMyLocationButtonEnabled = false
        }
        val france = LatLng(46.7111, 1.7191)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(france, 5.5f))
        updateRadiusLabel()
    }

    private fun hideKeyboardAndClearFocus() {
        val actv = binding.autoCompleteCityName as AutoCompleteTextView
        actv.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(actv.windowToken, 0)
    }

    private fun placeMarkerAndCircle(position: LatLng, title: String?) {
        val m = map ?: return

        // Marker
        if (marker == null) {
            marker = m.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(title ?: "Localisation")
            )
        } else {
            marker?.position = position
            marker?.title = title
        }

        // Circle
        val radiusMeters = (radiusKm * 1000).toDouble()
        if (circle == null) {
            circle = m.addCircle(
                CircleOptions()
                    .center(position)
                    .radius(radiusMeters)
                    .strokeWidth(0f)
                    .fillColor(0x55FF7F00.toInt()) // orange translucide
            )
        } else {
            circle?.center = position
            circle?.radius = radiusMeters
        }

        currentLatLng = position
        zoomToCircle(position, radiusMeters)
    }

    private fun updateCircleRadius() {
        val pos = currentLatLng ?: return
        val radiusMeters = (radiusKm * 1000).toDouble()
        circle?.radius = radiusMeters
        zoomToCircle(pos, radiusMeters)
    }

    private fun zoomToCircle(center: LatLng, radiusMeters: Double) {
        val bounds = boundsFrom(center, radiusMeters)
        map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80))
    }

    private fun boundsFrom(center: LatLng, radiusMeters: Double): LatLngBounds {
        val lat = center.latitude
        val lng = center.longitude
        val latOffset = radiusMeters / 111_320.0
        val lngOffset = radiusMeters / (111_320.0 * cos(Math.toRadians(lat)))
        val sw = LatLng(lat - latOffset, lng - lngOffset)
        val ne = LatLng(lat + latOffset, lng + lngOffset)
        return LatLngBounds(sw, ne)
    }

    // ---------------- UI ----------------
    private fun setupUi() {
        // utilise tes strings existantes
        binding.tvTitle.text = getString(R.string.onboarding_zone_title)
        binding.tvSubtitle.text = getString(R.string.onboarding_zone_subtitle)
        updateRadiusLabel()
        binding.mapCard.post {
            val h = resources.displayMetrics.heightPixels
            binding.mapCard.layoutParams.height = (h * 0.33f).toInt()
            binding.mapCard.requestLayout()
        }
        updatePaddingTopForEdgeToEdge(binding.layoutChoiceZone)

    }

    private fun setupButtons() {
        binding.buttonConfigureLater.setOnClickListener { finish() }
        binding.buttonStart.setOnClickListener { finish() }
    }

    private fun setupSeekbar() {
        binding.radiusSeek.progress = radiusKm
        updateRadiusLabel()
        binding.radiusSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                radiusKm = progress.coerceAtLeast(1)
                updateRadiusLabel()
                updateCircleRadius()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateRadiusLabel() {
        // tv_radius_value dans TON layout
        binding.tvRadiusValue.text = getString(R.string.km_suffix, radiusKm)
    }

    // ------------- AUTOCOMPLETE -------------
    private fun setupAutocomplete() {
        val actv = binding.autoCompleteCityName as AutoCompleteTextView
        actv.threshold = 1
        actv.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line))

        actv.setOnItemClickListener { _, _, position, _ ->
            val prediction = predictions.getOrNull(position) ?: return@setOnItemClickListener
            hideKeyboardAndClearFocus()
            fetchPlaceDetails(prediction.placeId)
            actv.dismissDropDown()
        }

        actv.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val q = s?.toString()?.trim().orEmpty()
                if (q.length >= 1) fetchAutocompletePredictions(q)
            }
        })
    }

    private fun fetchAutocompletePredictions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries("FR")
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                predictions = response.autocompletePredictions
                val suggestions = predictions.map { it.getFullText(null).toString() }
                val actv = (binding.autoCompleteCityName as AutoCompleteTextView)
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions)
                actv.setAdapter(adapter)
                adapter.notifyDataSetChanged()
                if (suggestions.isNotEmpty()) actv.showDropDown()
            }
            .addOnFailureListener { e ->
                Log.e("ZoneActivity", "findAutocompletePredictions error: ${e.message}", e)
            }
    }

    private fun fetchPlaceDetails(placeId: String) {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val request = FetchPlaceRequest.builder(placeId, fields).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { rsp ->
                val place = rsp.place
                val latLng = place.latLng ?: return@addOnSuccessListener
                binding.autoCompleteCityName.setText(place.name ?: place.address ?: "")
                placeMarkerAndCircle(latLng, place.name ?: place.address)
            }
            .addOnFailureListener { e ->
                Log.e("ZoneActivity", "fetchPlace error: ${e.message}", e)
            }
    }
}
