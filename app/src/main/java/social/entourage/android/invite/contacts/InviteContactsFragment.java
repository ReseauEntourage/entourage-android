package social.entourage.android.invite.contacts;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.telephony.PhoneNumberUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.MultipleInvitations;
import social.entourage.android.invite.InviteBaseFragment;
import social.entourage.android.tools.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class InviteContactsFragment extends InviteBaseFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.invite_contacts";

    private static final String[] PROJECTION =
            {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
            };
    // The column index for the _ID column
    private static final int CONTACT_ID_INDEX = 0;
    // The column index for the LOOKUP_KEY column
    private static final int LOOKUP_KEY_INDEX = 1;
    // Defines the text expression
    @SuppressLint("InlinedApi")
    private static final String SELECTION =
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ? " + "AND " +
                    ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '1'";
    // Sort order
    private static final String SORT_ORDER =
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC ";
    // Contacts Loader ID
    private static final int CONTACTS_LOADER_ID = 0;

    // Contact's phone number
    private static final String[] PHONE_PROJECTION =
            {
                    ContactsContract.CommonDataKinds.Phone._ID,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };
    @SuppressLint("InlinedApi")
    private static final String PHONE_SELECTION =
            ContactsContract.Data.CONTACT_ID + " IN (?) ";// + " AND " +
                    //ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'";
    // Phone Loader ID
    private static final int PHONE_LOADER_ID = 1;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    /*
     * Defines an array that contains column names to move from
     * the Cursor to the ListView.
     */
    @SuppressLint("InlinedApi")
    private final static String[] FROM_COLUMNS = {
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    };
    /*
     * Defines an array that contains resource ids for the layout views
     * that get the Cursor column contents. The id is pre-defined in
     * the Android framework, so it is prefaced with "android.R.id"
     */
    private final static int[] TO_IDS = {
            R.id.contact_phone
    };

    @BindView(R.id.invite_contacts_listView)
    ListView contactsList;

    // An adapter that binds the result Cursor to the ListView
    private InviteContactsAdapter mContactsAdapter;

    // Quick-jump list-view
    @BindView(R.id.invite_contacts_quick_jump_listview)
    ListView quickJumpList;

    private ArrayAdapter<String> quickJumpAdapter;

    // Defines a variable for the search string
    private String mSearchString = "";
    // Defines the array to hold values that replace the ?
    private String[] mSelectionArgs = { mSearchString };

    // Defines a variable for the list of contact ids
    private String mContactIds = "";
    // Defines the array to hold values that replace the ?
    private String[] mContactIdsSelectionArgs = { mContactIds };

    // Number of server requests made
    private int serverRequestsCount;

    // Number of successfully server requests
    private int successfulyServerRequestCount = 0;

    @BindView(R.id.invite_contacts_search)
    EditText searchEditText;

    @BindView(R.id.title_action_button)
    TextView sendButton;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public InviteContactsFragment() {
        // Required empty public constructor
    }

    public static InviteContactsFragment newInstance(String feedItemUUID, int feedItemType) {
        InviteContactsFragment fragment = new InviteContactsFragment();
        fragment.setFeedData(feedItemUUID, feedItemType);

        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_invite_contacts, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContactsAdapter = new InviteContactsAdapter(
                getActivity(),
                FROM_COLUMNS[0]
        );
        // Sets the adapter for the ListView
        contactsList.setAdapter(mContactsAdapter);
        // Set the item click listener to be the current fragment.
        contactsList.setOnItemClickListener(this);

        // Initializes the loader
        getLoaderManager().initLoader(CONTACTS_LOADER_ID, null, this);

        // Initialize the search field
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                if (event == null) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        mSearchString = v.getText().toString().trim();
                        getLoaderManager().destroyLoader(CONTACTS_LOADER_ID);
                        getLoaderManager().initLoader(CONTACTS_LOADER_ID, null, InviteContactsFragment.this);
                    }
                }
                else if (event.getKeyCode() == KeyEvent.ACTION_DOWN) {
                    mSearchString = v.getText().toString().trim();
                    //getLoaderManager().restartLoader(CONTACTS_LOADER_ID, null, InviteContactsFragment.this);
                    getLoaderManager().destroyLoader(CONTACTS_LOADER_ID);
                    getLoaderManager().initLoader(CONTACTS_LOADER_ID, null, InviteContactsFragment.this);
                }
                return false;
            }
        });

        // Initialize the quick-jump list
        ArrayList<String> quickJumpArray = new ArrayList<>();
        for(char c = 'A'; c <= 'Z'; c++) {
            quickJumpArray.add(String.format("%c", c));
        }
        quickJumpArray.add("#");
        quickJumpAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.layout_invite_contacts_quick_jump_item,
                R.id.invite_contacts_quick_jump_textview,
                quickJumpArray);
        quickJumpList.setAdapter(quickJumpAdapter);
        quickJumpList.setOnItemClickListener(this);
    }

    // ----------------------------------
    // ONCLICK CALLBACKS
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseClicked() {
        if (!isStateSaved()) dismiss();
    }

    @OnClick(R.id.title_action_button)
    protected void onSendClicked() {
        // Disable the send button
        sendButton.setEnabled(false);
        // Get the selected contacts
        MultipleInvitations invitations = new MultipleInvitations(Invitation.INVITE_BY_SMS);
        for (int i = 0; i < contactsList.getCount(); i++) {
            if (contactsList.isItemChecked(i)) {
                String phone = mContactsAdapter.getPhoneAt(i);
                if (phone != null) {
                    phone = Utils.checkPhoneNumberFormat(PhoneNumberUtils.stripSeparators(phone));
                    if (phone != null) {
                        invitations.addPhoneNumber(phone);
                    }
                }
            }
        }
        // Update the progress dialog
        ((EntourageActivity)getActivity()).showProgressDialog(R.string.invite_contacts_inviting);
        // Send the phone number to server
        serverRequestsCount++;
        presenter.inviteBySMS(feedItemUUID, feedItemType, invitations);
    }

    // ----------------------------------
    // AdapterView.OnItemClickListener
    // ----------------------------------

    @Override
    public void onItemClick(
            AdapterView<?> parent, View item, int position, long rowID) {
        if (parent == contactsList) {
            // Toggle the checkbox
            CheckBox contactCheckbox = (CheckBox) item.findViewById(R.id.contact_checkBox);
            if (contactCheckbox == null) return;

            if (!contactCheckbox.isChecked()) {
                sendButton.setEnabled(true);
            }
            contactCheckbox.setChecked(!contactCheckbox.isChecked());

            contactsList.setItemChecked(position, contactCheckbox.isChecked());
            mContactsAdapter.setItemSelected(position, contactCheckbox.isChecked());

            // Enable or disable the send button
            sendButton.setEnabled(contactsList.getCheckedItemCount() > 0);
        }
        else if (parent == quickJumpList) {
            // Jump to the selected section
            String jumpToString = quickJumpAdapter.getItem(position);
            int sectionPosition = mContactsAdapter.getPositionForSection(jumpToString);
            if (sectionPosition != -1) {
                contactsList.smoothScrollToPositionFromTop(sectionPosition, 0);
            }
        }
    }

    // ----------------------------------
    // LoaderManager.LoaderCallbacks<Cursor>
    // ----------------------------------

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case CONTACTS_LOADER_ID:
                // Reset the checked items
                contactsList.clearChoices();
                sendButton.setEnabled(false);
                /*
                 * Makes search string into pattern and
                 * stores it in the selection array
                 */
                mSelectionArgs[0] = "%" + mSearchString + "%";
                // Starts the query
                return new CursorLoader(
                        getActivity(),
                        ContactsContract.Contacts.CONTENT_URI,
                        PROJECTION,
                        SELECTION,
                        mSelectionArgs,
                        SORT_ORDER
                );
            case PHONE_LOADER_ID:
                // Starts the query
                return new CursorLoader(
                        getActivity(),
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        PHONE_PROJECTION,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " IN (" + mContactIds + ")",
                        mContactIdsSelectionArgs,
                        null
                );
        }
        return null;
    }

    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case CONTACTS_LOADER_ID:
                // Put the result Cursor in the adapter for the ListView
                mContactsAdapter.swapCursor(cursor);

                break;

            case PHONE_LOADER_ID:
                // Update the progress dialog
                ((EntourageActivity)getActivity()).showProgressDialog(R.string.invite_contacts_inviting);
                // Get the phone numbers
                MultipleInvitations invitations = new MultipleInvitations(Invitation.INVITE_BY_SMS);

                while (cursor.moveToNext()) {
                    String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phone = PhoneNumberUtils.stripSeparators(phone);
                    invitations.addPhoneNumber(phone);
                }
                // Send the phone number to server
                serverRequestsCount++;
                presenter.inviteBySMS(feedItemUUID, feedItemType, invitations);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case CONTACTS_LOADER_ID:
                // Delete the reference to the existing Cursor
                mContactsAdapter.swapCursor(null);

                break;

            case PHONE_LOADER_ID:
                break;
        }
    }

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------

    protected void onInviteSent(boolean success) {
        serverRequestsCount--;
        successfulyServerRequestCount += success ? 1 : -1;
        if (serverRequestsCount <= 0) {
            serverRequestsCount = 0;
            // Hide the progress dialog
            ((EntourageActivity)getActivity()).dismissProgressDialog();
            // Re-enable the send button
            sendButton.setEnabled(true);
            // If success, close the fragment
            if (successfulyServerRequestCount >= 0) {
                // Notify the listener
                if (inviteFriendsListener != null) {
                    inviteFriendsListener.onInviteSent();
                }
                // Close the fragment
                dismiss();
            } else {
                // Show error
                Toast.makeText(getActivity(), R.string.invite_contacts_send_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
