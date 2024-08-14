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
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.api.model.Image
import social.entourage.android.databinding.NewFragmentCreateEventStepOneBinding
import social.entourage.android.groups.choosePhoto.ChooseGalleryPhotoModalFragment
import social.entourage.android.groups.choosePhoto.ImagesType
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import social.entourage.android.tools.log.AnalyticsEvents

class CreateEventStepOneFragment : Fragment() {

    private var _binding: NewFragmentCreateEventStepOneBinding? = null
    val binding: NewFragmentCreateEventStepOneBinding get() = _binding!!
    private var selectedImage: Image? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateEventStepOneBinding.inflate(inflater, container, false)
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
            selectedImage = bundle.getParcelable(Const.CHOOSE_PHOTO_PATH)
            CommunicationHandler.isButtonClickable.value = isImageValid()
            CommunicationHandler.event.entourageImageId(selectedImage?.id)
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
        return selectedImage != null
    }

    fun canExitEventCreation(): Boolean {
        return binding.layout.eventName.text.isEmpty() && binding.layout.eventDescription.text.isEmpty()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.layout.error.root.visibility = View.GONE
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