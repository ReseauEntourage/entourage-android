package social.entourage.android.main_filter

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ScrollView
import android.widget.SeekBar
import androidx.core.widget.NestedScrollView
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
import social.entourage.android.tools.log.AnalyticsEvents

enum class MainFilterMode {
    ACTION,
    GROUP,
    EVENT
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
        var hasToReloadAction = false
        var hasFilter = false
        data class PlaceDetails(val name: String, val lat: Double, val lng: Double)
        fun resetAllFilters(context: Context) {
            val user = EntourageApplication.me(context)
            savedGroupInterests.clear()
            savedActionInterests.clear()
            savedRadius = user?.travelDistance ?: 0
            savedLocation = user?.address?.let { PlaceDetails(it.displayAddress, it.latitude, it.longitude) }
            hasFilter = false
        }
    }

    private var selectedInterests = mutableListOf<String>()
    private var selectedRadius = 0
    private var selectedLocation = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)

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

        // Ajouter un listener pour détecter les changements de layout (comme l'ouverture du clavier)
        addKeyboardListener()
    }

    override fun onResume() {
        super.onResume()
        if(mod == MainFilterMode.ACTION ){
            binding.tvSubtitleItems.text = getString(R.string.main_filter_subtitle_action)
        }else{
            binding.tvSubtitleItems.text = getString(R.string.main_filter_subtitle_group_event)
        }
    }

    private fun addKeyboardListener() {
        val rootView = binding.root
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val screenHeight = rootView.height
                val keypadHeight = screenHeight - rect.bottom

                if (keypadHeight > screenHeight * 0.15) { // si le clavier est visible
                    val params = binding.rvMainFilter.layoutParams as ViewGroup.MarginLayoutParams
                    params.height = screenHeight - keypadHeight - binding.autoCompleteCityName.height
                    binding.rvMainFilter.layoutParams = params
                    scrollToView(binding.autoCompleteCityName)
                } else {
                    val params = binding.rvMainFilter.layoutParams as ViewGroup.MarginLayoutParams
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT
                    binding.rvMainFilter.layoutParams = params
                }
            }
        })
    }

    private fun scrollToView(view: View) {
        binding.scrollView.post {
            binding.scrollView.smoothScrollTo(0, view.bottom)
        }
    }

    private fun loadSavedFilters() {
        hasFilter = false
        if ((mod == MainFilterMode.GROUP || mod == MainFilterMode.EVENT) && savedGroupInterests.isNotEmpty()) {
            selectedInterests = savedGroupInterests.toMutableList()
            hasFilter = true
        } else if (mod == MainFilterMode.ACTION && savedActionInterests.isNotEmpty()) {
            selectedInterests = savedActionInterests.toMutableList()
            hasFilter = true
        }

        if (savedRadius != 0) {
            hasFilter = true
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
            hasFilter = true
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
        //LOG WTF des selectedInterests
        Log.wtf("wtf", "wtf" + selectedInterests.toString())
        return listOf(
            MainFilterInterestForAdapter("social", "Temps de partage", "(café, activité...)", selectedInterests.contains("social")),
            MainFilterInterestForAdapter("services", "Service", "(lessive, impression de documents...)", selectedInterests.contains("services")),
            MainFilterInterestForAdapter("clothes", "Vêtement", "(chaussures, manteau...)", selectedInterests.contains("clothes")),
            MainFilterInterestForAdapter("equipment", "Équipement", "(téléphone, duvet...)", selectedInterests.contains("equipment")),
            MainFilterInterestForAdapter("hygiene", "Produit d'hygiène", "(savon, protections hygiéniques,...)", selectedInterests.contains("hygiene"))
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
        if(count > 0){
            hasFilter = true
        }
    }

    private fun setupSeekBar() {
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedRadius = progress
                binding.tvRadius.text = "$progress km"
                hasFilter = true
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

            }

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    fetchAutocompletePredictions(s.toString())
                    hasFilter = true
                }
            }
        })

        // Add focus change listener to scroll to the view when it gains focus
        autoCompleteTextView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                scrollToView(v)
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
            autoCompleteTextView.setSelection(autoCompleteTextView.text.length) // Placer le curseur à la fin
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
        binding.iconBack.setOnClickListener {
            finish()
        }
    }

     fun resetFilters() {
        val user = EntourageApplication.me(this)
        selectedInterests.clear()
        savedLocation = user?.address?.let { PlaceDetails(it.displayAddress, it.latitude, it.longitude) }
        selectedRadius = user?.travelDistance ?: 0
        selectedLocation = user?.address?.displayAddress ?: ""
        // Reset UI elements
        interestsAdapter.resetItems(loadInterestsOrActions())
        binding.seekbar.progress = user?.travelDistance ?: 0
        binding.tvRadius.text = user?.travelDistance.toString() ?: "0 km"
        binding.autoCompleteCityName.setText(user?.address?.displayAddress?: "")
        updateFilterCount(0)
        if (mod == MainFilterMode.GROUP || mod == MainFilterMode.EVENT) {
            savedGroupInterests.clear()
        } else {
            savedActionInterests.clear()
        }
        savedRadius = 0
        savedLocation = null
    }

    private fun applyFilters() {
        when (mod) {
            MainFilterMode.GROUP -> {
                AnalyticsEvents.logEvent(AnalyticsEvents.groups_filter_apply_clic)
            }
            MainFilterMode.EVENT -> {
                AnalyticsEvents.logEvent(AnalyticsEvents.events_filter_apply_clic)
            }
            MainFilterMode.ACTION -> {
                AnalyticsEvents.logEvent(AnalyticsEvents.actions_filter_apply_clic)
            }
        }
        if (mod == MainFilterMode.GROUP|| mod == MainFilterMode.EVENT) {
            savedGroupInterests = selectedInterests
        } else {
            savedActionInterests = selectedInterests
        }
        savedRadius = selectedRadius
        // savedLocation is already set in fetchPlaceDetails
        finish()
    }

}
