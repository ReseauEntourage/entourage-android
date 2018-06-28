package social.entourage.android.invite.phonenumber;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
 * Use the {@link InviteByPhoneNumberFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InviteByPhoneNumberFragment extends InviteBaseFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.invite_phonenumber";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.title_action_button)
    TextView sendButton;

    @BindView(R.id.invite_phone_number)
    EditText phoneNumberEditText;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public InviteByPhoneNumberFragment() {
        // Required empty public constructor
    }

    public static InviteByPhoneNumberFragment newInstance(String feedItemUUID, int feedItemType) {
        InviteByPhoneNumberFragment fragment = new InviteByPhoneNumberFragment();
        fragment.setFeedData(feedItemUUID, feedItemType);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_invite_by_phone_number, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeEditText();
    }

    // ----------------------------------
    // ONCLICK CALLBACKS
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseClicked() {
        dismiss();
    }

    @OnClick(R.id.title_action_button)
    protected void onSendClicked() {

        // Check phone number
        String phoneNumber = Utils.checkPhoneNumberFormat(phoneNumberEditText.getText().toString());
        if (phoneNumber == null) {
            Toast.makeText(getActivity(), R.string.login_text_invalid_format, Toast.LENGTH_SHORT).show();
            return;
        }

        if (presenter != null) {
            // Disable the send button
            sendButton.setEnabled(false);
            // Send the request to server
            MultipleInvitations invitations = new MultipleInvitations(Invitation.INVITE_BY_SMS);
            invitations.addPhoneNumber(phoneNumber);
            presenter.inviteBySMS(feedItemUUID, feedItemType, invitations);
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeEditText() {
        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                sendButton.setEnabled( s.length() > 0 );
            }
        });
    }

    // ----------------------------------
    // PRESENTER CALLBACKS
    // ----------------------------------

    protected void onInviteSent(boolean success) {
        // Hide the progress dialog
        ((EntourageActivity)getActivity()).dismissProgressDialog();
        // Enable the send button
        sendButton.setEnabled(true);

        if (success) {
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
