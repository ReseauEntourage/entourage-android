package social.entourage.android.invite.contacts;


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.invite.InviteFriendsListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class InviteContactsFragment extends EntourageDialogFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.invite_contacts";

    private static final String KEY_FEEDITEM_ID = "social.entourage.android.KEY_FEEDITEM_ID";
    private static final String KEY_FEEDITEM_TYPE = "social.entourage.android.KEY_FEEDITEM_TYPE";

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

    @Inject
    InviteContactsPresenter presenter;

    long feedItemId;
    int feedItemType;

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
            R.id.contact_name
    };

    @Bind(R.id.invite_contacts_listView)
    ListView contactsList;

    // An adapter that binds the result Cursor to the ListView
    private SimpleCursorAdapter mCursorAdapter;

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

    @Bind(R.id.invite_contacts_search)
    EditText searchEditText;

    @Bind(R.id.invite_contacts_send_button)
    Button sendButton;

    private InviteFriendsListener mInviteFriendsListener;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public InviteContactsFragment() {
        // Required empty public constructor
    }

    public static InviteContactsFragment newInstance(long feedId, int feedItemType) {
        InviteContactsFragment fragment = new InviteContactsFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_FEEDITEM_ID, feedId);
        args.putInt(KEY_FEEDITEM_TYPE, feedItemType);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_invite_contacts, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());

        if (getArguments() != null) {
            feedItemId = getArguments().getLong(KEY_FEEDITEM_ID);
            feedItemType = getArguments().getInt(KEY_FEEDITEM_TYPE);
        }
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerInviteContactsComponent.builder()
                .entourageComponent(entourageComponent)
                .inviteContactsModule(new InviteContactsModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Gets a CursorAdapter
        mCursorAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.layout_invite_contacts_list_item,
                null,
                FROM_COLUMNS, TO_IDS,
                0);
        // Sets the adapter for the ListView
        contactsList.setAdapter(mCursorAdapter);
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
    }

    // ----------------------------------
    // LISTENER
    // ----------------------------------

    public void setInviteFriendsListener(final InviteFriendsListener inviteFriendsListener) {
        this.mInviteFriendsListener = inviteFriendsListener;
    }


    // ----------------------------------
    // ONCLICK CALLBACKS
    // ----------------------------------

    @OnClick(R.id.invite_contacts_close_button)
    protected void onCloseClicked() {
        dismiss();
    }

    @OnClick(R.id.invite_contacts_send_button)
    protected void onSendClicked() {
        // Disable the send button
        sendButton.setEnabled(false);
        // Show the progress dialog
        ((EntourageActivity)getActivity()).showProgressDialog(R.string.invite_contacts_retrieving_phone_numbers);
        // Get the Cursor
        Cursor cursor = ((SimpleCursorAdapter)contactsList.getAdapter()).getCursor();
        // Get the selected contacts
        int selectedContactsCount = 0;
        StringBuffer contactIds = new StringBuffer();
        SparseBooleanArray checkedItems = contactsList.getCheckedItemPositions();
        for (int i = 0; i < contactsList.getCount(); i++) {
            if (checkedItems.valueAt(i)) {
                selectedContactsCount++;
            }
        }
        if (selectedContactsCount == 0) {
            sendButton.setEnabled(true);
            return;
        }
        mContactIdsSelectionArgs = new String[selectedContactsCount];
        int index = 0;
        for (int i = 0; i < contactsList.getCount(); i++) {
            if (checkedItems.valueAt(i)) {
                int position = checkedItems.keyAt(i);
                // Move to the selected contact
                cursor.moveToPosition(position);
                // Get the _ID value
                String contactId = cursor.getString(CONTACT_ID_INDEX);
                mContactIdsSelectionArgs[index] = contactId;
                index++;
                if (contactIds.length() > 0) {
                    contactIds.append(",");
                }
                contactIds.append("?");
            }
        }
        mContactIds = contactIds.toString();

        getLoaderManager().destroyLoader(PHONE_LOADER_ID);
        getLoaderManager().initLoader(PHONE_LOADER_ID, null, this);
    }

    // ----------------------------------
    // AdapterView.OnItemClickListener
    // ----------------------------------

    @Override
    public void onItemClick(
            AdapterView<?> parent, View item, int position, long rowID) {
        // Toggle the checkbox
        CheckBox contactCheckbox = (CheckBox)item.findViewById(R.id.contact_checkBox);
        if (contactCheckbox == null) return;

        if (!contactCheckbox.isChecked()) {
            sendButton.setEnabled(true);
        }
        contactCheckbox.setChecked(!contactCheckbox.isChecked());

        contactsList.setItemChecked(position, contactCheckbox.isChecked());

        // Enable or disable the send button
        sendButton.setEnabled( contactsList.getCheckedItemCount() > 0 );
    }

    // ----------------------------------
    // LoaderManager.LoaderCallbacks<Cursor>
    // ----------------------------------

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

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case CONTACTS_LOADER_ID:
                // Put the result Cursor in the adapter for the ListView
                mCursorAdapter.swapCursor(cursor);

                break;

            case PHONE_LOADER_ID:
                // Update the progress dialog
                ((EntourageActivity)getActivity()).showProgressDialog(R.string.invite_contacts_inviting);
                // Get the phone numbers
                while (cursor.moveToNext()) {
                    String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    // Send the phone number to server
                    serverRequestsCount++;
                    presenter.inviteBySMS(feedItemId, feedItemType, phone);
                }
                if (serverRequestsCount == 0) {
                    onInviteSent(false);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case CONTACTS_LOADER_ID:
                // Delete the reference to the existing Cursor
                mCursorAdapter.swapCursor(null);

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
                if (mInviteFriendsListener != null) {
                    mInviteFriendsListener.onInviteSent();
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
