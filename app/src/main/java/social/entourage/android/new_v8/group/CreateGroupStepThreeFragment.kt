package social.entourage.android.new_v8.group

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.databinding.NewFragmentCreateGroupStepThreeBinding
import social.entourage.android.new_v8.user.ReportUserModalFragment
import social.entourage.android.new_v8.utils.Const


class CreateGroupStepThreeFragment : Fragment() {

    private var _binding: NewFragmentCreateGroupStepThreeBinding? = null
    val binding: NewFragmentCreateGroupStepThreeBinding get() = _binding!!
    private val viewModel: CommunicationHandlerViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onFragmentResult()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleChoosePhoto()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateGroupStepThreeBinding.inflate(inflater, container, false)
        return binding.root
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
            val selectedImage = bundle.getString(Const.CHOOSE_PHOTO)
            selectedImage?.let { imageUrl ->
                binding.addPhotoLayout.visibility = View.GONE
                binding.addPhoto.visibility = View.VISIBLE
                Glide.with(requireActivity())
                    .load(Uri.parse(imageUrl))
                    .transform(CenterCrop(), RoundedCorners(14))
                    .into(binding.addPhoto)
            }
        }
    }
}