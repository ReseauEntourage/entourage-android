package social.entourage.android.groups.create

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import social.entourage.android.R
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.user.edit.place.UserActionPlaceFragment

/** Sélecteur d'adresse du groupe : écrit le lieu dans le formulaire de l'assistant. */
class CreateGroupActionZoneFragment : UserActionPlaceFragment() {

    private val viewModel: CreateGroupViewModel by activityViewModels()

    override fun setupViews() {
        super.setupViews()
        binding.uiOnboardPlaceTvTitle.text = getString(R.string.profile_edit_zone_title)
        binding.uiOnboardPlaceTvInfo.text = getString(R.string.profile_edit_zone_description)
        binding.uiOnboardPhoneTvInfo2.visibility = View.INVISIBLE
        binding.editPlaceTitleLayout.visibility = View.VISIBLE
        binding.editPlaceTitleLayout.binding.titleActionButton.setOnClickListener {
            validate()
        }
        binding.editPlaceTitleLayout.binding.titleCloseButton.setOnClickListener {
            dismiss()
            findNavController().popBackStack()
        }
        updatePaddingTopForEdgeToEdge(binding.editPlaceTitleLayout)
    }

    private fun validate() {
        val address = userAddress ?: return
        with(viewModel.form) {
            this.address = address.displayAddress
            latitude = address.latitude
            longitude = address.longitude
        }
        viewModel.onFormChanged()
        findNavController().popBackStack()
    }
}
