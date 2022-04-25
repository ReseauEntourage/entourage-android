package social.entourage.android.new_v8.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.GroupImage
import social.entourage.android.databinding.NewFragmentCreateGroupChoosePhotoModalBinding
import social.entourage.android.new_v8.utils.Const

class CreateGroupChoosePhotoModalFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentCreateGroupChoosePhotoModalBinding? = null
    val binding: NewFragmentCreateGroupChoosePhotoModalBinding get() = _binding!!
    private var photosList: MutableList<GroupImage> = mutableListOf()
    private lateinit var choosePhotoAdapter: ChoosePhotoAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateGroupChoosePhotoModalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleCloseButton()
        initializeInterests()
        handleValidateButton()
        MetaDataRepository.groupImages.observe(requireActivity(), ::getPhotosHandleResponse)
    }

    private fun getPhotosHandleResponse(list: List<GroupImage>) {
        photosList.clear()
        photosList.addAll(list)
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun handleCloseButton() {
        binding.header.iconBack.setOnClickListener {
            dismiss()
        }
    }

    private fun initializeInterests() {
        choosePhotoAdapter = ChoosePhotoAdapter(photosList)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = choosePhotoAdapter
        }
    }

    private fun handleValidateButton() {
        binding.validate.setOnClickListener {
            val image = choosePhotoAdapter.getSelected()
            setFragmentResult(
                Const.REQUEST_KEY_CHOOSE_PHOTO,
                bundleOf(Const.CHOOSE_PHOTO to image)
            )
            dismiss()
        }
    }


    companion object {
        const val TAG = "CreateGroupChoosePhotoModalFragment"
        fun newInstance(): CreateGroupChoosePhotoModalFragment {
            return CreateGroupChoosePhotoModalFragment()
        }
    }
}