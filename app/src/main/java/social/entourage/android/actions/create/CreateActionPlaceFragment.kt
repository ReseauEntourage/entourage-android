package social.entourage.android.actions.create

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import social.entourage.android.R
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.user.edit.place.UserActionPlaceFragment

/** Sélecteur d'adresse de l'entraide : écrit le lieu dans le formulaire de l'assistant. */
class CreateActionPlaceFragment : UserActionPlaceFragment() {

    private val viewModel: CreateActionViewModel by activityViewModels()

    override fun setupViews() {
        super.setupViews()
        binding.uiOnboardPlaceTvTitle.text = getString(
            R.string.action_create_location_title,
            if (viewModel.isDemand) getString(R.string.action_name_demand) else getString(R.string.action_name_contrib)
        )
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

        updatePaddingTopForEdgeToEdge(binding.editPlaceTitleLayout)
    }

    private fun validate() {
        with(viewModel.form) {
            address = userAddress?.displayAddress
            latitude = userAddress?.latitude
            longitude = userAddress?.longitude
        }
        viewModel.onFormChanged()
        findNavController().popBackStack()
    }
}
