package social.entourage.android.user.edit

import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_onboarding_place.*
import kotlinx.android.synthetic.main.layout_view_title.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.api.model.User
import social.entourage.android.onboarding.OnboardingPlaceFragment
import timber.log.Timber

private const val ARG_PLACE = "place"

class UserEditActionZoneFragment : OnboardingPlaceFragment() {


    private var mListener: UserEditActionZoneFragmentCompat.FragmentListener? = null
    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isFromProfile = true
        super.onViewCreated(view, savedInstanceState)

        edit_place_title_layout?.visibility = View.VISIBLE
        edit_place_title_layout?.title_action_button?.setOnClickListener {
            Timber.d("Click valider")
            sendNetwork()
        }
        edit_place_title_layout?.title_close_button?.setOnClickListener {
            dismiss()
        }
    }

    fun setupListener(listener: UserEditActionZoneFragmentCompat.FragmentListener?) {
        if (listener == null) return
        mListener = listener
    }

    //**********//**********//**********
    // Methods
    //**********//**********//**********

    fun sendNetwork() {
        if (userAddress != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_PROFILE_ACTION_ZONE_SUBMIT)
            OnboardingAPI.getInstance(EntourageApplication.get()).updateAddress(userAddress!!) { isOK, userResponse ->
                if (isOK) {
                    val authenticationController = EntourageApplication.get().entourageComponent.authenticationController
                    val me = authenticationController.user
                    if (me != null && userResponse != null) {
                        me.address = userResponse.address
                        authenticationController.saveUser(me)
                        mListener?.onUserEditActionZoneFragmentAddressSaved()
                        dismiss()
                    }
                    Toast.makeText(activity, R.string.user_action_zone_send_ok, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(activity, R.string.user_action_zone_send_failed, Toast.LENGTH_LONG).show()
                    mListener?.onUserEditActionZoneFragmentIgnore()
                }
            }
            return
        }
        mListener?.onUserEditActionZoneFragmentIgnore()
    }


    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        const val TAG = "social.entourage.android.user.edit.EditUserPlaceFragment"//EditUserPlaceFragment::class.java.simpleName
        @JvmStatic
        fun newInstance(googlePlaceAddress: User.Address?) =
                UserEditActionZoneFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_PLACE, googlePlaceAddress)
                    }
                }
    }
}

interface UserEditPlaceListener {
    fun onUserEditPlaceAddressSaved()
    fun onUserEditPlaceIgnore()
}