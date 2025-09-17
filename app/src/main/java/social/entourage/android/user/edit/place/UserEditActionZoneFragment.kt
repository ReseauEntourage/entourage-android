package social.entourage.android.user.edit.place

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.api.model.User
import social.entourage.android.groups.create.CommunicationHandlerViewModel
import social.entourage.android.home.OnHomeChangeLocationUpdate
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import java.io.IOException

class UserEditActionZoneFragment : UserActionPlaceFragment() {
    private var mListener: FragmentListener? = null

    private val viewModel: CommunicationHandlerViewModel by activityViewModels()

    private var setGroupLocation = false
    private var listener: OnHomeChangeLocationUpdate? = null

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            setGroupLocation = it.getBoolean("setGroupLocation")
        }

        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun setupViews() {
        super.setupViews()

        if (isSecondaryAddress) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_PROFILE_ACTION_ZONE2)
        } else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_PROFILE_ACTION_ZONE)
        }

        binding.uiOnboardPlaceTvTitle.text = getString(R.string.profile_edit_zone_title)
        binding.uiOnboardPlaceTvInfo.text = getString(R.string.profile_edit_zone_description)

        binding.editPlaceTitleLayout.visibility = View.VISIBLE
        binding.editPlaceTitleLayout.binding.titleActionButton.setOnClickListener {
            sendNetwork()
        }
        binding.editPlaceTitleLayout.binding.titleCloseButton.setOnClickListener {
            dismiss()
            if (isAdded && view != null) {
                findNavController().popBackStack()
            } else {
                val intent = Intent(context, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("goDemand", true)
                requireContext().startActivity(intent)
            }
        }

        updatePaddingTopForEdgeToEdge(binding.editPlaceTitleLayout)
    }

    override fun onCurrentLocationClicked() {
        super.onCurrentLocationClicked()
        if (isSecondaryAddress) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_SETACTION_ZONE2_GEOLOC)
        } else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_SETACTION_ZONE_GEOLOC)
        }
    }

    override fun onSearchCalled() {
        super.onSearchCalled()
        if (isSecondaryAddress) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_SETACTION_ZONE2_SEARCH)
        } else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ACTION_PROFILE_SETACTION_ZONE_SEARCH)
        }
    }

    fun setupListener(listener: FragmentListener) {
        mListener = listener
    }

    //**********//**********//**********
    // Methods
    //**********//**********//**********

    private fun sendNetwork() {
        if (setGroupLocation) {
            try {
                val geocoder = Geocoder(requireContext())
                userAddress?.displayAddress?.let { userDisplayAddress->
                    geocoder.getFromLocationName(userDisplayAddress, 1)?.let { addresses ->
                        if (addresses.size > 0) {
                            with(viewModel.group) {
                                latitude = addresses.first().latitude
                                longitude = addresses.first().longitude
                                displayAddress = userDisplayAddress.toString()
                            }
                            mListener?.onUserEditActionZoneFragmentAddressSaved()
                            if (isAdded && view != null) {
                                findNavController().popBackStack()
                            } else {
                                val intent = Intent(context, MainActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.putExtra("goDemand", true)
                                requireContext().startActivity(intent)
                            }
                        }
                    }
                }
            } catch(e: IOException) {
                activity?.let {
                    Toast.makeText(
                        it,
                        R.string.user_action_zone_send_failed,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            userAddress?.let { userAddress ->
                AnalyticsEvents.logEvent(
                    if (isSecondaryAddress) AnalyticsEvents.EVENT_ACTION_PROFILE_ACTION_ZONE2_SUBMIT
                    else AnalyticsEvents.EVENT_ACTION_PROFILE_ACTION_ZONE_SUBMIT
                )
                OnboardingAPI.getInstance()
                    .updateAddress(userAddress, isSecondaryAddress) { isOK, userResponse ->
                        if (isOK) {
                            userResponse?.user?.let { newUser ->
                                val authenticationController =
                                    EntourageApplication.get().authenticationController
                                authenticationController.me?.phone?.let { phone ->
                                    newUser.phone = phone
                                    authenticationController.saveUser(newUser)
                                    mListener?.onUserEditActionZoneFragmentAddressSaved()
                                    if(listener != null) {
                                        val intent = Intent(context, MainActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK )
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        requireContext().startActivity(intent)
                                    }
                                    if (isAdded && view != null) {
                                        findNavController().popBackStack()
                                    } else {
                                        val intent = Intent(context, MainActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        intent.putExtra("goDemand", true)
                                        requireContext().startActivity(intent)
                                    }
                                    dismissAllowingStateLoss()
                                }
                            }
                            activity?.let {
                                Toast.makeText(
                                    it,
                                    R.string.user_action_zone_send_ok,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            activity?.let {
                                Toast.makeText(
                                    it,
                                    R.string.user_action_zone_send_failed,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            mListener?.onUserEditActionZoneFragmentIgnore()
                        }
                    }
            } ?: run {
                mListener?.onUserEditActionZoneFragmentIgnore()
            }
        }
    }

    interface FragmentListener {
        fun onUserEditActionZoneFragmentDismiss()
        fun onUserEditActionZoneFragmentAddressSaved()
        fun onUserEditActionZoneFragmentIgnore()
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        val TAG: String? = UserEditActionZoneFragment::class.java.simpleName

        fun newInstance(googlePlaceAddress: User.Address?, isSecondary: Boolean, listener: OnHomeChangeLocationUpdate) =
            UserEditActionZoneFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PLACE, googlePlaceAddress)
                    putBoolean(ARG_2ND, isSecondary)
                }
                this.listener = listener
            }
    }
}