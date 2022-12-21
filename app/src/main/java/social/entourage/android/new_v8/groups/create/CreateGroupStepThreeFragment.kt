package social.entourage.android.new_v8.groups.create

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
import social.entourage.android.new_v8.groups.choosePhoto.ChoosePhotoModalFragment
import social.entourage.android.new_v8.groups.choosePhoto.ImagesType
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.px
import social.entourage.android.tools.log.AnalyticsEvents

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
                binding.layout.error.root.visibility = View.VISIBLE
                binding.layout.error.errorMessage.text =
                    getString(R.string.error_categories_create_group_image)
            } else {
                viewModel.isCondition.value = true
                binding.layout.error.root.visibility = View.GONE
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
        binding.layout.groupMessageWelcome.addTextChangedListener(object : TextWatcher {
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
        val choosePhotoModalFragment = ChoosePhotoModalFragment.newInstance(ImagesType.GROUPS)
        binding.layout.addPhotoLayout.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_NEW_GROUP_STEP3_ADD_PICTURE)
            choosePhotoModalFragment.show(parentFragmentManager, ChoosePhotoModalFragment.TAG)
        }
        binding.layout.addPhoto.setOnClickListener {
            choosePhotoModalFragment.show(parentFragmentManager, ChoosePhotoModalFragment.TAG)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.layout.error.root.visibility = View.GONE
    }

    private fun onFragmentResult() {
        setFragmentResultListener(Const.REQUEST_KEY_CHOOSE_PHOTO) { _, bundle ->
            selectedImage = bundle.getParcelable(Const.CHOOSE_PHOTO_PATH)
            viewModel.isButtonClickable.value = imageHasBeenSelected()
            viewModel.group.neighborhoodImageId(selectedImage?.id)
            selectedImage?.imageUrl.let { imageUrl ->
                binding.layout.addPhotoLayout.visibility = View.GONE
                binding.layout.addPhoto.visibility = View.VISIBLE
                Glide.with(requireActivity())
                    .load(Uri.parse(imageUrl))
                    .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                    .into(binding.layout.addPhoto)
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