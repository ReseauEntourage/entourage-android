package social.entourage.android.profile

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.api.model.User
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.NewFragmentEditProfileBinding
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.main_filter.MainFilterActivity
import social.entourage.android.main_filter.MainFilterActivity.Companion.PlaceDetails
import social.entourage.android.profile.editProfile.EditPhotoActivity
import social.entourage.android.profile.editProfile.EditProfilePresenter
import social.entourage.android.tools.isValidEmail
import social.entourage.android.tools.utils.transformIntoDatePicker
import social.entourage.android.tools.utils.trimEnd
import social.entourage.android.user.AvatarUploadPresenter
import social.entourage.android.user.AvatarUploadRepository
import social.entourage.android.user.AvatarUploadView
import social.entourage.android.user.languechoose.ActivityChooseLanguage
import timber.log.Timber

class EditProfileActivity : BaseActivity(), AvatarUploadView {

    private lateinit var binding: NewFragmentEditProfileBinding
    private lateinit var avatarUploadPresenter: AvatarUploadPresenter
    private val editProfilePresenter: EditProfilePresenter by lazy { EditProfilePresenter() }
    val profilePresenter: ProfilePresenter by lazy { ProfilePresenter() }

    private lateinit var placesClient: PlacesClient

    private val paddingRight = 20
    private val paddingRightLimit = 60
    private val progressLimit = 96
    private var descriptionRegistered = ""
    private var savedLocation: PlaceDetails? = null

