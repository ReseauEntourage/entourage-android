package social.entourage.android.onboarding.onboard

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.user.edit.photo.EditPhotoFragment

class OnboardingPhotoFragment : EditPhotoFragment() {
    private var firstname: String? = null
    private var callback: OnboardingCallback? = null


    init {
        analyticsEventView = AnalyticsEvents.EVENT_VIEW_ONBOARDING_CHOOSE_PHOTO
        analyticsEventActionGallery = AnalyticsEvents.EVENT_ACTION_ONBOARDING_UPLOAD_PHOTO
        analyticsEventActionPhoto = AnalyticsEvents.EVENT_ACTION_ONBOARDING_TAKE_PHOTO
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callback?.updateButtonNext(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            firstname = it.getString(ARG_FIRSTNAME)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onPhotoEdited(photoURI: Uri?, photoSource: Int) {
        super.onPhotoEdited(photoURI, photoSource)
        callback?.updateUserPhoto(pickedImageEditedUri)
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.onboarding.OnboardingPhotoFragment"
        const val ARG_FIRSTNAME = "firstname"

        fun newInstance(firstName: String) =
            OnboardingPhotoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FIRSTNAME, firstName)
                }
            }
    }

    //**********//**********//**********
    // Interface
    //**********//**********//**********
    interface OnboardingCallback {
        val errorMessage: MutableLiveData<String>
        fun updateUserPhoto(imageUri: Uri?)
        fun updateButtonNext(isValid: Boolean)
    }
}