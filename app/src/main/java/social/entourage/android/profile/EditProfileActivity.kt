package social.entourage.android.profile

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
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
import social.entourage.android.databinding.ActivityEditProfileBinding
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.main_filter.MainFilterActivity.Companion.PlaceDetails
import social.entourage.android.profile.editProfile.EditPhotoActivity
import social.entourage.android.profile.editProfile.EditProfilePresenter
import social.entourage.android.tools.isValidEmail
import social.entourage.android.tools.utils.transformIntoDatePicker
import social.entourage.android.tools.utils.trimEnd
import social.entourage.android.user.AvatarUploadPresenter
import social.entourage.android.user.AvatarUploadRepository
import social.entourage.android.user.AvatarUploadView
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class EditProfileActivity : BaseActivity(), AvatarUploadView {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var avatarUploadPresenter: AvatarUploadPresenter
    private val editProfilePresenter: EditProfilePresenter by lazy { EditProfilePresenter() }
    val profilePresenter: ProfilePresenter by lazy { ProfilePresenter() }

    private lateinit var placesClient: PlacesClient

    private val paddingRight = 20
    private val paddingRightLimit = 60
    private val progressLimit = 96
    private var descriptionRegistered = ""
    private var savedLocation: PlaceDetails? = null
    private var selectedGender: String? = null

    // ----------------------------------------------------------------------
    // CONFIGURATION DATE
    // ----------------------------------------------------------------------
    private val dateFormatString = "dd/MM/yyyy"
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE)
    private val uiDateFormat = SimpleDateFormat(dateFormatString, Locale.FRANCE)

    private var autocompletePredictions: List<AutocompletePrediction> = listOf()
    private var autocompleteAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPlacesClient()
        initAvatarPresenter()
        initUI()
        updateUserView()
        adjustPaddingForKeyboard()

        editProfilePresenter.isUserUpdated.observe(this, ::hasUserBeenUpdated)
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
        setupGender()
        setupInterestsButtons()
        setupActionZoneAutocomplete()

        setBackButton()
        setAddressFromCurrentUser()
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
            String.format(getString(R.string.progress_km), progress)
        val bounds = binding.seekBarLayout.seekbar.thumb.bounds
        val offset = if (progress > progressLimit) paddingRightLimit else paddingRight
        binding.seekBarLayout.tvTrickleIndicator.x =
            (binding.seekBarLayout.seekbar.left + bounds.left - offset).toFloat()
    }

    private fun initializeDescriptionCounter() {
        binding.description.peiContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                updateDescriptionCounter(s.length)
            }
            override fun afterTextChanged(s: Editable) {}
        })
        updateDescriptionCounter(binding.description.peiContent.text?.length ?: 0)
    }

    private fun updateDescriptionCounter(length: Int) {
        binding.description.counter.text = String.format(
            getString(R.string.description_counter),
            length.toString()
        )
        descriptionRegistered = binding.description.peiContent.text.toString()
    }

    private fun setupEditImageButton() {
        binding.editImage.setOnClickListener {
            startActivity(Intent(this, EditPhotoActivity::class.java))
        }
    }

    private fun setupGender() {
        binding.gender.peciLayout.setOnClickListener {
            // CORRECTION ICI : Label "Non renseigné" et Clé "secret"
            val genderOptions = arrayOf(
                getString(R.string.onboard_welcome_gender_female),
                getString(R.string.onboard_welcome_gender_male),
                "Non renseigné"
            )
            val genderKeys = arrayOf("female", "male", "secret")

            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.onboard_welcome_title_gender))
            builder.setItems(genderOptions) { _, which ->
                binding.gender.peciContent.text = genderOptions[which]
                selectedGender = genderKeys[which]
            }
            builder.show()
        }
    }

    private fun setupInterestsButtons() {
        binding.interests.profileSettingsItemLayout.setOnClickListener {
            EnhancedOnboarding.isFromSettingsinterest = true
            startActivity(Intent(this, EnhancedOnboarding::class.java))
            finish()
        }

        binding.personnalize.profileSettingsItemLayout.setOnClickListener {
            MainActivity.isFromProfile = true
            startActivity(Intent(this, EnhancedOnboarding::class.java))
            finish()
        }
    }

    private fun setupActionZoneAutocomplete() {
        val autoCompleteTextView = binding.cityAction

        autocompleteAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line)
        autoCompleteTextView.setAdapter(autocompleteAdapter)
        autoCompleteTextView.threshold = 1

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedPrediction = autocompletePredictions[position]
            fetchPlaceDetails(selectedPrediction.placeId)
            autoCompleteTextView.dismissDropDown()
            autoCompleteTextView.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(autoCompleteTextView.windowToken, 0)
        }

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!autoCompleteTextView.hasFocus()) {
                    autoCompleteTextView.dismissDropDown()
                    return
                }
                val query = s?.toString()?.trim()
                if (query.isNullOrEmpty()) {
                    autoCompleteTextView.dismissDropDown()
                } else {
                    fetchAutocompletePredictions(query)
                }
            }
        })
    }

    private fun setBackButton() {
        binding.header.headerIconBack.setOnClickListener {
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
        val isArabic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0].language == "ar"
        } else {
            resources.configuration.locale.language == "ar"
        }

        with(binding) {
            configureTextDirection(isArabic, firstname.peeiContent)
            configureTextDirection(isArabic, lastname.peeiContent)
            configureTextDirection(isArabic, description.peiContent)
            configureTextDirection(isArabic, birthday.peeiContent)
            configureTextDirection(isArabic, phone.peciContent)
            configureTextDirection(isArabic, gender.peciContent)
            configureTextDirection(isArabic, email.peeiContent)
            configureTextDirection(isArabic, cityAction)

            firstname.peeiContent.setText(user.firstName)
            lastname.peeiContent.setText(user.lastName)

            if (descriptionRegistered.isEmpty()) {
                description.peiContent.setText(user.about)
            } else {
                description.peiContent.setText(descriptionRegistered)
            }

            user.gender?.let {
                selectedGender = it
                binding.gender.peciContent.text = when (it) {
                    "female" -> getString(R.string.onboard_welcome_gender_female)
                    "male" -> getString(R.string.onboard_welcome_gender_male)
                    "secret" -> "Non renseigné" // CORRECTION ICI
                    else -> ""
                }
            }

            birthday.peeiContent.transformIntoDatePicker(
                this@EditProfileActivity,
                dateFormatString
            )

            user.birthday?.let { apiDateStr ->
                try {
                    val date = apiDateFormat.parse(apiDateStr)
                    if (date != null) {
                        birthday.peeiContent.setText(uiDateFormat.format(date))
                    }
                } catch (e: Exception) {
                    birthday.peeiContent.setText(apiDateStr)
                }
            }

            phone.peciContent.setText(user.phone)
            phone.peciContent.setTextColor(ContextCompat.getColor(this@EditProfileActivity, R.color.dark_grey_opacity_40))
            email.peeiContent.setText(user.email)
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
            .setCountries("FR")
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
        if (!isFinishing && !isDestroyed) {
            autocompleteAdapter?.clear()
            autocompleteAdapter?.addAll(suggestions)
            autocompleteAdapter?.notifyDataSetChanged()

            if (binding.cityAction.hasFocus()) {
                binding.cityAction.showDropDown()
            }
        }
    }

    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION, Place.Field.FORMATTED_ADDRESS)
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            binding.cityAction.setText(place.displayName ?: "", false)

            savedLocation = place.location?.let {
                PlaceDetails(
                    place.displayName ?: "",
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
            val firstname = binding.firstname.peeiContent.text.toString().trimEnd()
            val lastname = binding.lastname.peeiContent.text.toString().trimEnd()
            val about = binding.description.peiContent.text.toString().trimEnd()
            val email = binding.email.peeiContent.text.toString().trimEnd()
            val travelDistance = binding.seekBarLayout.seekbar.progress

            val userParams: ArrayMap<String, Any> = ArrayMap()
            userParams["first_name"] = firstname
            userParams["last_name"] = lastname
            userParams["about"] = about
            userParams["email"] = email
            userParams["travel_distance"] = travelDistance
            selectedGender?.let { userParams["gender"] = it }

            val birthdayUI = binding.birthday.peeiContent.text.toString().trim()
            if (birthdayUI.isNotEmpty()) {
                try {
                    val date = uiDateFormat.parse(birthdayUI)
                    if (date != null) {
                        userParams["birthdate"] = apiDateFormat.format(date)
                    }
                } catch (e: Exception) {
                    Timber.e("Erreur parsing date onSave: $e")
                }
            }

            val finalWrapper: ArrayMap<String, Any> = ArrayMap()
            finalWrapper["user"] = userParams

            editProfilePresenter.updateUser(finalWrapper)
        }
    }

    private fun checkEmail(): Boolean {
        if(binding.email.peeiContent.text.isEmpty()){
            return true
        }
        val isEmailCorrect = binding.email.peeiContent.text.trimEnd().isValidEmail()
        with(binding.email) {
            error.root.visibility = if (isEmailCorrect) View.GONE else View.VISIBLE
            error.errorMessage.text = getString(R.string.error_email)
            DrawableCompat.setTint(
                peeiContent.background,
                ContextCompat.getColor(
                    this@EditProfileActivity,
                    if (isEmailCorrect) R.color.light_orange_opacity_50 else R.color.red
                )
            )
        }
        return isEmailCorrect
    }

    private fun checkLastName():Boolean{
        val isLastnameCorrect = binding.lastname.peeiContent.text.trimEnd().length > 2
        with(binding.lastname) {
            error.root.visibility = if (isLastnameCorrect) View.GONE else View.VISIBLE
            error.errorMessage.text = getString(R.string.error_lastname)
            DrawableCompat.setTint(
                peeiContent.background,
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

    private fun checkError(): Boolean {
        val isLastnameCorrect = binding.lastname.peeiContent.text.trimEnd().length > 2
        val isEmailCorrect = binding.email.peeiContent.text.trimEnd().isValidEmail()

        updateErrorUI(binding.lastname, isLastnameCorrect, getString(R.string.error_lastname))
        updateErrorUI(binding.email, isEmailCorrect, getString(R.string.error_email))

        return isLastnameCorrect && isEmailCorrect
    }

    private fun updateErrorUI(
        itemLayout: social.entourage.android.databinding.ProfileEditEditableItemBinding,
        isCorrect: Boolean,
        errorMsg: String
    ) {
        itemLayout.error.root.visibility = if (isCorrect) View.GONE else View.VISIBLE
        itemLayout.error.errorMessage.text = errorMsg
        DrawableCompat.setTint(
            itemLayout.peeiContent.background,
            ContextCompat.getColor(this, if (isCorrect) R.color.light_orange_opacity_50 else R.color.red)
        )
    }

    private fun adjustPaddingForKeyboard() {
        val rootView = binding.root
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val screenHeight = rootView.height
                val keypadHeight = screenHeight - rect.bottom

                if (keypadHeight > screenHeight * 0.15) {
                    binding.scrollView.setPadding(0, 0, 0, keypadHeight)
                } else {
                    binding.scrollView.setPadding(0, 0, 0, 0)
                }
            }
        })
    }

    override fun onUploadError() {
        Timber.e("Error uploading photo")
    }
}