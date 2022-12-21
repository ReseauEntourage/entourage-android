package social.entourage.android.new_v8.actions.create

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_select_place.*
import kotlinx.android.synthetic.main.layout_view_title.view.*
import social.entourage.android.R
import social.entourage.android.user.edit.place.UserActionPlaceFragment

class CreateActionPlaceFragment : UserActionPlaceFragment() {
    private val viewModel: CommunicationActionHandlerViewModel by activityViewModels()

    override fun setupViews() {
        super.setupViews()
        ui_onboard_place_tv_title?.text = getString(R.string.action_create_location_title, if (viewModel.isDemand) getString(R.string.action_name_demand) else getString(R.string.action_name_contrib))
        ui_onboard_place_tv_info?.text = getString(R.string.action_create_location_subtitle)

        edit_place_title_layout?.setTitle("")

        ui_onboard_phone_tv_info2?.visibility = View.INVISIBLE
        edit_place_title_layout?.visibility = View.VISIBLE
        edit_place_title_layout?.title_action_button?.setOnClickListener {
            validate()
        }
        edit_place_title_layout?.title_close_button?.setOnClickListener {
            dismiss()
            findNavController().popBackStack()
        }
    }

    private fun validate() {
        with(viewModel.metadata.value) {
            this?.streetAddress = userAddress?.displayAddress
            this?.latitude = userAddress?.latitude
            this?.longitude = userAddress?.longitude
            this?.googlePlaceId = userAddress?.googlePlaceId ?: ""
            this?.placeName = userAddress?.displayAddress ?: ""
        }
        findNavController().popBackStack()
    }
}