package social.entourage.android.user.discussion;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.AuthenticationController;
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

    private static final String KEY_SHOW_INFO_BUTTON = "UserDiscussionFragment.KEY_SHOW_INFO_BUTTON";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private User otherUser;
    private boolean showInfoButton;

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
    public static UserDiscussionFragment newInstance(User otherUser, boolean showInfoButton) {
        UserDiscussionFragment fragment = new UserDiscussionFragment();
        Bundle args = new Bundle();
        args.putSerializable(User.KEY_USER, otherUser);
        args.putBoolean(KEY_SHOW_INFO_BUTTON, showInfoButton);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            otherUser = (User)getArguments().getSerializable(User.KEY_USER);
            showInfoButton = getArguments().getBoolean(KEY_SHOW_INFO_BUTTON, true);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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

        //info button
        View infoButton = this.getView().findViewById(R.id.user_discussion_info);
        if (infoButton != null) {
            infoButton.setVisibility(showInfoButton ? View.VISIBLE : View.GONE);
        }
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

    @OnClick({R.id.user_discussion_info, R.id.user_discussion_icon, R.id.user_discussion_title})
    protected void onInfoButtonClicked() {
        if (showInfoButton) {
            UserFragment userFragment = UserFragment.newInstance(otherUser.getId());
            userFragment.show(getFragmentManager(), UserFragment.TAG);
        }
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
        User.UserConversation userConversation = otherUser.getConversation();
        if (userConversation == null ||  userConversation.getUUID() == null) return;

        EntourageRequest entourageRequest = EntourageApplication.get().getEntourageComponent().getEntourageRequest();
        if (entourageRequest == null) return;
        Call<ChatMessage.ChatMessagesWrapper> call = entourageRequest.retrieveEntourageMessages(userConversation.getUUID());
        call.enqueue(new Callback<ChatMessage.ChatMessagesWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<ChatMessage.ChatMessagesWrapper> call, @NonNull final Response<ChatMessage.ChatMessagesWrapper> response) {
                if (response.isSuccessful()) {
                    List<ChatMessage> chatMessageList = response.body().getChatMessages();
                    //check who sent the message
                    AuthenticationController authenticationController = EntourageApplication.get(getActivity()).getEntourageComponent().getAuthenticationController();
                    if (authenticationController.isAuthenticated()) {
                        int me = authenticationController.getUser().getId();
                        for (final ChatMessage chatMessage : chatMessageList) {
                            chatMessage.setIsMe(chatMessage.getUserId() == me);
                        }
                    }
                    //add the messages to the adapter
                    List<TimestampedObject> timestampedObjectList = new ArrayList<TimestampedObject>(chatMessageList);
                    discussionAdapter.addItems(timestampedObjectList);
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), R.string.server_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull final Call<ChatMessage.ChatMessagesWrapper> call, @NonNull final Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendChatMessage(String message) {
        User.UserConversation userConversation = otherUser.getConversation();
        if (userConversation == null ||  userConversation.getUUID() == null) return;

        if (message == null || message.trim().length() == 0) {
            onChatMessageSent(null);
            return;
        }
        EntourageRequest entourageRequest = EntourageApplication.get().getEntourageComponent().getEntourageRequest();
        if (entourageRequest == null) return;

        ChatMessage chatMessage = new ChatMessage(message);
        chatMessage.setCreationDate(new Date());
        ChatMessage.ChatMessageWrapper chatMessageWrapper = new ChatMessage.ChatMessageWrapper();
        chatMessageWrapper.setChatMessage(chatMessage);

        Call<ChatMessage.ChatMessageWrapper> call = entourageRequest.chatMessage(userConversation.getUUID(), chatMessageWrapper);
        call.enqueue(new Callback<ChatMessage.ChatMessageWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<ChatMessage.ChatMessageWrapper> call, @NonNull final Response<ChatMessage.ChatMessageWrapper> response) {
                if (response.isSuccessful()) {
                    onChatMessageSent(response.body().getChatMessage());
                } else {
                    onChatMessageSent(null);
                }
            }

            @Override
            public void onFailure(@NonNull final Call<ChatMessage.ChatMessageWrapper> call, @NonNull final Throwable t) {
                onChatMessageSent(null);
            }
        });
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
