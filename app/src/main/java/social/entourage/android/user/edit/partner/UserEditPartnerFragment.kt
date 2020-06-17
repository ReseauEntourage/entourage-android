package social.entourage.android.user.edit.partner

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import kotlinx.android.synthetic.main.fragment_user_edit_partner.*
import kotlinx.android.synthetic.main.layout_view_title.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.EntourageApplication.Companion.me
import social.entourage.android.R
import social.entourage.android.api.model.Partner
import social.entourage.android.api.model.Partner.PartnerWrapper
import social.entourage.android.api.model.Partner.PartnersWrapper
import social.entourage.android.api.model.User
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.user.edit.UserEditFragment

/**
 *
 * Edit the association that an user supports
 *
 */
class UserEditPartnerFragment  : EntourageDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var adapter: UserEditPartnerAdapter = UserEditPartnerAdapter()
    private var user: User? = null
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_edit_partner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureView()
        title_close_button.setOnClickListener {onCloseButtonClicked()}
        title_action_button.setOnClickListener {onSaveButtonClicked()}
    }

    private fun configureView() {
        (parentFragmentManager.findFragmentByTag(UserEditFragment.TAG) as UserEditFragment?)?.let {userEditFragment ->
            user = userEditFragment.editedUser
        } ?: run { user = me(activity)}

        // Configure the partners list
        user_edit_partner_listview?.adapter = adapter

        // Initialize the search field
        user_edit_partner_search?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            var hideKeyboard = false
            if (event == null) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard = true
                }
            } else if (event.keyCode == KeyEvent.ACTION_DOWN) {
                hideKeyboard = true
            }
            if (hideKeyboard) {
                // hide virtual keyboard
                (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(v.windowToken,
                        InputMethodManager.RESULT_UNCHANGED_SHOWN)
                return@OnEditorActionListener true
            }
            false
        })

        // retrieve the list of partners
        getAllPartners()
    }

    // ----------------------------------
    // Buttons Handling
    // ----------------------------------
    fun onCloseButtonClicked() {
        dismiss()
    }

    fun onSaveButtonClicked() {
        val position = adapter.selectedPartnerPosition
        user?.partner?.let {oldPartner->
            removePartner(oldPartner, if (position != AdapterView.INVALID_POSITION) adapter.getItem(position) else null)
        } ?: run  {
            // This user has no partner, so check only if the use has selected one
            if (position != AdapterView.INVALID_POSITION) {
                // add the partner to the user
                adapter.getItem(position)?.let { addPartner(it)}
            }
        }
    }

    // ----------------------------------
    // Network
    // ----------------------------------
    private fun getAllPartners() {
        get(context).entourageComponent.partnerRequest.allPartners.enqueue(object : Callback<PartnersWrapper> {
            override fun onResponse(call: Call<PartnersWrapper>, response: Response<PartnersWrapper>) {
                if (response.isSuccessful) {
                    response.body()?.partners?.let { partnerList ->
                        adapter.partnerList = partnerList
                        for (i in partnerList.indices) {
                            if (partnerList[i].isDefault) {
                                user_edit_partner_listview?.setItemChecked(i, true)
                                adapter.selectedPartnerPosition = i
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PartnersWrapper>, t: Throwable) {}
        })
    }

    private fun addPartner(partner: Partner) {
        val userID = user?.id ?: return
        user_edit_partner_progressBar?.visibility = View.VISIBLE
        get(context).entourageComponent.userRequest.addPartner(userID, PartnerWrapper(partner))
                .enqueue(object : Callback<PartnerWrapper> {
            override fun onResponse(call: Call<PartnerWrapper>, response: Response<PartnerWrapper>) {
                user_edit_partner_progressBar?.visibility = View.GONE
                if (response.isSuccessful) {
                    val authenticationController = get(context).entourageComponent.authenticationController
                    authenticationController.user?.let { user ->
                        response.body()?.partner?.let {
                            user.partner = it
                            authenticationController.saveUser(user)
                        }
                 }
                    Toast.makeText(activity, R.string.partner_add_ok, Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(context, R.string.partner_add_error, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PartnerWrapper>, t: Throwable) {
                user_edit_partner_progressBar?.visibility = View.GONE
                Toast.makeText(context, R.string.partner_add_error, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun removePartner(oldPartner: Partner, currentPartner: Partner?) {
        user_edit_partner_progressBar?.visibility = View.VISIBLE
        val userId = user?.id ?: return
        get(context).entourageComponent.userRequest.removePartnerFromUser(userId, oldPartner.id)
                .enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                user_edit_partner_progressBar?.visibility = View.GONE
                if (response.isSuccessful) {
                    currentPartner?.let {
                        addPartner(currentPartner)
                    } ?: run {
                        get(context).entourageComponent.authenticationController.user?.let { user ->
                            user.partner = null
                            get(context).entourageComponent.authenticationController.saveUser(user)
                        }
                        Toast.makeText(activity, R.string.partner_remove_ok, Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                } else {
                    Toast.makeText(context, R.string.partner_remove_error, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                user_edit_partner_progressBar?.visibility = View.GONE
                Toast.makeText(context, R.string.partner_remove_error, Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "user_edit_association_fragment"
    }
}