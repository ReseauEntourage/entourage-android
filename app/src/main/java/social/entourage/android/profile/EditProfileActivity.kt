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
import androidx.navigation.fragment.findNavController
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
import social.entourage.android.main_filter.MainFilterActivity.Companion
import social.entourage.android.main_filter.MainFilterActivity.Companion.PlaceDetails
import social.entourage.android.profile.editProfile.EditPhotoActivity
import social.entourage.android.profile.editProfile.EditProfilePresenter
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.transformIntoDatePicker
import social.entourage.android.tools.utils.trimEnd
import social.entourage.android.tools.isValidEmail
import social.entourage.android.user.AvatarUpdatePresenter
import social.entourage.android.user.AvatarUploadPresenter
import social.entourage.android.user.AvatarUploadRepository
import social.entourage.android.user.AvatarUploadView
import social.entourage.android.user.edit.photo.PhotoChooseInterface
import social.entourage.android.user.languechoose.ActivityChooseLanguage
import timber.log.Timber
import java.io.File

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
    var savedLocation: PlaceDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewFragmentEditProfileBinding.inflate(layoutInflater)
        initializeSeekBar()
        onEditInterests()
        onEditImage()
        handleLanguageButton()
        onEditActionZone()
        initializeDescriptionCounter()
        setBackButton()
        updateUserView()
        adjustPaddingForKeyboard()
        Places.initialize(applicationContext, getString(R.string.google_api_key))
        placesClient = Places.createClient(this)
        avatarUploadPresenter = AvatarUploadPresenter(
            this, // Puisque EditPhotoActivity implémente AvatarUploadView
            AvatarUploadRepository(),
            profilePresenter
        )
        setAddress()
        setContentView(binding.root)

    }

    override fun onResume() {
        super.onResume()

    }

    private fun setAddress(){
        val user = EntourageApplication.me(this)
        val address = user?.address
        if (address != null) {
            savedLocation = PlaceDetails(address.displayAddress, address.latitude, address.longitude)
        }
    }

    private fun handleLanguageButton() {
        binding.language.layout.setOnClickListener {
            val intent = Intent(this, ActivityChooseLanguage::class.java)
            startActivity(intent)
        }
        binding.language.layout.visibility = View.GONE
    }

    private fun createAddressFromSavedLocation(): User.Address? {
        return savedLocation?.let {
            User.Address(it.lat, it.lng, it.name)
        }
    }

    private fun registerAdress(){
        if(createAddressFromSavedLocation() != null){
            OnboardingAPI.getInstance()
                .updateAddress(createAddressFromSavedLocation()!!, false) { isOK, userResponse ->
                    if (isOK) {
                        userResponse?.user?.let { newUser ->
                            this.let {
                                Toast.makeText(
                                    it,
                                    R.string.user_action_zone_send_ok,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
        }

    }

    private fun setProgressThumb(progress: Int) {
        binding.seekBarLayout.tvTrickleIndicator.text =
            String.format(getString(R.string.progress_km), progress.toString())
        val bounds: Rect = binding.seekBarLayout.seekbar.thumb.dirtyBounds
        val paddingRight = if (progress > progressLimit) paddingRightLimit else paddingRight
        binding.seekBarLayout.tvTrickleIndicator.x =
            (binding.seekBarLayout.seekbar.left + bounds.left - paddingRight).toFloat()
    }

    private fun initializeSeekBar() {
        binding.seekBarLayout.seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressValue = if (progress == 0) 1 else progress
                setProgressThumb(progressValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initializeDescriptionCounter() {
        binding.description.counter.text = String.format(
            getString(R.string.description_counter),
            binding.description.content.text?.length.toString()
        )
        binding.description.content.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.description.counter.text = String.format(
                    getString(R.string.description_counter),
                    s.length.toString()
                )
                descriptionRegistered = binding.description.content.text.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun onEditInterests() {
        binding.interests.layout.setOnClickListener {
            EnhancedOnboarding.isFromSettingsinterest = true
            val intent = Intent(this, EnhancedOnboarding::class.java)
            startActivity(intent)
            finish()
        }
        binding.personnalize.layout.setOnClickListener {
            MainActivity.isFromProfile = true
            val intent = Intent(this, EnhancedOnboarding::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun onEditImage() {
        binding.editImage.setOnClickListener {
            val intent = Intent(this, EditPhotoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onEditActionZone() {
        binding.cityAction.setOnClickListener {
            val autoCompleteTextView = binding.cityAction as AutoCompleteTextView
            autoCompleteTextView.threshold = 1 // Nombre minimum de caractères avant de commencer la recherche
            autoCompleteTextView.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line))
            autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
                val selectedPrediction = autocompletePredictions[position]
                fetchPlaceDetails(selectedPrediction.placeId)
                autoCompleteTextView.dismissDropDown()
            }
            autoCompleteTextView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (!s.isNullOrEmpty()) {
                        fetchAutocompletePredictions(s.toString())
                    }
                }
            })
        }
    }

    private fun clearFocusFromEditTexts() {
        binding.root.clearFocus() // Supprime le focus de la vue racine
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private var autocompletePredictions: List<AutocompletePrediction> = listOf()

    private fun fetchAutocompletePredictions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries("FR") // Limitez les résultats à la France
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            autocompletePredictions = response.autocompletePredictions
            val suggestions = autocompletePredictions.map { it.getFullText(null).toString() }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions)
            (binding.cityAction as AutoCompleteTextView).setAdapter(adapter)
            adapter.notifyDataSetChanged()
        }.addOnFailureListener { exception ->
            Timber.e("PlaceAutocomplete Error: ${exception.message}")
        }
    }

    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            binding.cityAction.setText(place.name) // Met à jour le champ avec le nom sélectionné
            savedLocation = PlaceDetails(
                place.name!!,
                place.latLng!!.latitude,
                place.latLng!!.longitude
            )
        }.addOnFailureListener { exception ->
            Timber.e("PlaceDetails Error: ${exception.message}")
        }
    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener {
            finish()
        }
    }

    private fun updateUserView() {
        val user = EntourageApplication.me(this) ?: return

        val isArabic = resources.configuration.locales[0].language == "ar"

        with(binding) {
            configureTextViewForRTL(firstname.content, isArabic)
            configureTextViewForRTL(lastname.content, isArabic)
            configureTextViewForRTL(description.content, isArabic)
            configureTextViewForRTL(birthday.content, isArabic)
            configureTextViewForRTL(phone.content, isArabic)
            configureTextViewForRTL(email.content, isArabic)
            configureTextViewForRTL(cityAction, isArabic)

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
            phone.content.text = user.phone
            phone.content.setTextColor(ContextCompat.getColor(this@EditProfileActivity, R.color.dark_grey_opacity_40))
            email.content.setText(user.email)
            cityAction.text = Editable.Factory.getInstance().newEditable(user.address?.displayAddress ?: "")
            seekBarLayout.seekbar.progress = user.travelDistance ?: 0

            validate.button.setOnClickListener {
                onSaveProfile()
            }
            seekBarLayout.seekbar.post {
                user.travelDistance?.let { setProgressThumb(it) }
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

    private fun configureTextViewForRTL(textView: TextView, isArabic: Boolean) {
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

    private fun onSaveProfile() {
        val firstname = binding.firstname.content.text.trimEnd()
        val lastname = binding.lastname.content.text.trimEnd()
        val about = binding.description.content.text?.trimEnd()
        val email = binding.email.content.text.trimEnd()
        val birthday = binding.birthday.content.text.trimEnd()
        val travelDistance = binding.seekBarLayout.seekbar.progress
        if (checkError()) {
            val editedUser: ArrayMap<String, Any> = ArrayMap()
            editedUser["first_name"] = firstname
            editedUser["last_name"] = lastname
            editedUser["about"] = about
            editedUser["email"] = email
            editedUser["birthday"] = birthday
            editedUser["travel_distance"] = travelDistance
            editProfilePresenter.updateUser(editedUser)
        }
        registerAdress()
        this.finish()
    }

    private fun adjustPaddingForKeyboard() {
        val rootView = binding.root
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val screenHeight = rootView.height
                val keypadHeight = screenHeight - rect.bottom

                // Si le clavier est visible
                if (keypadHeight > screenHeight * 0.15) {
                    // Ajouter un padding au bas du ScrollView
                    binding.scrollView.setPadding(0, 0, 0, keypadHeight)

                } else {
                    // Réinitialiser le padding lorsque le clavier est fermé
                    binding.scrollView.setPadding(0, 0, 0, 0)
                }
            }
        })
    }


    private fun checkError(): Boolean {
        val isLastnameCorrect = binding.lastname.content.text.trimEnd().length > 2
        val isEmailCorrect = binding.email.content.text.trimEnd().isValidEmail()

        with(binding.lastname) {
            error.root.visibility = if (isLastnameCorrect) View.GONE else View.VISIBLE
            error.errorMessage.text = getString(R.string.error_lastname)
            DrawableCompat.setTint(
                content.background,
                ContextCompat.getColor(this@EditProfileActivity, if (isLastnameCorrect) R.color.light_orange_opacity_50 else R.color.red)
            )
        }

        with(binding.email) {
            error.root.visibility = if (isEmailCorrect) View.GONE else View.VISIBLE
            error.errorMessage.text = getString(R.string.error_email)
            DrawableCompat.setTint(
                content.background,
                ContextCompat.getColor(this@EditProfileActivity, if (isEmailCorrect) R.color.light_orange_opacity_50 else R.color.red)
            )
        }
        return isLastnameCorrect && isEmailCorrect
    }


    override fun onUploadError() {
        Timber.e("Error uploading photo")
    }
}
