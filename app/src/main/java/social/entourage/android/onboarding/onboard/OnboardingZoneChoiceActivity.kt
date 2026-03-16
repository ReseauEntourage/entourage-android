package social.entourage.android.onboarding.onboard

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
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

    private lateinit var binding: ActivityOnboardingZoneChoiceBinding
    private lateinit var placesClient: PlacesClient
    private var map: GoogleMap? = null
    private var predictions: List<AutocompletePrediction> = emptyList()
    private var marker: Marker? = null
    private var circle: Circle? = null
    private var currentLatLng: LatLng? = null
    private var radiusKm: Int = 20
    private var selectedUserType: UserType = UserType.ENTOUR
    private var lastPlaceId: String? = null
    private var lastDisplayAddress: String? = null
    private var suppressAutocomplete = false
    private var queryGen = 0
    private var lastPostalCode: String? = null
    private var currentAverageCount: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingZoneChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        social.entourage.android.tools.log.AnalyticsEvents.logEvent(social.entourage.android.tools.log.AnalyticsEvents.View__Onboarding__Location)

        selectedUserType = intent.getStringExtra(EXTRA_USER_TYPE)
            ?.let { runCatching { UserType.valueOf(it) }.getOrNull() }
            ?: UserType.ENTOUR

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_api_key))
        }
        placesClient = Places.createClient(this)

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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap.apply {
            uiSettings.isMapToolbarEnabled = false
            uiSettings.isMyLocationButtonEnabled = false
        }
        val france = LatLng(46.7111, 1.7191)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(france, 5.5f))
        updateRadiusLabel()
    }

    private fun setupUi() {
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
        binding.buttonConfigureLater.setOnClickListener {
            social.entourage.android.tools.log.AnalyticsEvents.logEvent(social.entourage.android.tools.log.AnalyticsEvents.Clic__Back__Onboarding__Location)
            onBackPressed()
        }

        binding.buttonStart.setOnClickListener {
            social.entourage.android.tools.log.AnalyticsEvents.logEvent(social.entourage.android.tools.log.AnalyticsEvents.Clic__Next__Onboarding__Location)
            val latLng = currentLatLng
            if (latLng == null) {
                Toast.makeText(this, R.string.onboarding_zone_pick_location_first, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val label = lastDisplayAddress
                ?.takeIf { it.isNotBlank() }
                ?: binding.autoCompleteCityName.text?.toString()?.takeIf { it.isNotBlank() }
                ?: ""

            val addr = buildPrimaryAddress() ?: run {
                Toast.makeText(this, R.string.onboarding_zone_pick_location_first, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            OnboardingAPI.getInstance().updateTravelDistance(radiusKm) { okDist, _ ->
                if (!okDist) {
                    Toast.makeText(this, R.string.user_action_zone_send_failed, Toast.LENGTH_SHORT).show()
                    return@updateTravelDistance
                }

                OnboardingAPI.getInstance().updateAddress(addr, false) { isOK, _ ->
                    if (!isOK) {
                        Toast.makeText(this, R.string.user_action_zone_send_failed, Toast.LENGTH_SHORT).show()
                        return@updateAddress
                    }

                    if (selectedUserType == UserType.ASSO) {
                        startActivity(
                            OnboardingAssociationChoiceActivity.newIntent(
                                context = this,
                                address = label,
                                lat = latLng.latitude,
                                lng = latLng.longitude,
                                postalCode = lastPostalCode
                            )
                        )
                        finish()
                    } else {
                        startActivity(Intent(this, OnboardingEndActivity::class.java))
                        finish()
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
                radiusKm = progress.coerceAtLeast(1)
                updateRadiusLabel()
                updateCircleRadius()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                updateAverageCount()
            }
        })
    }

    private fun updateRadiusLabel() {
        binding.tvRadiusValue.text = getString(R.string.km_suffix, radiusKm)
    }

    private fun setupAutocomplete() {
        val actv = binding.autoCompleteCityName as AutoCompleteTextView
        actv.threshold = 1
        val baseAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        )
        actv.setAdapter(baseAdapter)

        actv.setOnItemClickListener { _, _, position, _ ->
            val prediction = predictions.getOrNull(position) ?: return@setOnItemClickListener
            val label = prediction.getFullText(null).toString()

            suppressAutocomplete = true
            actv.setText(label, false)
            actv.setSelection(label.length)
            actv.dismissDropDown()
            actv.clearFocus()
            window?.decorView?.clearFocus()
            binding.layoutChoiceZone.requestFocus()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(actv.windowToken, 0)

            queryGen++
            fetchPlaceDetails(prediction.placeId)
            actv.postDelayed({ suppressAutocomplete = false }, 300)
        }

        actv.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                (v as AutoCompleteTextView).dismissDropDown()
            }
        }

        actv.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (suppressAutocomplete) return
                val q = s?.toString()?.trim().orEmpty()
                if (q.length >= 1) fetchAutocompletePredictions(q)
            }
        })
    }

    private fun fetchAutocompletePredictions(query: String) {
        val myGen = ++queryGen
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries("FR")
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val actv = (binding.autoCompleteCityName as AutoCompleteTextView)
                if (suppressAutocomplete || !actv.hasFocus() || myGen != queryGen) return@addOnSuccessListener
                predictions = response.autocompletePredictions
                val suggestions = predictions.map { it.getFullText(null).toString() }
                val adapter = (actv.adapter as? ArrayAdapter<String>)
                    ?: ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, mutableListOf()).also {
                        actv.setAdapter(it)
                    }

                adapter.clear()
                adapter.addAll(suggestions)
                adapter.notifyDataSetChanged()
                if (suggestions.isNotEmpty()) actv.showDropDown()
            }
            .addOnFailureListener { e ->
                Log.e("ZoneActivity", "findAutocompletePredictions error: ${e.message}", e)
            }
    }

    private fun fetchPlaceDetails(placeId: String) {
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.ADDRESS_COMPONENTS
        )
        val request = FetchPlaceRequest.builder(placeId, fields).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { rsp ->
                val place = rsp.place
                val latLng = place.latLng ?: return@addOnSuccessListener
                val label = place.name ?: place.address ?: ""
                lastPlaceId = place.id
                lastDisplayAddress = label

                lastPostalCode = place.addressComponents
                    ?.asList()
                    ?.firstOrNull { it.types.contains("postal_code") }
                    ?.name
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }

                binding.autoCompleteCityName.setText(label, false)
                binding.autoCompleteCityName.setSelection(label.length)
                placeMarkerAndCircle(latLng, label)
            }
            .addOnFailureListener { e ->
                Log.e("ZoneActivity", "fetchPlace error: ${e.message}", e)
            }
    }

    private fun placeMarkerAndCircle(position: LatLng, title: String?) {
        val m = map ?: return
        if (marker == null) {
            marker = m.addMarker(MarkerOptions().position(position).title(title ?: "Localisation"))
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
                    .fillColor(0x55FF7F00.toInt())
            )
        } else {
            circle?.center = position
            circle?.radius = radiusMeters
        }

        currentLatLng = position
        lastDisplayAddress = title ?: lastDisplayAddress
        zoomToCircle(position, radiusMeters)
        updateAverageCount()
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

    private fun updateAverageCount() {
        val latLng = currentLatLng ?: return

        OnboardingAPI.getInstance().getEventsWeekAverage(latLng.latitude, latLng.longitude, radiusKm) { isOk, average ->
            // Changement ici : on s'assure d'être sur le thread UI pour modifier la visibilité
            runOnUiThread {
                if (isOk && average != null) {
                    if (average <= 0f) {
                        binding.layoutEventCount.visibility = View.GONE
                        currentAverageCount = 0f
                    } else {
                        binding.layoutEventCount.visibility = View.VISIBLE
                        animateCount(currentAverageCount, average)
                        currentAverageCount = average
                    }
                } else {
                    binding.layoutEventCount.visibility = View.GONE
                    currentAverageCount = 0f
                }
            }
        }
    }

    private fun animateCount(start: Float, end: Float) {
        val animator = ValueAnimator.ofFloat(start, end)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            val intValue = kotlin.math.round(value).toInt()
            val rawString = if (intValue > 1) {
                getString(R.string.onboarding_zone_events_count_plural, intValue)
            } else {
                getString(R.string.onboarding_zone_events_count_singular, intValue)
            }
            binding.tvEventCount.text = HtmlCompat.fromHtml(rawString, HtmlCompat.FROM_HTML_MODE_COMPACT)
        }
        animator.start()
    }

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

    enum class UserType { ENTOUR, BE_ENTOUR, ASSO }

    companion object {
        private const val EXTRA_USER_TYPE = "extra_user_type"

        fun newIntent(context: Context, userType: UserType): Intent {
            return Intent(context, OnboardingZoneChoiceActivity::class.java)
                .putExtra(EXTRA_USER_TYPE, userType.name)
        }
    }
}