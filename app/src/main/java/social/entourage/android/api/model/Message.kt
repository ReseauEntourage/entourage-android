package social.entourage.android.api.model

import android.content.Context
import com.google.gson.Gson
import social.entourage.android.R
import java.io.Serializable

class Message(var author: String, var msgObject: String?, content: String, pushNotificationId: Int, pushNotificationTag: String?) : Serializable {
    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    val content: PushNotificationContent?
    var pushNotificationId: Int
    var pushNotificationTag: String?
    var isVisible: Boolean

    val message: String
        get() = content?.message ?: ""

    val hash: String
        get() {
            if (pushNotificationTag == null) {
                pushNotificationTag = content?.notificationTag
            }
            return if (pushNotificationTag.isNullOrEmpty()) pushNotificationId.toString() else (pushNotificationTag + HASH_SEPARATOR + getContentType())
        }

    private fun getContentType():Int {
        when(content?.type) {
            PushNotificationContent.TYPE_NEW_CHAT_MESSAGE -> return CHAT_MESSAGE_NOTIFICATION_ID
            PushNotificationContent.TYPE_NEW_JOIN_REQUEST -> return JOIN_REQUEST_NOTIFICATION_ID
        }
        return 1
    }

    fun getContentTitleForCount(count: Int, context: Context): String {
        when (content?.type) {
            PushNotificationContent.TYPE_NEW_CHAT_MESSAGE -> return context.resources.getQuantityString(R.plurals.notification_title_chat_message, count, author)
            PushNotificationContent.TYPE_NEW_JOIN_REQUEST -> return context.resources.getQuantityString(R.plurals.notification_title_join_request, count, author)
        }
        return author
    }

    fun getContentTextForCount(count: Int, context: Context): String {
        if (content != null) {
            when (content.type) {
                PushNotificationContent.TYPE_NEW_CHAT_MESSAGE -> {
                    return context.resources.getQuantityString(R.plurals.notification_text_chat_message, count, count, content.message)
                }
                PushNotificationContent.TYPE_NEW_JOIN_REQUEST -> {
                    return if (count > 1) {
                        context.resources.getQuantityString(R.plurals.notification_text_join_request_entourage_multiple, count, count)
                    } else {
                        context.getString(R.string.notification_text_join_request_entourage_single_nomsg, author)
                    }
                }
            }
            content.message?.let {
                if(it.isNotEmpty()) {
                    return it
                }
            }
        }
        return msgObject ?: ""
    }

    companion object {
        private const val serialVersionUID = 929859042482137137L
        const val HASH_SEPARATOR = ';'
        private const val CHAT_MESSAGE_NOTIFICATION_ID = 2
        private const val JOIN_REQUEST_NOTIFICATION_ID = 3
    }

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------
    init {
        val gson = Gson()
        this.content = gson.fromJson(content, PushNotificationContent::class.java)
        this.pushNotificationId = pushNotificationId
        this.pushNotificationTag = pushNotificationTag
        isVisible = true
    }
}