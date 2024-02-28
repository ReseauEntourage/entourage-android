package social.entourage.android.onboarding.onboard

import android.util.Log
import android.view.View
import social.entourage.android.R
import social.entourage.android.user.edit.place.UserActionPlaceFragment

class OnboardingAddPlaceFragment : UserActionPlaceFragment() {

    var callback:OnboardingChoosePlaceCallback? = null

    override fun setupViews() {
        super.setupViews()
        binding.uiOnboardPlaceTvTitle.text = getString(R.string.profile_edit_zone_title)
        binding.uiOnboardPlaceTvInfo.text = getString(R.string.profile_edit_zone_description)

        binding.editPlaceTitleLayout.visibility = View.VISIBLE
        binding.editPlaceTitleLayout.binding.titleActionButton.setOnClickListener {
            validate()
            dismiss()
        }
    }

    private fun validate() {
        callback?.updatePlace(userAddress)
        dismiss()
    }
}

