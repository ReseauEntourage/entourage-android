package social.entourage.android.user.edit.photo

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.profile.editProfile.EditProfileCallback
import social.entourage.android.tools.log.AnalyticsEvents

class ChooseProfilePhotoFragment : EditPhotoFragment() {

    private var mListener: PhotoChooseInterface? = null
    var editProfileCallback: EditProfileCallback? = null

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (context is PhotoChooseInterface) {
            mListener = requireContext() as PhotoChooseInterface
        }
        setBackButton()
        updateUserView()
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    //**********//**********//**********
    // methods
    //**********//**********//**********

    private fun updateUserView() {
        val user = EntourageApplication.me(activity) ?: return
        user.avatarURL?.let { avatarURL ->
            Glide.with(this)
                .load(Uri.parse(avatarURL))
                .placeholder(R.drawable.ic_user_photo_small)
                .circleCrop()
                .into(binding.imageProfile)
        } ?: run {
            binding.imageProfile.setImageResource(R.drawable.ic_user_photo_small)
        }
    }

    private fun setBackButton() {
        binding.header.headerIconBack.setOnClickListener {
            if (editProfileCallback != null) {
                dismiss()
                return@setOnClickListener
            }

            findNavController().popBackStack()
        }
    }

    //**********//**********//**********
    // PhotoEditInterface
    //**********//**********//**********

    override fun onPhotoEdited(photoURI: Uri?, photoSource: Int) {
        super.onPhotoEdited(photoURI, photoSource)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_PHOTO_SUBMIT)
        mListener?.onPhotoChosen(photoURI, photoSource)
        editProfileCallback?.updateUserPhoto(photoURI)
        dismissAllowingStateLoss()
    }

    init {
        analyticsEventView = AnalyticsEvents.EVENT_VIEW_PROFILE_CHOOSE_PHOTO
        analyticsEventActionGallery = AnalyticsEvents.EVENT_ACTION_PROFILE_UPLOAD_PHOTO
        analyticsEventActionPhoto = AnalyticsEvents.EVENT_ACTION_PROFILE_TAKE_PHOTO
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        const val TAG = "social.entourage.android.user.edit.photo.ChooseProfilePhotoFragment"
    }
}