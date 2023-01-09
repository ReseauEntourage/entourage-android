package social.entourage.android.events.create

import android.view.View
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_select_place.*
import kotlinx.android.synthetic.main.layout_view_title.view.*
import social.entourage.android.R
import social.entourage.android.user.edit.place.UserActionPlaceFragment

class CreateEventActionZoneFragment : UserActionPlaceFragment() {

    // To be changed
    override fun setupViews() {
        super.setupViews()
        ui_onboard_place_tv_title?.text = getString(R.string.event_location_title)
        ui_onboard_place_tv_info?.text = getString(R.string.event_location_detail)
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
        with(CommunicationHandler.event) {
            metadata?.streetAddress = userAddress?.displayAddress
            latitude = userAddress?.latitude
            longitude = userAddress?.longitude
            metadata?.googlePlaceId = userAddress?.googlePlaceId ?: ""
            metadata?.placeName = userAddress?.displayAddress ?: ""
        }
        findNavController().popBackStack()
    }
}