    // Mémorisation des prédictions pour l'autocomplete
    private var autocompletePredictions: List<AutocompletePrediction> = listOf()
    private var autocompleteAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewFragmentEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPlacesClient()
        initAvatarPresenter()
        initUI()
        updateUserView()
        adjustPaddingForKeyboard()
        editProfilePresenter.isUserUpdated.observe(this, ::hasUserBeenUpdated)
    }

    override fun onResume() {
        super.onResume()
        // Eventuellement recharger des infos si besoin
    }

    // --------------------------
    // Initialisations
    // --------------------------

    private fun initPlacesClient() {
        Places.initialize(applicationContext, getString(R.string.google_api_key))
        placesClient = Places.createClient(this)
    }

    private fun initAvatarPresenter() {
        avatarUploadPresenter = AvatarUploadPresenter(
            this,
            AvatarUploadRepository(),
            profilePresenter
        )
    }

    private fun initUI() {
        initializeSeekBar()
        initializeDescriptionCounter()

        setupEditImageButton()
        setupLanguageButton()
        setupInterestsButtons()
        setupActionZoneAutocomplete()

        setBackButton()
        setAddressFromCurrentUser() // Récupérer l'adresse utilisateur enregistrée
        setupValidateButton()
    }

    private fun setAddressFromCurrentUser() {
        val user = EntourageApplication.me(this)
        user?.address?.let {
            savedLocation = PlaceDetails(it.displayAddress, it.latitude, it.longitude)
        }
    }

    private fun hasUserBeenUpdated(isUpdated: Boolean) {
        if (isUpdated) {
            registerAddress()

        } else {
            Toast.makeText(this, "Erreur lors de l'enregistrement", Toast.LENGTH_LONG).show()
        }
    }

    // --------------------------
    // Setup UI Elements
    // --------------------------

    private fun initializeSeekBar() {
        binding.seekBarLayout.seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressValue = if (progress == 0) 1 else progress
                setProgressThumbPosition(progressValue)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setProgressThumbPosition(progress: Int) {
        binding.seekBarLayout.tvTrickleIndicator.text =
            String.format(getString(R.string.progress_km), progress.toString())
        val bounds = binding.seekBarLayout.seekbar.thumb.bounds
        val offset = if (progress > progressLimit) paddingRightLimit else paddingRight
        binding.seekBarLayout.tvTrickleIndicator.x =
            (binding.seekBarLayout.seekbar.left + bounds.left - offset).toFloat()
    }

    private fun initializeDescriptionCounter() {
        binding.description.content.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                updateDescriptionCounter(s.length)
            }
            override fun afterTextChanged(s: Editable) {}
        })
        updateDescriptionCounter(binding.description.content.text?.length ?: 0)
    }

    private fun updateDescriptionCounter(length: Int) {
        binding.description.counter.text = String.format(
            getString(R.string.description_counter),
            length.toString()
        )
        descriptionRegistered = binding.description.content.text.toString()
    }

    private fun setupEditImageButton() {
        binding.editImage.setOnClickListener {
            startActivity(Intent(this, EditPhotoActivity::class.java))
        }
    }

    private fun setupLanguageButton() {
        // Bouton langues désactivé pour l'instant
        binding.language.layout.setOnClickListener {
            val intent = Intent(this, ActivityChooseLanguage::class.java)
            startActivity(intent)
        }
        binding.language.layout.visibility = View.GONE
    }

    private fun setupInterestsButtons() {
        // Ouvrir l'écran d'onboarding amélioré pour l'édition des intérêts
        binding.interests.layout.setOnClickListener {
            EnhancedOnboarding.isFromSettingsinterest = true
            startActivity(Intent(this, EnhancedOnboarding::class.java))
            finish()
        }

        // Personnaliser le onboarding
        binding.personnalize.layout.setOnClickListener {
            MainActivity.isFromProfile = true
            startActivity(Intent(this, EnhancedOnboarding::class.java))
            finish()
        }
    }

    private fun setupActionZoneAutocomplete() {
        val autoCompleteTextView = binding.cityAction

        // Adapter vide au départ
        autocompleteAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line)
        autoCompleteTextView.setAdapter(autocompleteAdapter)
        autoCompleteTextView.threshold = 1

        // Listener sur la sélection d'un item
        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedPrediction = autocompletePredictions[position]
            fetchPlaceDetails(selectedPrediction.placeId)
            autoCompleteTextView.dismissDropDown()
        }

        // TextWatcher pour déclencher la recherche
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim()
                if (!query.isNullOrEmpty()) {
                    fetchAutocompletePredictions(query)
                }
            }
        })
    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener {
            finish()
        }
    }

    private fun setupValidateButton() {
        binding.validate.button.setOnClickListener {
            onSaveProfile()
        }
    }

    // --------------------------
    // Mise à jour de l'UI avec les données de l'utilisateur
    // --------------------------

    private fun updateUserView() {
        val user = EntourageApplication.me(this) ?: return
        val isArabic = resources.configuration.locales[0].language == "ar"

        with(binding) {
            // Configuration RTL si nécessaire
            configureTextDirection(isArabic, firstname.content)
            configureTextDirection(isArabic, lastname.content)
            configureTextDirection(isArabic, description.content)
            configureTextDirection(isArabic, birthday.content)
            configureTextDirection(isArabic, phone.content)
            configureTextDirection(isArabic, email.content)
            configureTextDirection(isArabic, cityAction)

            firstname.content.setText(user.firstName)
            lastname.content.setText(user.lastName)

            if (descriptionRegistered.isEmpty()) {
                description.content.setText(user.about)
            } else {
                description.content.setText(descriptionRegistered)
            }

            birthday.content.transformIntoDatePicker(
                this@EditProfileActivity,
                getString(R.string.birthday_date_format)
            )
            birthday.content.setText(user.birthday)

            phone.content.setText(user.phone)
            phone.content.setTextColor(ContextCompat.getColor(this@EditProfileActivity, R.color.dark_grey_opacity_40))
            email.content.setText(user.email)
            cityAction.setText(user.address?.displayAddress ?: "")

            seekBarLayout.seekbar.progress = user.travelDistance ?: 0
            seekBarLayout.seekbar.post {
                user.travelDistance?.let { setProgressThumbPosition(it) }
            }

            user.avatarURL?.let { avatarURL ->
                Glide.with(this@EditProfileActivity)
                    .load(Uri.parse(avatarURL))
                    .placeholder(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(imageProfile)
            } ?: run {
                imageProfile.setImageResource(R.drawable.placeholder_user)
            }
        }
    }

    private fun configureTextDirection(isArabic: Boolean, textView: TextView) {
        if (isArabic) {
            textView.layoutDirection = View.LAYOUT_DIRECTION_RTL
            textView.gravity = Gravity.END
            textView.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            textView.textDirection = View.TEXT_DIRECTION_RTL
        } else {
            textView.layoutDirection = View.LAYOUT_DIRECTION_LTR
            textView.gravity = Gravity.START
            textView.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            textView.textDirection = View.TEXT_DIRECTION_LTR
        }
    }

    // --------------------------
    // Gestion de l'autocomplete Places
    // --------------------------

    private fun fetchAutocompletePredictions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries("FR") // Limite à la France
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                autocompletePredictions = response.autocompletePredictions
                val suggestions = autocompletePredictions.map { it.getFullText(null).toString() }
                updateAutocompleteSuggestions(suggestions)
            }
            .addOnFailureListener { exception ->
                Timber.e("PlaceAutocomplete Error: ${exception.message}")
            }
    }

    private fun updateAutocompleteSuggestions(suggestions: List<String>) {
        autocompleteAdapter?.clear()
        autocompleteAdapter?.addAll(suggestions)
        autocompleteAdapter?.notifyDataSetChanged()
        // Montre le menu déroulant
        binding.cityAction.showDropDown()
    }

    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            binding.cityAction.setText(place.name ?: "")
            savedLocation = place.latLng?.let {
                PlaceDetails(
                    place.name ?: "",
                    it.latitude,
                    it.longitude
                )
            }
        }.addOnFailureListener { exception ->
            Timber.e("PlaceDetails Error: ${exception.message}")
        }
    }

    // --------------------------
    // Sauvegarde du profil
    // --------------------------

    private fun onSaveProfile() {
        if(checkEmail() && checkLastName()){
            val firstname = binding.firstname.content.text.trimEnd()
            val lastname = binding.lastname.content.text.trimEnd()
            val about = binding.description.content.text?.trimEnd()
            val email = binding.email.content.text.trimEnd()
            val birthday = binding.birthday.content.text.trimEnd()
            val travelDistance = binding.seekBarLayout.seekbar.progress
            val editedUser: ArrayMap<String, Any> = ArrayMap()
            editedUser["first_name"] = firstname
            editedUser["about"] = about
            editedUser["email"] = email
            editedUser["last_name"] = lastname

            editedUser["birthday"] = birthday
            editedUser["travel_distance"] = travelDistance
            editProfilePresenter.updateUser(editedUser)
        }
    }

    private fun checkEmail(): Boolean {
        if(binding.email.content.text.isEmpty()){
            return true
        }
        val isEmailCorrect = binding.email.content.text.trimEnd().isValidEmail()
        with(binding.email) {
            error.root.visibility = if (isEmailCorrect) View.GONE else View.VISIBLE
            error.errorMessage.text = getString(R.string.error_email)
            DrawableCompat.setTint(
                content.background,
                ContextCompat.getColor(
                    this@EditProfileActivity, // ou juste 'this' si vous êtes dans la même Activity
                    if (isEmailCorrect) R.color.light_orange_opacity_50 else R.color.red
                )
            )
        }
        return isEmailCorrect
    }

    private fun checkLastName():Boolean{
        if(binding.lastname.content.text.isEmpty()){
            return true
        }
        val isLastnameCorrect = binding.lastname.content.text.trimEnd().length > 2
        with(binding.lastname) {
            error.root.visibility = if (isLastnameCorrect) View.GONE else View.VISIBLE
            error.errorMessage.text = getString(R.string.error_lastname)
            DrawableCompat.setTint(
                content.background,
                ContextCompat.getColor(
                    this@EditProfileActivity,
                    if (isLastnameCorrect) R.color.light_orange_opacity_50 else R.color.red
                )
            )
        }
        return isLastnameCorrect
    }

    private fun registerAddress() {
        val address = createAddressFromSavedLocation() ?: return
        OnboardingAPI.getInstance().updateAddress(address, false) { isOK, userResponse ->
            if (isOK) {
                userResponse?.user?.let {
                    Toast.makeText(
                        this,
                        R.string.user_action_zone_send_ok,
                        Toast.LENGTH_LONG
                    ).show()
                    EntourageApplication.me(this)?.address = it.address
                    finish()
                }
            }
        }
    }

    private fun createAddressFromSavedLocation(): User.Address? {
        return savedLocation?.let {
            User.Address(it.lat, it.lng, it.name)
        }
    }

    // --------------------------
    // Vérification des champs
    // --------------------------

    private fun checkError(): Boolean {
        val isLastnameCorrect = binding.lastname.content.text.trimEnd().length > 2
        val isEmailCorrect = binding.email.content.text.trimEnd().isValidEmail()

        // Gestion de l'affichage des erreurs
        updateErrorUI(binding.lastname, isLastnameCorrect, getString(R.string.error_lastname))
        updateErrorUI(binding.email, isEmailCorrect, getString(R.string.error_email))

        return isLastnameCorrect && isEmailCorrect
    }

    private fun updateErrorUI(itemLayout: social.entourage.android.databinding.NewProfileEditItemBinding, isCorrect: Boolean, errorMsg: String) {
        itemLayout.error.root.visibility = if (isCorrect) View.GONE else View.VISIBLE
        itemLayout.error.errorMessage.text = errorMsg
        DrawableCompat.setTint(
            itemLayout.content.background,
            ContextCompat.getColor(this, if (isCorrect) R.color.light_orange_opacity_50 else R.color.red)
        )
    }

    // --------------------------
    // Gestion du clavier et du padding
    // --------------------------

    private fun adjustPaddingForKeyboard() {
        val rootView = binding.root
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val screenHeight = rootView.height
                val keypadHeight = screenHeight - rect.bottom

                if (keypadHeight > screenHeight * 0.15) {
                    // Clavier visible
                    binding.scrollView.setPadding(0, 0, 0, keypadHeight)
                } else {
                    // Clavier fermé
                    binding.scrollView.setPadding(0, 0, 0, 0)
                }
            }
        })
    }

    // --------------------------
    // AvatarUploadView Impl
    // --------------------------

    override fun onUploadError() {
        Timber.e("Error uploading photo")
    }
}
