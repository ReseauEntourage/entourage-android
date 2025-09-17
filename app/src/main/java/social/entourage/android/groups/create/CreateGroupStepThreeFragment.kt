package social.entourage.android.groups.create

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.api.model.Image
import social.entourage.android.databinding.NewFragmentCreateGroupStepThreeBinding
import social.entourage.android.groups.choosePhoto.ChooseGalleryPhotoModalFragment
import social.entourage.android.groups.choosePhoto.ImagesType
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px

class CreateGroupStepThreeFragment : Fragment() {

    private var _binding: NewFragmentCreateGroupStepThreeBinding? = null
    val binding: NewFragmentCreateGroupStepThreeBinding get() = _binding!!
    private val viewModel: CommunicationHandlerViewModel by activityViewModels()
    private var selectedImage: Image? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onFragmentResult()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetValues()
        handleChoosePhoto()
        setWelcomeMessage()
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (selectedImage == null) {
                viewModel.isCondition.value = false
                binding.layout.egs3Error.root.visibility = View.VISIBLE
                binding.layout.egs3Error.errorMessage.text =
                    getString(R.string.error_categories_create_group_image)
            } else {
                viewModel.isCondition.value = true
                binding.layout.egs3Error.root.visibility = View.GONE
                viewModel.clickNext.removeObservers(viewLifecycleOwner)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateGroupStepThreeBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_NEW_GROUP_STEP3)
        return binding.root
    }

    private fun setWelcomeMessage() {
        binding.layout.egs3GroupMessageWelcome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                viewModel.group.welcomeMessage(s.toString())
            }
        })
    }

    private fun handleChoosePhoto() {
        val choosePhotoModalFragment = ChooseGalleryPhotoModalFragment.newInstance(ImagesType.GROUPS)
        binding.layout.egs3AddPhotoLayout.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_NEW_GROUP_STEP3_ADD_PICTURE)
            choosePhotoModalFragment.show(parentFragmentManager, ChooseGalleryPhotoModalFragment.TAG)
        }
        binding.layout.egs3AddPhoto.setOnClickListener {
            choosePhotoModalFragment.show(parentFragmentManager, ChooseGalleryPhotoModalFragment.TAG)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.layout.egs3Error.root.visibility = View.GONE
    }

    private fun onFragmentResult() {
        setFragmentResultListener(Const.REQUEST_KEY_CHOOSE_PHOTO) { _, bundle ->
            selectedImage = bundle.getParcelable(Const.CHOOSE_PHOTO_PATH)
            viewModel.isButtonClickable.value = imageHasBeenSelected()
            viewModel.group.neighborhoodImageId(selectedImage?.id)
            selectedImage?.imageUrl.let { imageUrl ->
                binding.layout.egs3AddPhotoLayout.visibility = View.GONE
                binding.layout.egs3AddPhoto.visibility = View.VISIBLE
                Glide.with(requireActivity())
                    .load(Uri.parse(imageUrl))
                    .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                    .into(binding.layout.egs3AddPhoto)
            }
        }
    }

    private fun imageHasBeenSelected(): Boolean {
        return selectedImage != null
    }

    override fun onResume() {
        super.onResume()
        viewModel.resetValues()
        viewModel.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
        viewModel.isButtonClickable.value = imageHasBeenSelected()
    }
}