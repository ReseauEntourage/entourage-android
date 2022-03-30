package social.entourage.android.user.edit.photo

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.databinding.FragmentOnboardingPhotoBinding
import social.entourage.android.new_v8.profile.editProfile.EditProfileCallback
import social.entourage.android.onboarding.OnboardingPhotoFragment

class ChoosePhotoFragment : OnboardingPhotoFragment() {

    private var mListener: PhotoChooseInterface? = null
    private var _binding: FragmentOnboardingPhotoBinding? = null
    val binding: FragmentOnboardingPhotoBinding get() = _binding!!
    var editProfileCallback: EditProfileCallback? = null

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isFromProfile = true
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
        binding.header.iconBack.setOnClickListener { findNavController().popBackStack() }
    }


    //**********//**********//**********
    // PhotoEditDelegate
    //**********//**********//**********

    override fun onPhotoEdited(photoURI: Uri?, photoSource: Int) {
        super.onPhotoEdited(photoURI, photoSource)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_PHOTO_SUBMIT)
        mListener?.onPhotoChosen(photoURI, photoSource)
        editProfileCallback?.updateUserPhoto(photoURI)
        dismissAllowingStateLoss()
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        const val TAG = "social.entourage.android.user.edit.photo.UserChoosePhotoFragment"

        fun newInstance() = ChoosePhotoFragment()
    }
}