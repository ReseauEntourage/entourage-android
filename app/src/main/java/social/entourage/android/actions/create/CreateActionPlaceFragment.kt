package social.entourage.android.actions.create

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import social.entourage.android.R
import social.entourage.android.user.edit.place.UserActionPlaceFragment

class CreateActionPlaceFragment : UserActionPlaceFragment() {

    private val viewModel: CommunicationActionHandlerViewModel by activityViewModels()

    override fun setupViews() {
        super.setupViews()
        binding.uiOnboardPlaceTvTitle.text = getString(R.string.action_create_location_title, if (viewModel.isDemand) getString(R.string.action_name_demand) else getString(R.string.action_name_contrib))
        binding.uiOnboardPlaceTvInfo.text = getString(R.string.action_create_location_subtitle)

        binding.editPlaceTitleLayout.setTitle("")

        binding.uiOnboardPhoneTvInfo2.visibility = View.INVISIBLE
        binding.editPlaceTitleLayout.visibility = View.VISIBLE
        binding.editPlaceTitleLayout.binding.titleActionButton.setOnClickListener {
            validate()
        }
        binding.editPlaceTitleLayout.binding.titleCloseButton.setOnClickListener {
            dismiss()
            findNavController().popBackStack()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.editPlaceTitleLayout) { view, windowInsets ->
            // Get the insets for the statusBars() type:
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(
                top = insets.top
            )
            // Return the original insets so they aren’t consumed
            windowInsets
        }
    }

    private fun validate() {
        with(viewModel.metadata.value) {
            this?.streetAddress = userAddress?.displayAddress
            this?.latitude = userAddress?.latitude
            this?.longitude = userAddress?.longitude
            this?.googlePlaceId = userAddress?.googlePlaceId ?: ""
            //TODO check if placename had a utility on back before removing it
            //this?.placeName = userAddress?.displayAddress ?: ""
            this?.placeName = ""
        }
        findNavController().popBackStack()
    }
}