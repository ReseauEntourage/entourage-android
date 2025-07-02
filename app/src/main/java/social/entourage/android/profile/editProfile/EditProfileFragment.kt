package social.entourage.android.profile.editProfile

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.databinding.ActivityEditProfileBinding
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.profile.ProfileActivity
import social.entourage.android.tools.isValidEmail
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.transformIntoDatePicker
import social.entourage.android.tools.utils.trimEnd
import social.entourage.android.user.AvatarUpdatePresenter
import social.entourage.android.user.AvatarUploadPresenter
import social.entourage.android.user.AvatarUploadRepository
import social.entourage.android.user.AvatarUploadView
import social.entourage.android.user.edit.photo.PhotoChooseInterface
import social.entourage.android.user.edit.place.UserEditActionZoneFragment
import social.entourage.android.user.languechoose.ActivityChooseLanguage
import timber.log.Timber
import java.io.File

class EditProfileFragment : Fragment(), EditProfileCallback,
    UserEditActionZoneFragment.FragmentListener {

    private var _binding: ActivityEditProfileBinding? = null
    val binding: ActivityEditProfileBinding get() = _binding!!
    private var mListener: PhotoChooseInterface? = null
    private val paddingRight = 20
    private val paddingRightLimit = 60
    private val progressLimit = 96
    private var descriptionRegistered = ""


    private lateinit var avatarUploadPresenter: AvatarUploadPresenter
    private val editProfilePresenter: EditProfilePresenter by lazy { EditProfilePresenter() }

    var fromHomePage = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        updateUserView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeSeekBar()
        onEditInterests()
        onEditImage()
        handlelanguageButton()
        onEditActionZone()
        initializeDescriptionCounter()
        fromHomePage = activity?.intent?.extras?.getBoolean(Const.GO_TO_EDIT_PROFILE) == true
        setBackButton()
        editProfilePresenter.isUserUpdated.observe(requireActivity(), ::handleUpdateResponse)

        avatarUploadPresenter = AvatarUploadPresenter(
            (activity as AvatarUploadView),
            AvatarUploadRepository(),
            (activity as ProfileActivity).profilePresenter as AvatarUpdatePresenter
        )
        (context as? PhotoChooseInterface)?.let { mListener = it }
    }

    private fun handlelanguageButton(){
        binding.language.peciLayout.setOnClickListener {
            val intent = Intent(requireContext(), ActivityChooseLanguage::class.java)
            requireActivity().startActivity(intent)
        }
        binding.language.peciLayout.visibility = View.GONE
    }

    private fun handleUpdateResponse(success: Boolean) {
        try {
            if (success) findNavController().popBackStack()
        } catch (e: IllegalStateException) {
            Timber.d(e)
        }
    }

    private fun setProgressThumb(progress: Int) {
        binding.seekBarLayout.tvTrickleIndicator.text =
            String.format(
                getString(R.string.progress_km),
                progress.toString()
            )
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

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initializeDescriptionCounter() {
        binding.description.counter.text = String.format(
            getString(R.string.description_counter),
            binding.description.content.text?.length.toString()
        )
        binding.description.content.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.description.counter.text = String.format(
                    getString(R.string.description_counter),
                    s.length.toString()
                )
                descriptionRegistered = binding.description.content.text.toString()
            }

            override fun afterTextChanged(s: Editable) { }
        })
    }

    private fun onEditInterests() {
        binding.interests.layout.setOnClickListener {
            EnhancedOnboarding.isFromSettingsinterest = true
            val intent = Intent(requireContext(), EnhancedOnboarding::class.java)
            startActivity(intent)
            requireActivity().finish()

        }
        binding.personnalize.layout.setOnClickListener {
            //Launch Enhanced Onboarding activity
            MainActivity.isFromProfile = true
            val intent = Intent(requireContext(), EnhancedOnboarding::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun onEditImage() {
        binding.editImage.setOnClickListener {
            val intent = Intent(context, EditPhotoActivity::class.java).apply {
            }
            startActivity(intent)
        }
    }

    private fun onEditActionZone() {
        binding.cityAction.setOnClickListener {
            val action =
                EditProfileFragmentDirections.actionEditProfileFragmentToEditActionZoneFragment(
                    false
                )
            findNavController().navigate(action)
        }
    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener {
            if (fromHomePage)
                activity?.finish()
            else
                findNavController().popBackStack()
        }
    }

    private fun updateUserView() {
        if (isAdded) {
            val user = EntourageApplication.me(activity) ?: return

            // Vérification de la langue
            val isArabic = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                resources.configuration.locales[0].language == "ar"
            } else {
                resources.configuration.locale.language == "ar"
            }

            with(binding) {
                // Configuration des champs en fonction de la langue
                configureTextViewForRTL(firstname.peeiContent, isArabic)
                configureTextViewForRTL(lastname.peeiContent, isArabic)
                configureTextViewForRTL(description.content, isArabic)
                configureTextViewForRTL(birthday.peeiContent, isArabic)
                configureTextViewForRTL(phone.peciContent, isArabic)
                configureTextViewForRTL(email.peeiContent, isArabic)
                configureTextViewForRTL(cityAction, isArabic)
                configureTextViewForRTL(description.counter,isArabic)
                firstname.peeiContent.setText(user.firstName)
                lastname.peeiContent.setText(user.lastName)

                if (descriptionRegistered.isEmpty()) {
                    description.content.setText(user.about)
                } else {
                    description.content.setText(descriptionRegistered)
                }

                birthday.peeiContent.transformIntoDatePicker(
                    requireContext(),
                    getString(R.string.birthday_date_format)
                )
                birthday.peeiContent.setText(user.birthday)
                phone.peciContent.text = user.phone
                phone.peciContent.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.dark_grey_opacity_40
                    )
                )
                phone.peciDivider.visibility = View.GONE
                email.peeiContent.setText(user.email)
                cityAction.text = Editable.Factory.getInstance().newEditable(user.address?.displayAddress ?: "")
                seekBarLayout.seekbar.progress = user.travelDistance ?: 0
                validate.button.setOnClickListener { onSaveProfile() }
                seekBarLayout.seekbar.post {
                    user.travelDistance?.let { setProgressThumb(it) }
                }
                user.avatarURL?.let { avatarURL ->
                    Glide.with(requireActivity())
                        .load(Uri.parse(avatarURL))
                        .placeholder(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(imageProfile)
                } ?: run {
                    imageProfile.setImageResource(R.drawable.placeholder_user)
                }
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

    override fun updateUserPhoto(imageUri: Uri?) {
        imageUri?.path?.let { path ->
            Glide.with(this)
                .load(path)
                .placeholder(R.drawable.placeholder_user)
                .circleCrop()
                .into(binding.imageProfile)
            //Upload the photo to Amazon S3
            avatarUploadPresenter.uploadPhoto(File(path))
        }
    }

    private fun onSaveProfile() {
        val firstname = binding.firstname.peeiContent.text.trimEnd()
        val lastname = binding.lastname.peeiContent.text.trimEnd()
        val about = binding.description.content.text?.trimEnd()
        val email = binding.email.peeiContent.text.trimEnd()
        val birthday = binding.birthday.peeiContent.text.trimEnd()
        val travelDistance = binding.seekBarLayout.seekbar.progress
        val editedUser: ArrayMap<String, Any> = ArrayMap()
        editedUser["first_name"] = firstname
        editedUser["about"] = about
        if(checkEmail()){
            editedUser["email"] = email
        }
        if(checkLastName()){
            editedUser["last_name"] = lastname
        }
        editedUser["birthday"] = birthday
        editedUser["travel_distance"] = travelDistance
        editProfilePresenter.updateUser(editedUser)

    }

    private fun checkEmail():Boolean {
        val isEmailCorrect = binding.email.peeiContent.text.trimEnd().isValidEmail()
        with(binding.email) {
            error.root.visibility = if (isEmailCorrect) View.GONE else View.VISIBLE
            error.errorMessage.text = getString(R.string.error_email)
            DrawableCompat.setTint(
                peeiContent.background,
                ContextCompat.getColor(
                    requireContext(),
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
                    requireContext(),
                    if (isLastnameCorrect) R.color.light_orange_opacity_50 else R.color.red
                )
            )
        }
        return isLastnameCorrect
    }

    private fun checkError(): Boolean {
        val isLastnameCorrect = binding.lastname.peeiContent.text.trimEnd().length > 2
        val isEmailCorrect = binding.email.peeiContent.text.trimEnd().isValidEmail()

        with(binding.lastname) {
            error.root.visibility = if (isLastnameCorrect) View.GONE else View.VISIBLE
            error.errorMessage.text = getString(R.string.error_lastname)
            DrawableCompat.setTint(
                peeiContent.background,
                ContextCompat.getColor(
                    requireContext(),
                    if (isLastnameCorrect) R.color.light_orange_opacity_50 else R.color.red
                )
            )
        }

        with(binding.email) {
            error.root.visibility = if (isEmailCorrect) View.GONE else View.VISIBLE
            error.errorMessage.text = getString(R.string.error_email)
            DrawableCompat.setTint(
                peeiContent.background,
                ContextCompat.getColor(
                    requireContext(),
                    if (isEmailCorrect) R.color.light_orange_opacity_50 else R.color.red
                )
            )
        }
        return isLastnameCorrect && isEmailCorrect
    }

    override fun onUserEditActionZoneFragmentDismiss() {
    }

    override fun onUserEditActionZoneFragmentAddressSaved() {
        editProfilePresenter.storeActionZone(false)
        findNavController().popBackStack()
    }

    override fun onUserEditActionZoneFragmentIgnore() {
        editProfilePresenter.storeActionZone(true)
        findNavController().popBackStack()
    }
}

interface EditProfileCallback {
    fun updateUserPhoto(imageUri: Uri?)
}