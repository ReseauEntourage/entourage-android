package social.entourage.android.entourage.invite.contacts

import android.annotation.SuppressLint
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import kotlinx.android.synthetic.main.fragment_invite_contacts.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.base.BaseActivity
import social.entourage.android.R
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.MultipleInvitations
import social.entourage.android.entourage.invite.InviteBaseFragment
import social.entourage.android.tools.Utils.checkPhoneNumberFormat
import java.util.*

/**
 * A simple [InviteBaseFragment] subclass.
 */
class InviteContactsFragment  : InviteBaseFragment(), LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    // An adapter that binds the result Cursor to the ListView
    private var mContactsAdapter: InviteContactsAdapter? = null

    // Quick-jump list-view
    private var quickJumpAdapter: ArrayAdapter<String>? = null

    // Defines a variable for the search string
    private var mSearchString = ""

    // Defines the array to hold values that replace the ?
    private val mSelectionArgs = arrayOf(mSearchString)

    // Defines a variable for the list of contact ids
    private val mContactIds = ""

    // Defines the array to hold values that replace the ?
    private val mContactIdsSelectionArgs = arrayOf(mContactIds)

    // Number of server requests made
    private var serverRequestsCount = 0

    // Number of successfully server requests
    private var successfulyServerRequestCount = 0

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_invite_contacts, container, false)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mContactsAdapter = InviteContactsAdapter(
                requireContext(),
                FROM_COLUMNS[0]
        )
        // Sets the adapter for the ListView
        invite_contacts_listView?.adapter = mContactsAdapter
        // Set the item click listener to be the current fragment.
        invite_contacts_listView?.onItemClickListener = this

        // Initializes the loader
        val loaderInstance = LoaderManager.getInstance(this)
        loaderInstance.initLoader(CONTACTS_LOADER_ID, null, this)

        // Initialize the search field
        invite_contacts_search?.setOnEditorActionListener { v, actionId, event ->
            if (event == null) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mSearchString = v.text.toString().trim { it <= ' ' }
                    loaderInstance.destroyLoader(CONTACTS_LOADER_ID)
                    loaderInstance.initLoader(CONTACTS_LOADER_ID, null, this@InviteContactsFragment)
                }
            } else if (event.keyCode == KeyEvent.ACTION_DOWN) {
                mSearchString = v.text.toString().trim { it <= ' ' }
                //LoaderManager.getInstance(this).restartLoader(CONTACTS_LOADER_ID, null, InviteContactsFragment.this);
                loaderInstance.destroyLoader(CONTACTS_LOADER_ID)
                loaderInstance.initLoader(CONTACTS_LOADER_ID, null, this@InviteContactsFragment)
            }
            false
        }

        // Initialize the quick-jump list
        val quickJumpArray = ArrayList<String>()
        var c = 'A'
        while (c <= 'Z') {
            quickJumpArray.add(String.format("%c", c))
            c++
        }
        quickJumpArray.add("#")
        quickJumpAdapter = ArrayAdapter(
                requireContext(),
                R.layout.layout_invite_contacts_quick_jump_item,
                R.id.invite_contacts_quick_jump_textview,
                quickJumpArray)
        invite_contacts_quick_jump_listview?.adapter = quickJumpAdapter
        invite_contacts_quick_jump_listview?.onItemClickListener = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title_close_button?.setOnClickListener { onCloseClicked() }
        title_action_button?.setOnClickListener { onSendClicked() }
    }

    // ----------------------------------
    // ONCLICK CALLBACKS
    // ----------------------------------
    fun onCloseClicked() {
        if (!isStateSaved) dismiss()
    }

    private fun onSendClicked() {
        // Disable the send button
        title_action_button?.isEnabled = false
        // Get the selected contacts
        val invitations = MultipleInvitations(Invitation.INVITE_BY_SMS)
        invite_contacts_listView?.let { contactList ->
            for (i in 0 until contactList.count) {
                if (contactList.isItemChecked(i)) {
                    mContactsAdapter?.getPhoneAt(i)?.let { phone ->
                        checkPhoneNumberFormat(PhoneNumberUtils.stripSeparators(phone))?.let {
                            invitations.addPhoneNumber(it)
                        }
                    }
                }
            }
        }
        // Update the progress dialog
        (activity as? BaseActivity)?.showProgressDialog(R.string.invite_contacts_inviting)
        // Send the phone number to server
        serverRequestsCount++
        feedItemUUID?.let { presenter.inviteBySMS(it, feedItemType, invitations) }
    }

    // ----------------------------------
    // AdapterView.OnItemClickListener
    // ----------------------------------
    override fun onItemClick(
            parent: AdapterView<*>, item: View, position: Int, rowID: Long) {
        invite_contacts_listView?.let { contactsList ->
            if (parent === contactsList) {
                // Toggle the checkbox
                val contactCheckbox = item.findViewById<CheckBox>(R.id.contact_checkBox) ?: return
                if (!contactCheckbox.isChecked) {
                    title_action_button?.isEnabled = true
                }
                contactCheckbox.isChecked = !contactCheckbox.isChecked
                contactsList.setItemChecked(position, contactCheckbox.isChecked)
                mContactsAdapter?.setItemSelected(position, contactCheckbox.isChecked)

                // Enable or disable the send button
                title_action_button?.isEnabled = contactsList.checkedItemCount > 0
            } else if (parent === invite_contacts_quick_jump_listview) {
                // Jump to the selected section
                quickJumpAdapter?.getItem(position)?.let { jumpToString ->
                    mContactsAdapter?.getPositionForSection(jumpToString)?.let { sectionPosition ->
                        if (sectionPosition != -1) {
                            contactsList.smoothScrollToPositionFromTop(sectionPosition, 0)
                        }
                    }
                }
            }
        }
    }

    // ----------------------------------
    // LoaderManager.LoaderCallbacks<Cursor>
    // ----------------------------------
    override fun onCreateLoader(loaderId: Int, args: Bundle?): Loader<Cursor> {
        when (loaderId) {
            CONTACTS_LOADER_ID -> {
                // Reset the checked items
                invite_contacts_listView?.clearChoices()
                title_action_button?.isEnabled = false
                /*
                 * Makes search string into pattern and
                 * stores it in the selection array
                 */mSelectionArgs[0] = "%$mSearchString%"
                // Starts the query
                return CursorLoader(
                        requireActivity(),
                        ContactsContract.Contacts.CONTENT_URI,
                        PROJECTION,
                        SELECTION,
                        mSelectionArgs,
                        SORT_ORDER
                )
            }
            else /*PHONE_LOADER_ID*/ ->                 // Starts the query
                return CursorLoader(
                        requireActivity(),
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        PHONE_PROJECTION,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " IN (" + mContactIds + ")",
                        mContactIdsSelectionArgs,
                        null
                )
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        when (loader.id) {
            CONTACTS_LOADER_ID ->                 // Put the result Cursor in the adapter for the ListView
                mContactsAdapter?.swapCursor(cursor)
            PHONE_LOADER_ID -> {
                // Update the progress dialog
                (activity as? BaseActivity)?.showProgressDialog(R.string.invite_contacts_inviting)
                // Get the phone numbers
                val invitations = MultipleInvitations(Invitation.INVITE_BY_SMS)
                while (cursor.moveToNext()) {
                    var phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    phone = PhoneNumberUtils.stripSeparators(phone)
                    invitations.addPhoneNumber(phone)
                }
                // Send the phone number to server
                serverRequestsCount++
                feedItemUUID?.let { presenter.inviteBySMS(it, feedItemType, invitations) }
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        when (loader.id) {
            CONTACTS_LOADER_ID -> mContactsAdapter?.resetCursor()
            PHONE_LOADER_ID -> { }
        }
    }

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------
    override fun onInviteSent(success: Boolean) {
        serverRequestsCount--
        successfulyServerRequestCount += if (success) 1 else -1
        if (serverRequestsCount <= 0) {
            serverRequestsCount = 0
            // Hide the progress dialog
            (activity as? BaseActivity)?.dismissProgressDialog()
            // Re-enable the send button
            title_action_button?.isEnabled = true
            // If success, close the fragment
            if (successfulyServerRequestCount >= 0) {
                // Notify the listener
                inviteFriendsListener?.onInviteSent()
                // Close the fragment
                dismiss()
            } else {
                // Show error
                Toast.makeText(activity, R.string.invite_contacts_send_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.invite_contacts"
        private val PROJECTION = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        )

        // The column index for the _ID column
        private const val CONTACT_ID_INDEX = 0

        // The column index for the LOOKUP_KEY column
        private const val LOOKUP_KEY_INDEX = 1

        // Defines the text expression
        @SuppressLint("InlinedApi")
        private val SELECTION = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ? " + "AND " +
                ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '1'"

        // Sort order
        private const val SORT_ORDER = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC "

        // Contacts Loader ID
        private const val CONTACTS_LOADER_ID = 0

        // Contact's phone number
        private val PHONE_PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        //ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'";
        // Phone Loader ID
        private const val PHONE_LOADER_ID = 1

        // ----------------------------------
        // ATTRIBUTES
        // ----------------------------------
        /*
        * Defines an array that contains column names to move from
        * the Cursor to the ListView.
        */
        @SuppressLint("InlinedApi")
        private val FROM_COLUMNS = arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        )

        fun newInstance(feedItemUUID: String?, feedItemType: Int): InviteContactsFragment {
            val fragment = InviteContactsFragment()
            fragment.setFeedData(feedItemUUID, feedItemType)
            return fragment
        }
    }
}