package social.entourage.android.groups.choosePhoto

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Image
import social.entourage.android.databinding.NewFragmentCreateGroupChoosePhotoModalBinding
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const

private const val SPAN_COUNT = 3

enum class ImagesType {
    GROUPS,
    EVENTS,
}

class ChooseGalleryPhotoModalFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentCreateGroupChoosePhotoModalBinding? = null
    val binding: NewFragmentCreateGroupChoosePhotoModalBinding get() = _binding!!
    private var photosList: MutableList<Image> = mutableListOf()
    private lateinit var choosePhotoAdapter: ChoosePhotoAdapter
    private var imagesType: ImagesType? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateGroupChoosePhotoModalBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_NEW_GROUP_STEP3_PIC_GALLERY
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getImageType()
        handleCloseButton()
        initializeInterests()
        handleValidateButton()
        getImages()

        binding.header.title = getString(R.string.select_photo)

        //Use to force refresh layout
        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            val coordinatorLayout = bottomSheet!!.parent as CoordinatorLayout
            val bottomSheetBehavior: BottomSheetBehavior<*> =
                BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.peekHeight = bottomSheet.height
            coordinatorLayout.parent.requestLayout()
        }
    }

    private fun getPhotosHandleResponse(list: List<Image>) {
        photosList.clear()
        photosList.addAll(list)
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun getImages() {
        when (imagesType) {
            ImagesType.GROUPS -> MetaDataRepository.groupImages.observe(
                requireActivity(),
                ::getPhotosHandleResponse
            )
            ImagesType.EVENTS -> MetaDataRepository.eventsImages.observe(
                requireActivity(),
                ::getPhotosHandleResponse
            )
            else -> {}
        }
    }

    private fun handleCloseButton() {
        AnalyticsEvents.logEvent(
            AnalyticsEvents.ACTION_NEW_GROUP_STEP3_PIC_GALLERY_CLOSE
        )
        binding.header.hbsIconCross.setOnClickListener {
            dismiss()
        }
    }

    private fun getImageType() {
        imagesType = arguments?.getSerializable(Const.IMAGES_TYPE) as ImagesType
    }

    private fun initializeInterests() {
        choosePhotoAdapter = ChoosePhotoAdapter(photosList, imagesType == ImagesType.EVENTS)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, SPAN_COUNT)
            adapter = choosePhotoAdapter
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        AnalyticsEvents.logEvent(
            AnalyticsEvents.ACTION_NEW_GROUP_STEP3_PIC_GALLERY_CLOSE
        )
        super.onDismiss(dialog)
    }

    private fun handleValidateButton() {
        binding.validate.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_NEW_GROUP_STEP3_PIC_GALLERY_VALIDATE
            )
            val image = choosePhotoAdapter.getSelected()
            image?.let {
                setFragmentResult(
                    Const.REQUEST_KEY_CHOOSE_PHOTO,
                    bundleOf(Const.CHOOSE_PHOTO_PATH to it)
                )
                dismiss()
            }
        }
    }

    companion object {
        const val TAG = "ChooseGalleryPhotoModalFragment"
        fun newInstance(type: ImagesType): ChooseGalleryPhotoModalFragment {
            val fragment = ChooseGalleryPhotoModalFragment()
            val args = Bundle()
            args.putSerializable(Const.IMAGES_TYPE, type)
            fragment.arguments = args
            return fragment
        }
    }
}
