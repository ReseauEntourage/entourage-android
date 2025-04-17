package social.entourage.android.events.create

import android.view.View
import androidx.navigation.fragment.findNavController
import social.entourage.android.R
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.user.edit.place.UserActionPlaceFragment

class CreateEventActionZoneFragment : UserActionPlaceFragment() {
    // To be changed
    override fun setupViews() {
        super.setupViews()
        binding.uiOnboardPlaceTvTitle.text = getString(R.string.event_location_title)
        binding.uiOnboardPlaceTvInfo.text = getString(R.string.event_location_detail)
        binding.uiOnboardPhoneTvInfo2.visibility = View.INVISIBLE
        binding.editPlaceTitleLayout.visibility = View.VISIBLE
        binding.editPlaceTitleLayout.binding.titleActionButton.setOnClickListener {
            validate()
        }
        binding.editPlaceTitleLayout.binding.titleCloseButton.setOnClickListener {
            dismiss()
            findNavController().popBackStack()
        }
        binding.editPlaceTitleLayout.binding.titleText.text = ""

        updatePaddingTopForEdgeToEdge(binding.editPlaceTitleLayout)
    }

    private fun validate() {
        with(CommunicationHandler.event) {
            metadata?.streetAddress = userAddress?.displayAddress
            latitude = userAddress?.latitude
            longitude = userAddress?.longitude
            metadata?.googlePlaceId = userAddress?.googlePlaceId ?: ""
            //TODO check if palcename had a utility on back before removing it
            //metadata?.placeName = userAddress?.displayAddress ?: ""
            metadata?.placeName = ""

        }
        findNavController().popBackStack()
    }
}