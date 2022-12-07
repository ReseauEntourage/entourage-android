package social.entourage.android.onboarding.onboard

import android.view.View
import kotlinx.android.synthetic.main.fragment_select_place.*
import kotlinx.android.synthetic.main.layout_view_title.view.*
import social.entourage.android.R
import social.entourage.android.user.edit.place.UserActionPlaceFragment


class OnboardingAddPlaceFragment : UserActionPlaceFragment() {

    var callback:OnboardingChoosePlaceCallback? = null

    override fun setupViews() {
        super.setupViews()
        ui_onboard_place_tv_title?.text = getString(R.string.profile_edit_zone_title)
        ui_onboard_place_tv_info?.text = getString(R.string.profile_edit_zone_description)

        edit_place_title_layout?.visibility = View.VISIBLE
        edit_place_title_layout?.title_action_button?.setOnClickListener {
            validate()
        }
        edit_place_title_layout?.title_close_button?.setOnClickListener {
            dismiss()
        }
    }

    private fun validate() {
        callback?.updatePlace(userAddress)
        dismiss()
    }
}

