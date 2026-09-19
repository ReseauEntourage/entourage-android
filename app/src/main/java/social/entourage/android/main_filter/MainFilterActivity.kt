package social.entourage.android.main_filter

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
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
import social.entourage.android.tools.updatePaddingForEdgeToEdge

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
        var savedGroupInterestsFromOnboarding = mutableListOf<String>()
        var savedEventTypes = mutableListOf<String>()
        var savedEventFormat: String? = null
        var savedRadius = 0
        var savedLocation: PlaceDetails? = null
        var mod: MainFilterMode = MainFilterMode.GROUP
        var hasToReloadAction = false
        var hasFilter = false
        data class PlaceDetails(val name: String, val lat: Double, val lng: Double)

        const val EVENT_TYPE_ENTOURAGE = "entourage"
        const val EVENT_TYPE_RESERVED_FEMALE = "reserved_female"
        const val EVENT_FORMAT_PRESENTIAL = "presential"
        const val EVENT_FORMAT_REMOTE = "remote"

        fun resetAllFilters(context: Context) {
            val user = EntourageApplication.me(context)
            savedGroupInterests.clear()
            savedActionInterests.clear()
            savedEventTypes.clear()
            savedEventFormat = null
            savedRadius = user?.travelDistance ?: 0
            savedLocation = user?.address?.let { PlaceDetails(it.displayAddress, it.latitude, it.longitude) }
            hasFilter = false
        }
    }

    private var selectedInterests = mutableListOf<String>()
    private var selectedEventTypes = mutableListOf<String>()
    private var selectedFormat: String? = null
    private var selectedRadius = 0
    private var selectedLocation = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)

        binding = ActivityMainFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updatePaddingForEdgeToEdge(binding.root)

        // Initialize Places.
        Places.initialize(applicationContext, getString(R.string.google_api_key))
        placesClient = Places.createClient(this)

        loadSavedFilters()
        setupRecyclerView(loadInterestsOrActions())
        setupSeekBar()
        setupLocationAutoComplete()
        setupButtons()
        setupEventTypeAndFormatChips()
        updateFilterCount(totalFilterCount()) // Initialiser le compteur avec le nombre de filtres sélectionnés

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            binding.root.updatePadding(
                top = statusBars.top,
                bottom = maxOf(navBars.bottom, ime.bottom)
            )

            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                binding.scrollView.findFocus()?.let { focusedView ->
                    scrollToView(focusedView)
                }
            }

            insets
        }
    }

    private fun scrollToView(view: View) {
        binding.scrollView.post {
            val rect = Rect()
            view.getDrawingRect(rect)
            binding.scrollView.offsetDescendantRectToMyCoords(view, rect)
            binding.scrollView.scrollTo(0, rect.top)
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

        if (mod == MainFilterMode.EVENT) {
            selectedEventTypes = savedEventTypes.toMutableList()
            if (selectedEventTypes.isNotEmpty()) hasFilter = true
            selectedFormat = savedEventFormat
            if (selectedFormat != null) hasFilter = true
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
            MainFilterInterestForAdapter("sport", getString(R.string.interest_sport), getString(R.string.interest_sport_subtitle), selectedInterests.contains("sport")),
            MainFilterInterestForAdapter("animaux", getString(R.string.interest_animaux), getString(R.string.interest_animaux_subtitle), selectedInterests.contains("animaux")),
            MainFilterInterestForAdapter("marauding", getString(R.string.interest_marauding), getString(R.string.interest_marauding_subtitle), selectedInterests.contains("marauding")),
            MainFilterInterestForAdapter("bien-etre", getString(R.string.interest_bien_etre), getString(R.string.interest_bien_etre_subtitle), selectedInterests.contains("bien-etre")),
            MainFilterInterestForAdapter("cuisine", getString(R.string.interest_cuisine), getString(R.string.interest_cuisine_subtitle), selectedInterests.contains("cuisine")),
            MainFilterInterestForAdapter("culture", getString(R.string.interest_culture), getString(R.string.interest_culture_subtitle), selectedInterests.contains("culture")),
            MainFilterInterestForAdapter("nature", getString(R.string.interest_nature), getString(R.string.interest_nature_subtitle), selectedInterests.contains("nature")),
            MainFilterInterestForAdapter("jeux", getString(R.string.interest_jeux), getString(R.string.interest_jeux_subtitle), selectedInterests.contains("jeux")),
            MainFilterInterestForAdapter("activites", getString(R.string.interest_activites_main_filter), getString(R.string.interest_activites_subtitle), selectedInterests.contains("activites"))
        )
    }
    private fun loadActions(): List<MainFilterInterestForAdapter> {
        //LOG WTF des selectedInterests
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
            updateFilterCount(totalFilterCount()) // Mettre à jour le compteur chaque fois qu'un intérêt est sélectionné ou désélectionné
        }
        binding.rvMainFilter.layoutManager = LinearLayoutManager(this)
        binding.rvMainFilter.adapter = interestsAdapter
    }

    private fun totalFilterCount(): Int {
        return selectedInterests.size + selectedEventTypes.size + (if (selectedFormat != null) 1 else 0)
    }

    private fun updateFilterCount(count: Int) {
        // Le badge est affiché à côté du titre "Par thématique" : il ne doit refléter
        // que les intérêts sélectionnés, pas les chips type d'event / format.
        binding.tvNumberOfFilter.text = selectedInterests.size.toString()
        if(count > 0){
            hasFilter = true
        }
    }

    private fun setupEventTypeAndFormatChips() {
        val isEventMode = mod == MainFilterMode.EVENT
        binding.tvSubtitleEventType.visibility = if (isEventMode) View.VISIBLE else View.GONE
        binding.layoutEventTypeChips.visibility = if (isEventMode) View.VISIBLE else View.GONE
        binding.tvSubtitleFormat.visibility = if (isEventMode) View.VISIBLE else View.GONE
        binding.layoutFormatChips.visibility = if (isEventMode) View.VISIBLE else View.GONE

        if (!isEventMode) return

        val isFemale = EntourageApplication.me(this)?.gender == "female"
        binding.chipEventReservedFemale.visibility = if (isFemale) View.VISIBLE else View.GONE

        binding.chipEventEntourage.setOnClickListener { toggleEventType(EVENT_TYPE_ENTOURAGE) }
        binding.chipEventReservedFemale.setOnClickListener { toggleEventType(EVENT_TYPE_RESERVED_FEMALE) }
        binding.chipFormatPresentiel.setOnClickListener { toggleFormat(EVENT_FORMAT_PRESENTIAL) }
        binding.chipFormatVisio.setOnClickListener { toggleFormat(EVENT_FORMAT_REMOTE) }

        refreshEventTypeAndFormatStyles()
    }

    private fun toggleEventType(type: String) {
        if (selectedEventTypes.contains(type)) {
            selectedEventTypes.remove(type)
        } else {
            selectedEventTypes.add(type)
            AnalyticsEvents.logEvent("event_" + AnalyticsEvents.filter_tag_item_ + type)
        }
        refreshEventTypeAndFormatStyles()
        updateFilterCount(totalFilterCount())
    }

    private fun toggleFormat(format: String) {
        selectedFormat = if (selectedFormat == format) null else format
        if (selectedFormat != null) {
            AnalyticsEvents.logEvent("event_" + AnalyticsEvents.filter_tag_item_ + selectedFormat)
        }
        refreshEventTypeAndFormatStyles()
        updateFilterCount(totalFilterCount())
    }

    private fun refreshEventTypeAndFormatStyles() {
        updateChipToggleStyle(
            binding.chipEventEntourage, binding.ivChipEntourageIcon, binding.tvChipEntourage,
            selectedEventTypes.contains(EVENT_TYPE_ENTOURAGE)
        )
        updateChipToggleStyle(
            binding.chipEventReservedFemale, binding.ivChipReservedFemaleIcon, binding.tvChipReservedFemale,
            selectedEventTypes.contains(EVENT_TYPE_RESERVED_FEMALE), R.drawable.shape_chip_pill_purple_filled
        )
        updateChipToggleStyle(binding.chipFormatPresentiel, null, binding.tvChipPresentiel, selectedFormat == EVENT_FORMAT_PRESENTIAL)
        updateChipToggleStyle(binding.chipFormatVisio, null, binding.tvChipVisio, selectedFormat == EVENT_FORMAT_REMOTE)
    }

    private fun updateChipToggleStyle(
        container: LinearLayout, icon: ImageView?, text: TextView, isSelected: Boolean,
        selectedBackgroundRes: Int = R.drawable.shape_chip_pill_orange_filled
    ) {
        container.setBackgroundResource(if (isSelected) selectedBackgroundRes else R.drawable.shape_chip_pill_grey_border)
        text.setTextColor(ContextCompat.getColor(this, if (isSelected) R.color.white else R.color.black))
        if (isSelected) {
            icon?.setColorFilter(ContextCompat.getColor(this, R.color.white))
        } else {
            icon?.clearColorFilter()
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
        selectedEventTypes.clear()
        selectedFormat = null
        savedLocation = user?.address?.let { PlaceDetails(it.displayAddress, it.latitude, it.longitude) }
        selectedRadius = user?.travelDistance ?: 0
        selectedLocation = user?.address?.displayAddress ?: ""
        // Reset UI elements
        interestsAdapter.resetItems(loadInterestsOrActions())
        binding.seekbar.progress = user?.travelDistance ?: 0
        binding.tvRadius.text = user?.travelDistance.toString() ?: "0 km"
        binding.autoCompleteCityName.setText(user?.address?.displayAddress?: "")
        if (mod == MainFilterMode.EVENT) {
            refreshEventTypeAndFormatStyles()
        }
        updateFilterCount(0)
        if (mod == MainFilterMode.GROUP || mod == MainFilterMode.EVENT) {
            savedGroupInterests.clear()
        } else {
            savedActionInterests.clear()
        }
        savedRadius = 0
        savedLocation = null
        savedEventTypes.clear()
        savedEventFormat = null
        hasFilter = false
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
        if (mod == MainFilterMode.EVENT) {
            savedEventTypes = selectedEventTypes
            savedEventFormat = selectedFormat
        }
        finish()
    }

}