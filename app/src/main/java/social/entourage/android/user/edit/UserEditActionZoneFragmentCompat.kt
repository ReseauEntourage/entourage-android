package social.entourage.android.user.edit

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.compat.Place
import com.google.android.libraries.places.compat.ui.PlaceAutocomplete
import com.google.android.libraries.places.compat.ui.PlaceSelectionListener
import com.google.android.libraries.places.widget.AutocompleteActivity
import kotlinx.android.synthetic.main.fragment_user_edit_action_zone.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.api.model.User.AddressWrapper
import social.entourage.android.base.EntourageDialogFragment
import timber.log.Timber
import java.util.*

/**
 * Deprecated
 */
@Deprecated(message="Migrate to class UserEditActionZoneFragment", replaceWith = ReplaceWith("UserEditActionZoneFragment", "social.entourage.android.user.edit.UserEditActionZoneFragment"))
class UserEditActionZoneFragmentCompat  : EntourageDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var userAddress: User.Address? = null
    private var saving = false
    private lateinit var autocompleteFragment: SupportPlaceAutocompleteFragment
    private val fragmentListeners: MutableList<FragmentListener> = ArrayList()
    private var isFromLogin = false
    private var isSecondaryAddress = false
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userAddress = it.getSerializable(KEY_USER_ADDRESS) as User.Address?
            isSecondaryAddress = it.getBoolean(KEY_SECONDARY)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_edit_action_zone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //SupportPlaceAutocompleteFragment needs to be init here
        autocompleteFragment = SupportPlaceAutocompleteFragment()
        if (!isFromLogin) {
            //Hide the ignore button if the fragment is not shown from the login screen
            action_zone_ignore_button?.visibility = View.GONE
        }
        title_close_button?.setOnClickListener {onCloseButtonClicked()}
        action_zone_go_button?.setOnClickListener {onSaveButtonClicked()}
        action_zone_ignore_button?.setOnClickListener {onIgnoreButtonClicked()}
        initializeGooglePlaces()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        fragmentListeners.forEach {fragmentListener ->
            fragmentListener.onUserEditActionZoneFragmentDismiss()
        }
    }

    override fun getSlideStyle(): Int {
        return if (isFromLogin) 0 else super.getSlideStyle()
    }

    fun setFragmentListener(fragmentListener: FragmentListener) {
        fragmentListeners.add(fragmentListener)
    }

    fun addFragmentListener(fragmentListener: FragmentListener) {
        fragmentListeners.add(fragmentListener)
    }

    fun removeFragmentListener(fragmentListener: FragmentListener) {
        fragmentListeners.remove(fragmentListener)
    }

    fun setFromLogin(fromLogin: Boolean) {
        isFromLogin = fromLogin
    }

    // ----------------------------------
    // Buttons Handling
    // ----------------------------------
    fun onCloseButtonClicked() {
        dismiss()
        for (fragmentListener in fragmentListeners) {
            fragmentListener.onUserEditActionZoneFragmentDismiss()
        }
    }

    fun onSaveButtonClicked() {
        if (!saving) saveAddress()
    }

    fun onIgnoreButtonClicked() {
        for (fragmentListener in fragmentListeners) {
            fragmentListener.onUserEditActionZoneFragmentIgnore()
        }
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------
    private fun initializeGooglePlaces() {
        userAddress?.let {
            val args = Bundle()
            args.putString(KEY_USER_ADDRESS, it.displayAddress)
            autocompleteFragment.arguments = args
        }
        childFragmentManager.beginTransaction().replace(R.id.place_autocomplete_fragment, autocompleteFragment).commit()

        //TODO be more precise about Bounds EN-432
        autocompleteFragment.setBoundsBias(LatLngBounds(LatLng(42.0, -5.0), LatLng(51.0, 9.0)))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place?) {
                if (place != null) {
                    userAddress = User.Address(place.id)
                }
            }

            override fun onError(status: Status?) {
                if (activity != null) {
                    Toast.makeText(activity, R.string.entourage_location_address_not_found, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun saveAddress() {
        if (userAddress == null) {
            Toast.makeText(activity, R.string.user_action_zone_no_address_error, Toast.LENGTH_SHORT).show()
            return
        }
        saving = true
        val userRequest = get().entourageComponent.userRequest
        val call:Call<AddressWrapper>

        val address: MutableMap<String, Any> = ArrayMap()
        if (userAddress?.googlePlaceId.isNullOrEmpty()) {
            address["latitude"] = userAddress?.latitude ?: 0.0
            address["longitude"] = userAddress?.longitude ?: 0.0
            address["place_name"] = userAddress?.displayAddress ?: ""
        }
        else {
            address["google_place_id"] = userAddress?.googlePlaceId ?: ""
        }

        val request = ArrayMap<String, Any>()
        request["address"] = address

        if (!isSecondaryAddress) {
            call = userRequest.updatePrimaryAddressLocation(request)
        }
        else {
            call = userRequest.updateSecondaryAddressLocation(request)
        }

        call.enqueue(object : Callback<AddressWrapper?> {
            override fun onResponse(call: Call<AddressWrapper?>, response: Response<AddressWrapper?>) {
                if (response.isSuccessful) {
                    response.body()?.address?.let {
                        val authenticationController = get().entourageComponent.authenticationController
                        authenticationController.user?.let { me->
                            if (!isSecondaryAddress) {
                                me.address = it
                            }
                            else {
                                me.addressSecondary = it
                            }
                            authenticationController.saveUser(me)
                        }
                    }
                    for (fragmentListener in fragmentListeners) {
                        fragmentListener.onUserEditActionZoneFragmentAddressSaved()
                    }
                }
                if (activity != null) {
                    Toast.makeText(activity,
                            if (response.isSuccessful) R.string.user_action_zone_send_ok
                            else R.string.user_action_zone_send_failed,
                            Toast.LENGTH_SHORT).show()
                }
                saving = false
            }

            override fun onFailure(call: Call<AddressWrapper?>, t: Throwable) {
                if (activity != null) {
                    Toast.makeText(activity, R.string.user_action_zone_send_failed, Toast.LENGTH_SHORT).show()
                }
                saving = false
            }
        })
    }

    // ----------------------------------
    // Inner classes
    // ----------------------------------
    class SupportPlaceAutocompleteFragment : com.google.android.libraries.places.compat.ui.SupportPlaceAutocompleteFragment() {
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            setHint(getString(R.string.user_action_zone_hint))
            arguments?.let { setText(it.getString(KEY_USER_ADDRESS, ""))
            }
            getView()?.let {
                val autocompleteEditText = it.findViewById<EditText>(com.google.android.libraries.places.R.id.places_autocomplete_search_input)
                autocompleteEditText?.setTextColor(ContextCompat.getColor(it.context, R.color.white))
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
            super.onActivityResult(requestCode, resultCode, intent)
            if (requestCode == 30421 /*AUTOCOMPLETE_REQUEST_CODE*/) {
                if (resultCode == AutocompleteActivity.RESULT_OK) {
                    if (activity == null) return
                    var address = PlaceAutocomplete.getPlace(activity, intent)?.address?.toString() ?: return
                    val lastCommaIndex = address.lastIndexOf(',')
                    if (lastCommaIndex > 0) {
                        //remove the last part, which is the country
                        address = address.substring(0, lastCommaIndex)
                    }
                    setText(address)
                } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    if (activity != null) Timber.e(PlaceAutocomplete.getStatus(activity, intent).statusMessage)
                }
            }
        }
    }

    interface FragmentListener {
        fun onUserEditActionZoneFragmentDismiss()
        fun onUserEditActionZoneFragmentAddressSaved()
        fun onUserEditActionZoneFragmentIgnore()
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        @JvmField
        val TAG = UserEditActionZoneFragmentCompat::class.java.simpleName
        private const val KEY_USER_ADDRESS = "social.entourage.android.KEY_USER_ADDRESS"
        private const val KEY_SECONDARY = "social.entourage.android.KEY_SECONDARY"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param userAddress User Address.
         * @return A new instance of fragment UserEditActionZoneFragmentCompat.
         */
        @JvmStatic
        fun newInstance(userAddress: User.Address?,isSecondaryAddress:Boolean): UserEditActionZoneFragmentCompat {
            val fragment = UserEditActionZoneFragmentCompat()
            val args = Bundle()
            args.putSerializable(KEY_USER_ADDRESS, userAddress)
            args.putBoolean(KEY_SECONDARY,isSecondaryAddress)
            fragment.arguments = args
            return fragment
        }
    }
}