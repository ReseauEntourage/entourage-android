package social.entourage.android.events.create

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.content.Intent
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import java.io.File
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import android.widget.Toast
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.api.model.Image
import social.entourage.android.databinding.NewFragmentCreateEventStepOneBinding
import social.entourage.android.groups.choosePhoto.ChooseGalleryPhotoModalFragment
import social.entourage.android.groups.choosePhoto.ImagesType
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px

class CreateEventStepOneFragment : Fragment(), EventImageUploadView {

    private var _binding: NewFragmentCreateEventStepOneBinding? = null
    val binding: NewFragmentCreateEventStepOneBinding get() = _binding!!
    private var selectedImage: Image? = null
    private var uploadedImageFile: File? = null
    private lateinit var uploadPresenter: EventImageUploadPresenter
    private var isUploading = false

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // CORRECTION : Générer un nom de fichier unique avec le timestamp
            // Cela empêche Glide de charger l'ancienne image depuis son cache mémoire
            val uniqueFileName = "cropped_event_image_${System.currentTimeMillis()}.jpg"
            val destinationUri = Uri.fromFile(File(requireContext().cacheDir, uniqueFileName))

            val options = UCrop.Options()
            options.setToolbarTitle(getString(R.string.group_choose_photo))
            options.setCircleDimmedLayer(false)

            // CORRECTION : Forcer l'affichage des contrôles pour aider l'UI à se redessiner
            options.setHideBottomControls(true)
            options.setFreeStyleCropEnabled(false)

            UCrop.of(it, destinationUri)
                .withAspectRatio(16f, 9f)
                .withOptions(options)
                .start(requireContext(), this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let { uri ->
                val file = File(uri.path!!)
                uploadedImageFile = file
                selectedImage = null // Clear previously selected default image
                CommunicationHandler.event.entourageImageId(null)

                binding.layout.addPhotoLayout.visibility = View.GONE
                binding.layout.addPhoto.visibility = View.VISIBLE
                Glide.with(requireActivity())
                    .load(uri)
                    .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                    .into(binding.layout.addPhoto)

                CommunicationHandler.isButtonClickable.value =
                    isGroupNameValid() && isGroupDescriptionValid() && isImageValid()

                isUploading = true
                CommunicationHandler.isButtonClickable.value = false
                Toast.makeText(requireContext(), "Upload de l'image en cours...", Toast.LENGTH_SHORT).show()
                uploadPresenter.uploadPhoto(file)
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            cropError?.printStackTrace()
        }
    }

    override fun onUploadError() {
        isUploading = false
        Toast.makeText(requireContext(), "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show()
        CommunicationHandler.isButtonClickable.value = false
    }

    override fun onUploadSuccess(uploadKey: String) {
        isUploading = false
        CommunicationHandler.event.imageUrl(uploadKey)
        CommunicationHandler.isButtonClickable.value =
            isGroupNameValid() && isGroupDescriptionValid() && isImageValid()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateEventStepOneBinding.inflate(inflater, container, false)
        uploadPresenter = EventImageUploadPresenter(this, EventImageUploadRepository())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CommunicationHandler.resetValues()
        setView()
        initializeDescriptionCounter()
        handleChoosePhoto()
        onFragmentResult()
        handleNextButtonState()
        adjustTextViewsForRTL(binding.layout.root)
        if (CommunicationHandler.eventEdited == null) {
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_1)
        }
    }

