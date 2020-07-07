package social.entourage.android.user.discussion

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_user_discussion.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.R
import social.entourage.android.api.model.ChatMessage
import social.entourage.android.api.model.ChatMessage.ChatMessageWrapper
import social.entourage.android.api.model.ChatMessage.ChatMessagesWrapper
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.User
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.entourage.information.discussion.DiscussionAdapter
import social.entourage.android.tools.CropCircleTransformation
import social.entourage.android.user.UserFragment
import java.util.*

/**
 * A [EntourageDialogFragment] subclass that shows the discussion history between the current user and another user
 * Use the [UserDiscussionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserDiscussionFragment  : EntourageDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var otherUser: User? = null
    private var showInfoButton = false
    var discussionAdapter: DiscussionAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            otherUser = it.getSerializable(User.KEY_USER) as User?
            showInfoButton = it.getBoolean(KEY_SHOW_INFO_BUTTON, true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_discussion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseView()
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun initialiseView() {
        activity?.let { if(it.isFinishing) return } ?: return

        otherUser?.let {
            //name
            user_discussion_title?.text = it.displayName

            //photo
            user_discussion_icon?.let { iconView ->
                it.avatarURL?.let { avatarURL->
                    Picasso.get().load(Uri.parse(avatarURL))
                            .placeholder(R.drawable.ic_user_photo)
                            .transform(CropCircleTransformation())
                            .into(iconView)
                } ?: run  {
                    Picasso.get().load(R.drawable.ic_user_photo)
                            .transform(CropCircleTransformation())
                            .into(iconView)
                }
            }
        } ?: return

        //discussion list
        if (discussionAdapter == null) {
            user_discussion_list?.layoutManager = LinearLayoutManager(context)
            discussionAdapter = DiscussionAdapter()
            user_discussion_list?.adapter = discussionAdapter
        }
        //get the current discussion
        getDiscussion()

        //info button
        user_discussion_info?.visibility = if (showInfoButton) View.VISIBLE else View.GONE
        user_discussion_close?.setOnClickListener { onCloseButtonClicked()}
        user_discussion_info?.setOnClickListener {onInfoButtonClicked()}
        user_discussion_icon?.setOnClickListener {onInfoButtonClicked()}
        user_discussion_title?.setOnClickListener {onInfoButtonClicked()}
        user_discussion_comment_send_button?.setOnClickListener {onSendChatClicked()}
        user_discussion_comment_photo_button?.setOnClickListener {onPhotoClicked()}
    }

    private fun scrollToLastCard() {
        discussionAdapter?.let {user_discussion_list?.scrollToPosition(it.itemCount - 1)}
    }

    // ----------------------------------
    // BUTTONS HANDLING
    // ----------------------------------
    fun onCloseButtonClicked() {
        dismiss()
    }

    fun onInfoButtonClicked() {
        if (showInfoButton) {
            otherUser?.id?.let {UserFragment.newInstance(it).show(parentFragmentManager, UserFragment.TAG)}
        }
    }

    fun onSendChatClicked() {
        user_discussion_comment?.text.toString().let { chatMessage->
            sendChatMessage(chatMessage)
        }
    }

    fun onPhotoClicked() {
        Toast.makeText(context, R.string.error_not_yet_implemented, Toast.LENGTH_SHORT).show()
    }

    // ----------------------------------
    // API CALLS
    // ----------------------------------
    private fun getDiscussion() {
        val userConversationUUID = otherUser?.conversation?.uuid ?: return
        val entourageRequest = get().entourageComponent.entourageRequest ?: return
        entourageRequest.retrieveEntourageMessages(userConversationUUID).enqueue(object : Callback<ChatMessagesWrapper> {
            override fun onResponse(call: Call<ChatMessagesWrapper>, response: Response<ChatMessagesWrapper>) {
                if (response.isSuccessful) {
                    response.body()?.chatMessages?.let { chatMessageList ->
                        //check who sent the message
                        get(activity).entourageComponent.authenticationController.me?.id?.let { me ->
                            for (chatMessage in chatMessageList) {
                                chatMessage.setIsMe(chatMessage.userId == me)
                            }
                        }
                        //add the messages to the adapter
                        discussionAdapter?.addItems(ArrayList<TimestampedObject>(chatMessageList))
                    }
                } else {
                    if (context != null) {
                        Toast.makeText(context, R.string.server_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ChatMessagesWrapper>, t: Throwable) {
                if (context != null) {
                    Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun sendChatMessage(message: String?) {
        val userConversationUUID = otherUser?.conversation?.uuid ?: return
        if (message.isNullOrBlank()) {
            onChatMessageSent(null)
            return
        }
        val entourageRequest = get().entourageComponent.entourageRequest ?: return
        val chatMessage = ChatMessage(message)
        chatMessage.creationDate = Date()
        val chatMessageWrapper = ChatMessageWrapper()
        chatMessageWrapper.chatMessage = chatMessage
        val call = entourageRequest.chatMessage(userConversationUUID, chatMessageWrapper)
        call.enqueue(object : Callback<ChatMessageWrapper> {
            override fun onResponse(call: Call<ChatMessageWrapper>, response: Response<ChatMessageWrapper>) {
                if (response.isSuccessful) {
                    response.body()?.chatMessage?.let { onChatMessageSent(it)}
                } else {
                    onChatMessageSent(null)
                }
            }

            override fun onFailure(call: Call<ChatMessageWrapper>, t: Throwable) {
                onChatMessageSent(null)
            }
        })
    }

    private fun onChatMessageSent(chatMessage: ChatMessage?) {
        if (chatMessage == null) {
            if (context != null) {
                Toast.makeText(context, R.string.tour_info_error_chat_message, Toast.LENGTH_SHORT).show()
            }
            return
        }
        user_discussion_comment?.setText("")
        //add the chat to the discussion list
        //hide the keyboard
        user_discussion_comment?.let {
            if (it.hasFocus()) {
                (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(it.windowToken, 0)
            }
        }
        //add the message to the list
        chatMessage.setIsMe(true)
        discussionAdapter?.addCardInfoAfterTimestamp(chatMessage)
        scrollToLastCard()
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        val TAG = UserDiscussionFragment::class.java.simpleName
        private const val KEY_SHOW_INFO_BUTTON = "UserDiscussionFragment.KEY_SHOW_INFO_BUTTON"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param otherUser The other user of this discussion.
         * @return A new instance of fragment UserDiscussionFragment.
         */
        fun newInstance(otherUser: User?, showInfoButton: Boolean): UserDiscussionFragment {
            val fragment = UserDiscussionFragment()
            val args = Bundle()
            args.putSerializable(User.KEY_USER, otherUser)
            args.putBoolean(KEY_SHOW_INFO_BUTTON, showInfoButton)
            fragment.arguments = args
            return fragment
        }
    }
}