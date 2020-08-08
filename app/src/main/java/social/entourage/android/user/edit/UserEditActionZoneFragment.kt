package social.entourage.android.user.edit

import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_onboarding_place.*
import kotlinx.android.synthetic.main.layout_view_title.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.api.model.User
import social.entourage.android.onboarding.OnboardingPlaceFragment
import timber.log.Timber

private const val ARG_PLACE = "place"
private const val ARG_2ND = "is2ndAddress"

class UserEditActionZoneFragment : OnboardingPlaceFragment() {
    private var mListener: FragmentListener? = null
    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isFromProfile = true
        super.onViewCreated(view, savedInstanceState)

        ui_onboard_place_tv_title?.text = getString(R.string.profile_edit_zone_title)
        ui_onboard_place_tv_info?.text = getString(R.string.profile_edit_zone_description)

        edit_place_title_layout?.visibility = View.VISIBLE
        edit_place_title_layout?.title_action_button?.setOnClickListener {
            Timber.d("Click valider")
            sendNetwork()
        }
        edit_place_title_layout?.title_close_button?.setOnClickListener {
            dismiss()
        }
    }

    fun setupListener(listener: FragmentListener?) {
        if (listener == null) return
        mListener = listener
    }

    //**********//**********//**********
    // Methods
    //**********//**********//**********

    private fun sendNetwork() {
        if (userAddress != null) {
            if (isSecondaryAddress) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_PROFILE_ACTION_ZONE2_SUBMIT)
            }
            else {
                EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_PROFILE_ACTION_ZONE_SUBMIT)
            }
            OnboardingAPI.getInstance(EntourageApplication.get()).updateAddress(userAddress!!,isSecondaryAddress) { isOK, userResponse ->
                if (isOK) {
                    val authenticationController = EntourageApplication.get().entourageComponent.authenticationController
                    val me = authenticationController.me
                    if (me != null && userResponse != null) {
                        val newUser = userResponse.user
                        newUser.phone = me.phone

                        authenticationController.saveUser(newUser)
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


    interface FragmentListener {
        fun onUserEditActionZoneFragmentDismiss()
        fun onUserEditActionZoneFragmentAddressSaved()
        fun onUserEditActionZoneFragmentIgnore()
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        const val TAG = "social.entourage.android.user.edit.UserEditActionZoneFragment"//EditUserPlaceFragment::class.java.simpleName
        @JvmStatic
        fun newInstance(googlePlaceAddress: User.Address?,isSecondary:Boolean) =
                UserEditActionZoneFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_PLACE, googlePlaceAddress)
                        putBoolean(ARG_2ND,isSecondary)
                    }
                }
    }
}