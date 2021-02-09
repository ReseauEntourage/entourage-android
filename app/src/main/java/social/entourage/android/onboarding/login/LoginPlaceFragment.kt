package social.entourage.android.onboarding.login

import android.content.Context
import kotlinx.android.synthetic.main.fragment_onboarding_place.*
import social.entourage.android.R
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.user.edit.place.UserActionPlaceFragment

open class LoginPlaceFragment : UserActionPlaceFragment() {
    protected var callback:LoginNextCallback? = null

    //******************************
    // Lifecycle
    //******************************

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? LoginNextCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    //******************************
    // Methods
    //******************************

    override fun setupViews() {
        super.setupViews()

        callback?.updateAddress(userAddress)

        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_LOGIN_ACTION_ZONE)
        val _title = R.string.login_place_title
        val _desc = R.string.login_place_description

        ui_onboard_place_tv_title.text = getString(_title)
        ui_onboard_place_tv_info.text = getString(_desc)
    }

    override fun updateCallback() {
        super.updateCallback()
        callback?.updateAddress(userAddress)
    }

    //******************************
    // Locations Methods
    //******************************

    override fun onCurrentLocationClicked() {
        super.onCurrentLocationClicked()
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_LOGIN_SETACTION_ZONE_GEOLOC)
    }

    //******************************
    // Google place Methods
    //******************************

    override fun onSearchCalled() {
        super.onSearchCalled()

        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_LOGIN_SETACTION_ZONE_SEARCH)
    }

    //******************************
    // Companion
    //******************************

    companion object {
        fun newInstance() = LoginPlaceFragment()
    }
}