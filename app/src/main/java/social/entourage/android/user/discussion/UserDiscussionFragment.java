package social.entourage.android.user.discussion;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.User;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.map.tour.information.discussion.DiscussionAdapter;
import social.entourage.android.tools.CropCircleTransformation;
import social.entourage.android.user.UserFragment;

/**
 * A {@link EntourageDialogFragment} subclass that shows the discussion history between the current user and another user
 * Use the {@link UserDiscussionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserDiscussionFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = UserDiscussionFragment.class.getSimpleName();

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private User otherUser;

    @BindView(R.id.user_discussion_icon)
    ImageView userPhoto;

    @BindView(R.id.user_discussion_title)
    TextView userTitle;

    @BindView(R.id.user_discussion_comment)
    EditText commentEditText;

    @BindView(R.id.user_discussion_list)
    RecyclerView discussionView;

    DiscussionAdapter discussionAdapter;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public UserDiscussionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param otherUser The other user of this discussion.
     * @return A new instance of fragment UserDiscussionFragment.
     */
    public static UserDiscussionFragment newInstance(User otherUser) {
        UserDiscussionFragment fragment = new UserDiscussionFragment();
        Bundle args = new Bundle();
        args.putSerializable(User.KEY_USER, otherUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            otherUser = (User)getArguments().getSerializable(User.KEY_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_discussion, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialiseView();
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initialiseView() {
        if (otherUser == null) return;
        if (getActivity() == null || getActivity().isFinishing()) return;

        //photo
        if (otherUser.getAvatarURL() != null) {
            Picasso.with(getActivity()).load(Uri.parse(otherUser.getAvatarURL()))
                    .placeholder(R.drawable.ic_user_photo)
                    .transform(new CropCircleTransformation())
                    .into(userPhoto);
        }
        else {
            Picasso.with(getActivity()).load(R.drawable.ic_user_photo)
                    .transform(new CropCircleTransformation())
                    .into(userPhoto);
        }

        //name
        userTitle.setText(otherUser.getDisplayName());

        //discussion list
        if (discussionAdapter == null) {
            discussionView.setLayoutManager(new LinearLayoutManager(getContext()));
            discussionAdapter = new DiscussionAdapter();
            discussionView.setAdapter(discussionAdapter);
        }
        //get the current discussion
        getDiscussion();
    }

    private void scrollToLastCard() {
        discussionView.scrollToPosition(discussionAdapter.getItemCount()-1);
    }

    // ----------------------------------
    // BUTTONS HANDLING
    // ----------------------------------

    @OnClick(R.id.user_discussion_close)
    protected void onCloseButtonClicked() {
        dismiss();
    }

    @OnClick(R.id.user_discussion_info)
    protected void onInfoButtonClicked() {
        UserFragment userFragment = UserFragment.newInstance(otherUser.getId());
        userFragment.show(getFragmentManager(), UserFragment.TAG);
    }

    @OnClick(R.id.user_discussion_comment_send_button)
    protected void onSendChatClicked() {
        String chatMessage = commentEditText.getText().toString();
        sendChatMessage(chatMessage);
    }

    @OnClick(R.id.user_discussion_comment_photo_button)
    protected void onPhotoClicked() {
        Toast.makeText(getContext(), R.string.error_not_yet_implemented, Toast.LENGTH_SHORT).show();
    }

    // ----------------------------------
    // API CALLS
    // ----------------------------------

    private void getDiscussion() {

    }

    private void sendChatMessage(String message) {
        if (message == null || message.trim().length() == 0) {
            onChatMessageSent(null);
            return;
        }
        //For now simulate sending a message
        ChatMessage chatMessage = new ChatMessage(message);
        chatMessage.setCreationDate(new Date());
        onChatMessageSent(chatMessage);
    }

    private void onChatMessageSent(ChatMessage chatMessage) {
        if (chatMessage == null) {
            if(getContext()!=null) {
                Toast.makeText(getContext(), R.string.tour_info_error_chat_message, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        commentEditText.setText("");
        //add the chat to the discussion list
        //hide the keyboard
        if (commentEditText.hasFocus() && getActivity()!=null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
        }

        //add the message to the list
        chatMessage.setIsMe(true);
        discussionAdapter.addCardInfoAfterTimestamp(chatMessage);

        scrollToLastCard();
    }

}
