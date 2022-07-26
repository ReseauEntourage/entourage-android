package social.entourage.android.new_v8.events.create

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.api.model.Image
import social.entourage.android.databinding.NewFragmentCreateEventStepOneBinding
import social.entourage.android.new_v8.groups.choosePhoto.ChoosePhotoModalFragment
import social.entourage.android.new_v8.groups.choosePhoto.ImagesType
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.px
import timber.log.Timber


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
        initializeDescriptionCounter()
        handleChoosePhoto()
        onFragmentResult()
        handleNextButtonState()
    }

    private fun handleChoosePhoto() {
        val choosePhotoModalFragment = ChoosePhotoModalFragment.newInstance(ImagesType.EVENTS)
        binding.layout.addPhotoLayout.setOnClickListener {
            choosePhotoModalFragment.show(parentFragmentManager, ChoosePhotoModalFragment.TAG)
        }
        binding.layout.addPhoto.setOnClickListener {
            choosePhotoModalFragment.show(parentFragmentManager, ChoosePhotoModalFragment.TAG)
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
            // viewModel.isButtonClickable.value = imageHasBeenSelected()
            // viewModel.group.neighborhoodImageId(selectedImage?.id)
            val imageUrl =
                if (selectedImage?.imageUrl != null) selectedImage?.imageUrl else selectedImage?.landscapeSmallUrl
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
            if (isGroupNameValid() && isGroupDescriptionValid()) {
                if (isImageValid()) binding.layout.errorImage.root.visibility = View.GONE
                binding.layout.error.root.visibility = View.GONE
                CommunicationHandler.isCondition.value = true
                CommunicationHandler.clickNext.removeObservers(viewLifecycleOwner)
            } else {
                if (!isImageValid()) {
                    binding.layout.errorImage.root.visibility = View.VISIBLE
                    binding.layout.errorImage.errorMessage.text =
                        getString(R.string.image_mandatory)
                }
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

    fun canExitGroupCreation(): Boolean {
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
                CommunicationHandler.canExitEventCreation = canExitGroupCreation()
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
}