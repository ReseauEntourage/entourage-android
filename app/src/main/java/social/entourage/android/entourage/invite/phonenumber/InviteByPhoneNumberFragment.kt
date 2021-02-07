package social.entourage.android.entourage.invite.phonenumber

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_invite_by_phone_number.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.base.BaseActivity
import social.entourage.android.R
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.MultipleInvitations
import social.entourage.android.entourage.invite.InviteBaseFragment
import social.entourage.android.tools.Utils.checkPhoneNumberFormat

/**
 * A simple [InviteBaseFragment] subclass.
 * Use the [InviteByPhoneNumberFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InviteByPhoneNumberFragment  : InviteBaseFragment() {
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_invite_by_phone_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title_action_button.setOnClickListener { onSendClicked() }
        title_close_button.setOnClickListener { onCloseClicked() }
        initializeEditText()
    }

    // ----------------------------------
    // ONCLICK CALLBACKS
    // ----------------------------------
    fun onCloseClicked() {
        dismiss()
    }

    fun onSendClicked() {
        // Check phone number
        invite_phone_number?.text?.toString()?.let {
            checkPhoneNumberFormat(it)?.let { phoneNumber ->
                // Disable the send button
                title_action_button?.isEnabled = false
                // Send the request to server
                val invitations = MultipleInvitations(Invitation.INVITE_BY_SMS)
                invitations.addPhoneNumber(phoneNumber)
                feedItemUUID?.let { uuid -> presenter?.inviteBySMS(uuid, feedItemType, invitations) }
            } ?: run {
                Toast.makeText(activity, R.string.login_text_invalid_format, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun initializeEditText() {
        invite_phone_number?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                title_action_button?.isEnabled = s.isNotEmpty()
            }
        })
    }

    // ----------------------------------
    // PRESENTER CALLBACKS
    // ----------------------------------
    override fun onInviteSent(success: Boolean) {
        // Hide the progress dialog
        (activity as? BaseActivity)?.dismissProgressDialog() ?: return
        // Enable the send button
        title_action_button?.isEnabled = true
        if (success) {
            // Notify the listener
            inviteFriendsListener?.onInviteSent()
            // Close the fragment
            dismiss()
        } else {
            // Show error
            Toast.makeText(activity, R.string.invite_contacts_send_error, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.invite_phonenumber"
        fun newInstance(feedItemUUID: String?, feedItemType: Int): InviteByPhoneNumberFragment {
            val fragment = InviteByPhoneNumberFragment()
            fragment.setFeedData(feedItemUUID, feedItemType)
            return fragment
        }
    }
}