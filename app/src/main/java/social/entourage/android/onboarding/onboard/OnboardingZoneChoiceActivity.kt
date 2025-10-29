package social.entourage.android.onboarding.onboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlin.math.cos
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.api.model.User
import social.entourage.android.databinding.ActivityOnboardingZoneChoiceBinding
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge

class OnboardingZoneChoiceActivity : AppCompatActivity(), OnMapReadyCallback {

    // ViewBinding
    private lateinit var binding: ActivityOnboardingZoneChoiceBinding

    // Google Places / Maps
    private lateinit var placesClient: PlacesClient
    private var map: GoogleMap? = null
    private var predictions: List<AutocompletePrediction> = emptyList()

    // Carte / UI state
    private var marker: Marker? = null
    private var circle: Circle? = null
    private var currentLatLng: LatLng? = null
    private var radiusKm: Int = 20

    // Choix du type utilisateur (défini à l'écran précédent)
    private var selectedUserType: UserType = UserType.ENTOUR

    // Mémoire d’adresse sélectionnée
    private var lastPlaceId: String? = null
    private var lastDisplayAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingZoneChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Récupération du type choisi (ENTOUR / BE_ENTOUR / ASSO)
        selectedUserType = intent.getStringExtra(EXTRA_USER_TYPE)
            ?.let { runCatching { UserType.valueOf(it) }.getOrNull() }
            ?: UserType.ENTOUR

        // Places init
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_api_key))
        }
        placesClient = Places.createClient(this)

        // Map
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

    // ---------------- UI Helpers ----------------

    private fun setupUi() {
        binding.tvTitle.text = getString(R.string.onboarding_zone_title)
        binding.tvSubtitle.text = getString(R.string.onboarding_zone_subtitle)
        updateRadiusLabel()

        // hauteur de la carte ≈ 33% écran
        binding.mapCard.post {
            val h = resources.displayMetrics.heightPixels
            binding.mapCard.layoutParams.height = (h * 0.33f).toInt()
            binding.mapCard.requestLayout()
        }

        updatePaddingTopForEdgeToEdge(binding.layoutChoiceZone)
    }

    private fun setupButtons() {
        // "Plus tard" : si ASSO -> TODO toast, sinon on va vers l’écran de fin
        binding.buttonConfigureLater.setOnClickListener {
            if (selectedUserType == UserType.ASSO) {
                Toast.makeText(this, R.string.onboard_asso_todo, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, OnboardingEndActivity::class.java))
            finish()
        }

        // "Commencer" : si ASSO -> TODO toast
        // sinon: 1) update travel_distance, 2) update address, 3) OnboardingEnd
        binding.buttonStart.setOnClickListener {
            if (selectedUserType == UserType.ASSO) {
                Toast.makeText(this, R.string.onboard_asso_todo, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val addr = buildPrimaryAddress() ?: run {
                Toast.makeText(
                    this,
                    R.string.onboarding_zone_pick_location_first,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // 1) D’abord la distance
            OnboardingAPI.getInstance().updateTravelDistance(radiusKm) { okDist, _ ->
                if (!okDist) {
                    Toast.makeText(
                        this,
                        R.string.user_action_zone_send_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@updateTravelDistance
                }

                // 2) Puis l’adresse
                OnboardingAPI.getInstance().updateAddress(addr, false) { isOK, _ ->
                    if (isOK) {
                        startActivity(Intent(this, OnboardingEndActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            R.string.user_action_zone_send_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun setupSeekbar() {
        binding.radiusSeek.progress = radiusKm
        updateRadiusLabel()
        binding.radiusSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                radiusKm = progress.coerceAtLeast(1) // min 1 km
                updateRadiusLabel()
                updateCircleRadius()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateRadiusLabel() {
        binding.tvRadiusValue.text = getString(R.string.km_suffix, radiusKm)
    }

    // ---------------- AUTOCOMPLETE ----------------

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
                val label = place.name ?: place.address ?: ""
                lastPlaceId = place.id
                lastDisplayAddress = label
                binding.autoCompleteCityName.setText(label)
                placeMarkerAndCircle(latLng, label)
            }
            .addOnFailureListener { e ->
                Log.e("ZoneActivity", "fetchPlace error: ${e.message}", e)
            }
    }

    // ---------------- MAP Drawing ----------------

    private fun placeMarkerAndCircle(position: LatLng, title: String?) {
        val m = map ?: return

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
        lastDisplayAddress = title ?: lastDisplayAddress
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

    // ---------------- Address build ----------------

    /**
     * Construit l’adresse primaire pour l’API en respectant tes deux constructeurs :
     * - User.Address(googlePlaceId: String?)
     * - User.Address(latitude: Double, longitude: Double, displayAddress: String?)
     */
    private fun buildPrimaryAddress(): User.Address? {
        val latLng = currentLatLng ?: return null
        return if (!lastPlaceId.isNullOrBlank()) {
            User.Address(lastPlaceId)
        } else {
            val label = lastDisplayAddress
                ?.takeIf { it.isNotBlank() }
                ?: binding.autoCompleteCityName.text?.toString()?.takeIf { it.isNotBlank() }
                ?: ""
            User.Address(latLng.latitude, latLng.longitude, label)
        }
    }

    // ---------------- UX helpers ----------------

    private fun hideKeyboardAndClearFocus() {
        val actv = binding.autoCompleteCityName as AutoCompleteTextView
        actv.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(actv.windowToken, 0)
    }

    // ---------------- Types & Companion ----------------

    enum class UserType { ENTOUR, BE_ENTOUR, ASSO }

    companion object {
        private const val EXTRA_USER_TYPE = "extra_user_type"

        fun newIntent(context: Context, userType: UserType): Intent {
            return Intent(context, OnboardingZoneChoiceActivity::class.java)
                .putExtra(EXTRA_USER_TYPE, userType.name)
        }
    }
}
