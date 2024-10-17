package social.entourage.android.actions.create

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentCreateActionStepOneBinding
import social.entourage.android.groups.choosePhoto.ChooseGalleryPhotoModalFragment
import social.entourage.android.base.ChoosePhotoModalFragment
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import social.entourage.android.tools.log.AnalyticsEvents

class CreateActionStepOneFragment : Fragment() {
    private var _binding: NewFragmentCreateActionStepOneBinding? = null
    val binding: NewFragmentCreateActionStepOneBinding get() = _binding!!

    private val viewModel: CommunicationActionHandlerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateActionStepOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetValues()
        handleNextButtonState()
        handleChoosePhoto()
        initializeDescriptionCounter()
        setupViewWithEdit()



        if (viewModel.actionEdited == null) {
            if (viewModel.isDemand) {
                AnalyticsEvents.logEvent(AnalyticsEvents.Help_create_demand_1)
            } else {
                AnalyticsEvents.logEvent(AnalyticsEvents.Help_create_contrib_1)
            }
        }
    }


    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (isActionNameValid() && isActionDescriptionValid()) {
                binding.error.root.visibility = View.GONE
                viewModel.isCondition.value = true
                viewModel.action.title(binding.actionName.text.toString())
                viewModel.action.description(binding.actionDescription.text.toString())
                viewModel.clickNext.removeObservers(viewLifecycleOwner)
            } else {
                binding.error.root.visibility = View.VISIBLE
                binding.error.errorMessage.text = getString(R.string.error_mandatory_fields)
                viewModel.isCondition.value = false
            }
        }
    }

    private fun handleNextButtonState() {
        handleEditTextChangedTextListener(binding.actionDescription)
        handleEditTextChangedTextListener(binding.actionName)
    }

    private fun handleEditTextChangedTextListener(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.isButtonClickable.value = isActionNameValid() && isActionDescriptionValid()
                viewModel.action.title = binding.actionName.text.toString()
                viewModel.action.description = binding.actionDescription.text.toString()

            }

            override fun afterTextChanged(s: Editable) {
                viewModel.canExitActionCreation = canExitActionCreation()
            }
        })
    }

    private fun initializeDescriptionCounter() {
        binding.counter.text = String.format(
            getString(R.string.events_description_counter),
            binding.actionDescription.text?.length.toString()
        )
        binding.actionDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.counter.text = String.format(
                    getString(R.string.events_description_counter),
                    s.length.toString()
                )
            }
            override fun afterTextChanged(s: Editable) {}
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.resetValues()
        viewModel.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
        viewModel.isButtonClickable.value = isActionNameValid() && isActionDescriptionValid()
        // Définir les placeholders pour le titre et la description
        val (titlePlaceholder, descriptionPlaceholder) = viewModel.getPlaceholdersForActionType(requireContext())
        binding.actionName.hint = titlePlaceholder
        binding.actionDescription.hint = descriptionPlaceholder
    }

    fun isActionNameValid(): Boolean {
        return binding.actionName.text.length >= Const.GROUP_NAME_MIN_LENGTH && binding.actionName.text.isNotBlank()
    }

    fun isActionDescriptionValid(): Boolean {
        return binding.actionDescription.text.length >= Const.GROUP_DESCRIPTION_MIN_LENGTH && binding.actionDescription.text.isNotBlank()
    }

    fun canExitActionCreation(): Boolean {
        if (viewModel.actionEdited != null) return true

        return binding.actionName.text.isEmpty() && binding.actionDescription.text.isEmpty()
    }

    private fun handleChoosePhoto() {

        if (viewModel.isDemand) {
            binding.uiLayoutAddPhoto.visibility = View.GONE

        }
        else {
            getResult()
            binding.uiLayoutAddPhoto.visibility = View.VISIBLE
            binding.addPhotoLayout.setOnClickListener {
                choosePhoto()
            }
            binding.uiLayoutAddPhoto.setOnClickListener {
                choosePhoto()
            }

            viewModel.imageURI?.let {
                binding.addPhotoLayout.visibility = View.GONE
                binding.addPhoto.visibility = View.VISIBLE
                Glide.with(this)
                    .load(it)
                    .transform(CenterCrop(), RoundedCorners(14.px))
                    .into(binding.addPhoto)
            } ?: kotlin.run {
                binding.addPhotoLayout.visibility = View.VISIBLE
                binding.addPhoto.visibility = View.GONE
            }
        }
    }

    private fun choosePhoto() {
        val choosePhotoModalFragment = ChoosePhotoModalFragment.newInstance()
        choosePhotoModalFragment.show(parentFragmentManager, ChooseGalleryPhotoModalFragment.TAG)
    }

    private fun getResult() {
        parentFragmentManager.setFragmentResultListener(
            Const.REQUEST_KEY_CHOOSE_PHOTO,
            this
        ) { _, bundle ->
            viewModel.imageURI = bundle.getParcelable(Const.CHOOSE_PHOTO)
            viewModel.imageURI?.let {
                binding.addPhotoLayout.visibility = View.GONE
                binding.addPhoto.visibility = View.VISIBLE
                Glide.with(this)
                    .load(it)
                    .transform(CenterCrop(), RoundedCorners(14.px))
                    .into(binding.addPhoto)
            }?: kotlin.run {
                binding.addPhotoLayout.visibility = View.VISIBLE
                binding.addPhoto.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        binding.error.root.visibility = View.GONE
        super.onDestroy()
    }

    private fun setupViewWithEdit() {
        viewModel.actionEdited?.let {
            binding.actionName.setText(viewModel.actionEdited?.title)
            binding.actionDescription.setText(viewModel.actionEdited?.description)

            if (!viewModel.isDemand) {
                viewModel.actionEdited?.imageUrl?.let {
                    binding.addPhotoLayout.visibility = View.GONE
                    binding.addPhoto.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(it)
                        .transform(CenterCrop(), RoundedCorners(14.px))
                        .into(binding.addPhoto)
                }?: kotlin.run {
                    binding.addPhotoLayout.visibility = View.VISIBLE
                    binding.addPhoto.visibility = View.GONE
                }
            }
        }
    }
}