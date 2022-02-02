package social.entourage.android.user.edit.photo

import android.net.Uri
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_onboarding_photo.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.onboarding.OnboardingPhotoFragment

class ChoosePhotoFragment : OnboardingPhotoFragment() {

    private var mListener: PhotoChooseInterface? = null

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isFromProfile = true
        super.onViewCreated(view, savedInstanceState)
        if (context is PhotoChooseInterface) {
            mListener = requireContext() as PhotoChooseInterface
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    //**********//**********//**********
    // methods
    //**********//**********//**********

    override fun setupViews() {
        super.setupViews()

        title_close_button?.setOnClickListener {
            dismiss()
        }
        user_edit_title_layout?.visibility = View.VISIBLE
        user_edit_title_layout?.setTitle(getString(R.string.take_photo_title))
        ui_onboard_photo_tv_title?.visibility = View.INVISIBLE
        ui_onboard_photo_tv_description?.text = getString(R.string.take_photo_description)
    }

    //**********//**********//**********
    // PhotoEditDelegate
    //**********//**********//**********

    override fun onPhotoEdited(photoURI: Uri?, photoSource: Int) {
        super.onPhotoEdited(photoURI, photoSource)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_PHOTO_SUBMIT)
        mListener?.onPhotoChosen(photoURI,photoSource)
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