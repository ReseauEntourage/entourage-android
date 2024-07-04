package social.entourage.android.main_filter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ScrollView
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityMainFilterBinding

enum class MainFilterMode {
    ACTION,
    GROUP
}

class MainFilterActivity : BaseActivity() {

    private lateinit var binding: ActivityMainFilterBinding
    private lateinit var interestsAdapter: MainFilterAdapter
    private lateinit var placesClient: PlacesClient

    companion object {
        var savedGroupInterests = mutableListOf<String>()
        var savedActionInterests = mutableListOf<String>()
        var savedRadius = 0
        var savedLocation: PlaceDetails? = null
        var mod: MainFilterMode = MainFilterMode.GROUP
        data class PlaceDetails(val name: String, val lat: Double, val lng: Double)
    }

    private var selectedInterests = mutableListOf<String>()
    private var selectedRadius = 0
    private var selectedLocation = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Places.
        Places.initialize(applicationContext, getString(R.string.google_api_key))
        placesClient = Places.createClient(this)

        loadSavedFilters()
        setupRecyclerView(loadInterestsOrActions())
        setupSeekBar()
        setupLocationAutoComplete()
        setupButtons()
        updateFilterCount(selectedInterests.size) // Initialiser le compteur avec le nombre d'intérêts sélectionnés
    }

    private fun loadSavedFilters() {
        if (mod == MainFilterMode.GROUP && savedGroupInterests.isNotEmpty()) {
            selectedInterests = savedGroupInterests.toMutableList()
        } else if (mod == MainFilterMode.ACTION && savedActionInterests.isNotEmpty()) {
            selectedInterests = savedActionInterests.toMutableList()
        }

        if (savedRadius != 0) {
            selectedRadius = savedRadius
            binding.seekbar.progress = selectedRadius
            binding.tvRadius.text = "$selectedRadius km"
        } else {
            val user = EntourageApplication.me(this)
            selectedRadius = user?.travelDistance ?: 0
            binding.seekbar.progress = selectedRadius
            binding.tvRadius.text = "$selectedRadius km"
        }

        savedLocation?.let {
            selectedLocation = it.name
            binding.autoCompleteCityName.setText(it.name)
        } ?: run {
            val user = EntourageApplication.me(this)
            val address = user?.address
            if (address != null) {
                selectedLocation = address.displayAddress
                savedLocation = PlaceDetails(address.displayAddress, address.latitude, address.longitude)
                binding.autoCompleteCityName.setText(address.displayAddress)
            }
        }
    }

    private fun loadInterestsOrActions(): List<MainFilterInterestForAdapter> {
        return if (mod == MainFilterMode.ACTION) {
            loadActions()
        } else {
            loadInterests()
        }
    }

    private fun loadInterests(): List<MainFilterInterestForAdapter> {
        return listOf(
            MainFilterInterestForAdapter("sport", getString(R.string.interest_sport), "", selectedInterests.contains("sport")),
            MainFilterInterestForAdapter("animaux", getString(R.string.interest_animaux), "", selectedInterests.contains("animaux")),
            MainFilterInterestForAdapter("marauding", getString(R.string.interest_marauding), "", selectedInterests.contains("marauding")),
            MainFilterInterestForAdapter("bien-etre", getString(R.string.interest_bien_etre), "", selectedInterests.contains("bien-etre")),
            MainFilterInterestForAdapter("cuisine", getString(R.string.interest_cuisine), "", selectedInterests.contains("cuisine")),
            MainFilterInterestForAdapter("culture", getString(R.string.interest_culture), "", selectedInterests.contains("culture")),
            MainFilterInterestForAdapter("nature", getString(R.string.interest_nature), "", selectedInterests.contains("nature")),
            MainFilterInterestForAdapter("jeux", getString(R.string.interest_jeux), "", selectedInterests.contains("jeux")),
            MainFilterInterestForAdapter("activites", getString(R.string.interest_activites_main_filter), "", selectedInterests.contains("activites")),
            MainFilterInterestForAdapter("other", getString(R.string.interest_other), "", selectedInterests.contains("other"))
        )
    }

    private fun loadActions(): List<MainFilterInterestForAdapter> {
        return listOf(
            MainFilterInterestForAdapter("temps_de_partage", "Temps de partage", "café, activité...", selectedInterests.contains("temps_de_partage")),
            MainFilterInterestForAdapter("service", "Service", "lessive, impression de documents...", selectedInterests.contains("service")),
            MainFilterInterestForAdapter("vetement", "Vêtement", "chaussures, manteau...", selectedInterests.contains("vetement")),
            MainFilterInterestForAdapter("equipment ", "Équipement", "téléphone, duvet...", selectedInterests.contains("equipement")),
            MainFilterInterestForAdapter("produit_d'hygiene", "Produit d'hygiène", "savon, protection hygiénique,...", selectedInterests.contains("produit_d'hygiene"))
        )
    }

    private fun setupRecyclerView(items: List<MainFilterInterestForAdapter>) {
        interestsAdapter = MainFilterAdapter(this, items) { interest ->
            if (interest.isSelected) {
                selectedInterests.add(interest.id)
            } else {
                selectedInterests.remove(interest.id)
            }
            updateFilterCount(selectedInterests.size) // Mettre à jour le compteur chaque fois qu'un intérêt est sélectionné ou désélectionné
        }
        binding.rvMainFilter.layoutManager = LinearLayoutManager(this)
        binding.rvMainFilter.adapter = interestsAdapter
    }

    private fun updateFilterCount(count: Int) {
        binding.tvNumberOfFilter.text = count.toString()
    }

    private fun setupSeekBar() {
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedRadius = progress
                binding.tvRadius.text = "$progress km"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupLocationAutoComplete() {
        val autoCompleteTextView = binding.autoCompleteCityName as AutoCompleteTextView
        autoCompleteTextView.threshold = 1
        autoCompleteTextView.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line))
        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedPrediction = autocompletePredictions[position]
            fetchPlaceDetails(selectedPrediction.placeId)
            autoCompleteTextView.dismissDropDown() // Fermer le menu déroulant une fois qu'un élément est sélectionné
        }
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    fetchAutocompletePredictions(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Add focus change listener to scroll to the view when it gains focus
        autoCompleteTextView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val scrollView = findViewById<ScrollView>(R.id.scrollView)
                scrollView.post {
                    scrollView.smoothScrollTo(0, v.top)
                }
            }
        }
    }

    private var autocompletePredictions: List<AutocompletePrediction> = listOf()

    private fun fetchAutocompletePredictions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries("FR")
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            autocompletePredictions = response.autocompletePredictions
            val suggestions = autocompletePredictions.map { it.getFullText(null).toString() }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions)
            (binding.autoCompleteCityName as AutoCompleteTextView).setAdapter(adapter)
            adapter.notifyDataSetChanged()
        }.addOnFailureListener { exception ->
            Log.e("PlaceAutocomplete", "Error: ${exception.message}", exception)
        }
    }

    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            selectedLocation = place.name ?: ""
            binding.autoCompleteCityName.setText(selectedLocation)
            val autoCompleteTextView = binding.autoCompleteCityName as AutoCompleteTextView
            autoCompleteTextView.dismissDropDown()
            savedLocation = PlaceDetails(place.name!!, place.latLng!!.latitude, place.latLng!!.longitude)
        }.addOnFailureListener { exception ->
            Log.e("PlaceAutocomplete", "Error: ${exception.message}", exception)
        }
    }

    private fun setupButtons() {
        binding.buttonConfigureLater.setOnClickListener {
            resetFilters()
        }
        binding.buttonStart.setOnClickListener {
            applyFilters()
        }
    }

    private fun resetFilters() {
        selectedInterests.clear()
        selectedRadius = 0
        selectedLocation = ""
        // Reset UI elements
        interestsAdapter.resetItems(loadInterestsOrActions())
        binding.seekbar.progress = 0
        binding.tvRadius.text = "0 km"
        binding.autoCompleteCityName.setText("")
        updateFilterCount(0)
        if (mod == MainFilterMode.GROUP) {
            savedGroupInterests.clear()
        } else {
            savedActionInterests.clear()
        }
        savedRadius = 0
        savedLocation = null
    }

    private fun applyFilters() {
        if (mod == MainFilterMode.GROUP) {
            savedGroupInterests = selectedInterests
        } else {
            savedActionInterests = selectedInterests
        }
        savedRadius = selectedRadius
        // savedLocation is already set in fetchPlaceDetails
        finish()
    }
}
