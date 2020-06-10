package social.entourage.android.user.edit

import android.net.Uri
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_onboarding_photo.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.R
import social.entourage.android.onboarding.OnboardingPhotoFragment
import social.entourage.android.user.edit.photo.PhotoChooseInterface

class UserChoosePhotoFragment : OnboardingPhotoFragment() {

    private var mListener: PhotoChooseInterface? = null

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        mListener?.onPhotoChosen(photoURI,photoSource)
        dismiss()
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        const val TAG = "social.entourage.android.user.edit.UserChoosePhotoFragment"

        @JvmStatic
        fun newInstance() = UserChoosePhotoFragment()
    }
}