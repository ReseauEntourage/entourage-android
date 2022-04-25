package social.entourage.android.new_v8.group

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
import social.entourage.android.api.model.GroupImage
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.databinding.NewFragmentCreateGroupStepThreeBinding
import social.entourage.android.new_v8.user.ReportUserModalFragment
import social.entourage.android.new_v8.utils.Const
import timber.log.Timber


class CreateGroupStepThreeFragment : Fragment() {

    private var _binding: NewFragmentCreateGroupStepThreeBinding? = null
    val binding: NewFragmentCreateGroupStepThreeBinding get() = _binding!!
    private val viewModel: CommunicationHandlerViewModel by activityViewModels()
    private var selectedImage: GroupImage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onFragmentResult()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetStepOne()
        handleChoosePhoto()
        setWelcomeMessage()
    }

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (selectedImage == null) {
                viewModel.isCondition.value = false
                binding.error.root.visibility = View.VISIBLE
                binding.error.errorMessage.text =
                    getString(R.string.error_categories_create_group_image)
            } else {
                viewModel.isCondition.value = true
                binding.error.root.visibility = View.GONE
                viewModel.clickNext.removeObservers(viewLifecycleOwner)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateGroupStepThreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setWelcomeMessage() {
        binding.groupMessageWelcome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.group.welcomeMessage(s.toString())

            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun handleChoosePhoto() {
        val choosePhotoModalFragment = CreateGroupChoosePhotoModalFragment.newInstance()
        binding.addPhotoLayout.setOnClickListener {
            choosePhotoModalFragment.show(parentFragmentManager, ReportUserModalFragment.TAG)
        }
        binding.addPhoto.setOnClickListener {
            choosePhotoModalFragment.show(parentFragmentManager, ReportUserModalFragment.TAG)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.error.root.visibility = View.GONE
    }

    private fun onFragmentResult() {
        setFragmentResultListener(Const.REQUEST_KEY_CHOOSE_PHOTO) { _, bundle ->
            selectedImage = bundle.getParcelable(Const.CHOOSE_PHOTO)
            viewModel.isButtonClickable.value = isCondition()
            viewModel.group.neighborhoodImageId(selectedImage?.id)
            selectedImage?.imageUrl.let { imageUrl ->
                binding.addPhotoLayout.visibility = View.GONE
                binding.addPhoto.visibility = View.VISIBLE
                Glide.with(requireActivity())
                    .load(Uri.parse(imageUrl))
                    .transform(CenterCrop(), RoundedCorners(14))
                    .into(binding.addPhoto)
            }
        }
    }

    private fun isCondition(): Boolean {
        return selectedImage != null
    }

    override fun onResume() {
        super.onResume()
        viewModel.resetStepOne()
        viewModel.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
        viewModel.isButtonClickable.value = isCondition()
    }
}