    private fun adjustTextViewsForRTL(view: View) {
        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        if (isRTL) {
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    val child = view.getChildAt(i)
                    adjustTextViewsForRTL(child) // Récursion pour parcourir toutes les sous-vues
                }
            } else if (view is TextView) {
                // Ajuster la gravité et la direction du texte pour RTL
                view.gravity = View.TEXT_ALIGNMENT_VIEW_END
                view.textDirection = View.TEXT_DIRECTION_RTL
            }
        }
    }

    private fun handleChoosePhoto() {
        val choosePhotoModalFragment = ChooseGalleryPhotoModalFragment.newInstance(ImagesType.EVENTS)
        binding.layout.addPhotoLayout.setOnClickListener {
            choosePhotoModalFragment.show(parentFragmentManager, ChooseGalleryPhotoModalFragment.TAG)
        }
        binding.layout.addPhoto.setOnClickListener {
            choosePhotoModalFragment.show(parentFragmentManager, ChooseGalleryPhotoModalFragment.TAG)
        }
    }

    override fun onResume() {
        super.onResume()
        CommunicationHandler.resetValues()
        CommunicationHandler.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
        CommunicationHandler.isButtonClickable.value =
            isGroupNameValid() && isGroupDescriptionValid() && isImageValid()
    }

    private fun onFragmentResult() {
        setFragmentResultListener(Const.REQUEST_KEY_CHOOSE_PHOTO) { _, bundle ->
            val isAddPhoto = bundle.getBoolean("is_add_photo", false)
            if (isAddPhoto) {
                getContent.launch("image/*")
            } else {
                selectedImage = bundle.getParcelable(Const.CHOOSE_PHOTO_PATH)
                uploadedImageFile = null
                CommunicationHandler.isButtonClickable.value = isImageValid()
                CommunicationHandler.event.entourageImageId(selectedImage?.id)
                CommunicationHandler.event.imageUrl(null)
                val imageUrl =
                    if (selectedImage?.portraitUrl != null) selectedImage?.portraitUrl else selectedImage?.landscapeUrl
                imageUrl?.let { url ->
                    CommunicationHandler.isButtonClickable.value =
                        isGroupNameValid() && isGroupDescriptionValid() && isImageValid()
                    binding.layout.addPhotoLayout.visibility = View.GONE
                    binding.layout.addPhoto.visibility = View.VISIBLE
                    Glide.with(requireActivity())
                        .load(Uri.parse(url))
                        .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                        .into(binding.layout.addPhoto)
                }
            }
        }
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (isGroupNameValid() && isGroupDescriptionValid() && isImageValid()) {
                binding.layout.error.root.visibility = View.GONE
                CommunicationHandler.isCondition.value = true
                CommunicationHandler.event.title(binding.layout.eventName.text.toString())
                CommunicationHandler.event.description(binding.layout.eventDescription.text.toString())
                CommunicationHandler.clickNext.removeObservers(viewLifecycleOwner)
            } else {
                binding.layout.error.root.visibility = View.VISIBLE
                binding.layout.error.errorMessage.text =
                    getString(R.string.error_mandatory_fields)
                CommunicationHandler.isCondition.value = false
            }
        }
    }

    fun isGroupNameValid(): Boolean {
        return binding.layout.eventName.text.length >= Const.GROUP_NAME_MIN_LENGTH && binding.layout.eventName.text.isNotBlank()
    }

    fun isGroupDescriptionValid(): Boolean {
        return binding.layout.eventDescription.text.length >= Const.GROUP_DESCRIPTION_MIN_LENGTH && binding.layout.eventDescription.text.isNotBlank()
    }

    fun isImageValid(): Boolean {
        return (selectedImage != null || uploadedImageFile != null) && !isUploading
    }

    fun canExitEventCreation(): Boolean {
        return binding.layout.eventName.text.isEmpty() && binding.layout.eventDescription.text.isEmpty()
    }

    override fun onDestroy() {
        binding.layout.error.root.visibility = View.GONE
        super.onDestroy()
    }

    private fun handleNextButtonState() {
        handleEditTextChangedTextListener(binding.layout.eventDescription)
        handleEditTextChangedTextListener(binding.layout.eventName)
    }

    private fun handleEditTextChangedTextListener(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                CommunicationHandler.isButtonClickable.value =
                    isGroupNameValid() && isGroupDescriptionValid() && isImageValid()
            }

            override fun afterTextChanged(s: Editable) {
                CommunicationHandler.canExitEventCreation = canExitEventCreation()
            }
        })
    }

    private fun initializeDescriptionCounter() {
        binding.layout.counter.text = String.format(
            getString(R.string.events_description_counter),
            binding.layout.eventDescription.text?.length.toString()
        )
        binding.layout.eventDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.layout.counter.text = String.format(
                    getString(R.string.events_description_counter),
                    s.length.toString()
                )
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun setView() {
        CommunicationHandler.eventEdited?.let { event ->
            with(binding.layout) {
                eventName.setText(event.title)
                eventDescription.setText(event.description)
                addPhotoLayout.visibility = View.GONE
                addPhoto.visibility = View.VISIBLE
                selectedImage = Image()
                event.metadata?.landscapeUrl?.let {
                    Glide.with(requireActivity())
                        .load(Uri.parse(it))
                        .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                        .into(binding.layout.addPhoto)
                }
            }
        }
    }
}