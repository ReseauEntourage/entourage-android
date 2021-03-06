package social.entourage.android.user.edit.place

import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_onboarding_place.*
import kotlinx.android.synthetic.main.layout_view_title.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.api.model.User
import social.entourage.android.tools.log.AnalyticsEvents

class UserEditActionZoneFragment : UserActionPlaceFragment() {
    private var mListener: FragmentListener? = null

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********
    override fun setupViews() {
        super.setupViews()

        if (isSecondaryAddress) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_PROFILE_ACTION_ZONE2)
        }
        else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_PROFILE_ACTION_ZONE)
        }

        ui_onboard_place_tv_title?.text = getString(R.string.profile_edit_zone_title)
        ui_onboard_place_tv_info?.text = getString(R.string.profile_edit_zone_description)

        edit_place_title_layout?.visibility = View.VISIBLE
        edit_place_title_layout?.title_action_button?.setOnClickListener {
            sendNetwork()
        }
        edit_place_title_layout?.title_close_button?.setOnClickListener {
            dismiss()
        }
    }

    override fun onCurrentLocationClicked() {
        super.onCurrentLocationClicked()
        if (isSecondaryAddress) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_SETACTION_ZONE2_GEOLOC)
        }
        else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_SETACTION_ZONE_GEOLOC)
        }
    }

    override fun onSearchCalled() {
        super.onSearchCalled()
        if (isSecondaryAddress) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_SETACTION_ZONE2_SEARCH)
        }
        else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_SETACTION_ZONE_SEARCH)
        }
    }

    fun setupListener(listener: FragmentListener) {
        mListener = listener
    }

    //**********//**********//**********
    // Methods
    //**********//**********//**********

    private fun sendNetwork() {
        userAddress?.let {userAddress ->
            AnalyticsEvents.logEvent(
                    if (isSecondaryAddress) AnalyticsEvents.EVENT_ACTION_PROFILE_ACTION_ZONE2_SUBMIT
                    else AnalyticsEvents.EVENT_ACTION_PROFILE_ACTION_ZONE_SUBMIT
            )
            OnboardingAPI.getInstance().updateAddress(userAddress,isSecondaryAddress) { isOK, userResponse ->
                if (isOK) {
                    userResponse?.user?.let {newUser ->
                        val authenticationController = EntourageApplication.get().components.authenticationController
                        authenticationController.me?.phone?.let { phone ->
                            newUser.phone = phone
                            authenticationController.saveUser(newUser)
                            mListener?.onUserEditActionZoneFragmentAddressSaved()
                            dismissAllowingStateLoss()
                        }
                    }
                    activity?.let {Toast.makeText(it, R.string.user_action_zone_send_ok, Toast.LENGTH_LONG).show()}
                } else {
                    activity?.let {Toast.makeText(it, R.string.user_action_zone_send_failed, Toast.LENGTH_LONG).show()}
                    mListener?.onUserEditActionZoneFragmentIgnore()
                }
            }
        } ?: run {
            mListener?.onUserEditActionZoneFragmentIgnore()
        }
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
        val TAG: String? = UserEditActionZoneFragment::class.java.simpleName

        fun newInstance(googlePlaceAddress: User.Address?,isSecondary:Boolean) =
                UserEditActionZoneFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_PLACE, googlePlaceAddress)
                        putBoolean(ARG_2ND,isSecondary)
                    }
                }
    }
}