package social.entourage.android.onboarding

import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.fragment_onboarding_place.*
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.user.edit.place.UserActionPlaceFragment

open class OnboardingPlaceFragment : UserActionPlaceFragment() {

    protected var callback: OnboardingCallback? = null

    //******************************
    // Lifecycle
    //******************************

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun setupViews() {
        super.setupViews()

        callback?.updateAddress(userAddress,isSecondaryAddress)

        if (!isSecondaryAddress) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_ONBOARDING_ACTION_ZONE)
            val _title = if (isSdf) R.string.onboard_place_title_sdf else R.string.onboard_place_title
            val _desc = if (isSdf) R.string.onboard_place_description_sdf else R.string.onboard_place_description

            ui_onboard_place_tv_title.text = getString(_title)
            ui_onboard_place_tv_info.text = getString(_desc)
        }
        else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_ONBOARDING_ACTION_ZONE2)
            val _title = if (isSdf) R.string.onboard_place_title2_sdf else R.string.onboard_place_title2
            val _desc = if (isSdf) R.string.onboard_place_description2_sdf else R.string.onboard_place_description2

            ui_onboard_place_tv_title.text = getString(_title)
            ui_onboard_place_tv_info.text = getString(_desc)
        }

    }

    override fun onCurrentLocationClicked() {
        super.onCurrentLocationClicked()
        if (isSecondaryAddress) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_SETACTION_ZONE2_GEOLOC)
        }
        else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_SETACTION_ZONE_GEOLOC)
        }
    }

    override fun onSearchCalled() {
        super.onSearchCalled()
        if (isSecondaryAddress) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_SETACTION_ZONE2_SEARCH)
        }
        else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_ONBOARDING_SETACTION_ZONE_SEARCH)
        }
    }

    override fun updateCallback() {
        super.updateCallback()
        callback?.updateAddress(userAddress,isSecondaryAddress)
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        fun newInstance(googlePlaceAddress: User.Address?,is2ndAddress:Boolean,isSdf:Boolean) =
                OnboardingPlaceFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_PLACE, googlePlaceAddress)
                        putBoolean(ARG_SDF,isSdf)
                        putBoolean(ARG_2ND,is2ndAddress)
                    }
                }
    }

}

