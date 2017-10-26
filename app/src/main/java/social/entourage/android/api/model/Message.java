package social.entourage.android.api.model;

import android.content.Context;

import com.google.gson.Gson;

import java.io.Serializable;

import social.entourage.android.R;

public class Message implements Serializable {

    private static final long serialVersionUID = 929859042482137137L;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private String author;

    private String object;

    private PushNotificationContent content;

    private int pushNotificationId;

    private String pushNotificationTag;

    private boolean visible;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public Message(String author, String object, String content, int pushNotificationId) {
        this.author = author;
        this.object = object;
        Gson gson = new Gson();
        this.content = gson.fromJson(content, PushNotificationContent.class);
        this.pushNotificationId = pushNotificationId;
        this.visible = true;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getMessage() {
        if (content == null) return "";
        return content.message;
    }

    public PushNotificationContent getContent() {
        return content;
    }

    public int getPushNotificationId() {
        return pushNotificationId;
    }

    public void setPushNotificationId(final int pushNotificationId) {
        this.pushNotificationId = pushNotificationId;
    }

    public String getPushNotificationTag() {
        return pushNotificationTag;
    }

    public void setPushNotificationTag(final String pushNotificationTag) {
        this.pushNotificationTag = pushNotificationTag;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    public String getHash() {
        if (pushNotificationTag == null) return String.valueOf(pushNotificationId);
        return pushNotificationTag + "-" + String.valueOf(pushNotificationId);
    }

    public String getContentTitleForCount(int count, Context context) {
        if (count > 1) {
            if (content != null) {
                if (PushNotificationContent.TYPE_NEW_CHAT_MESSAGE.equals(content.getType())) {
                    return context.getString(R.string.notification_title_chat_message);
                }
                return author;
            } else {
                return author;
            }
        } else {
            return author;
        }
    }

    public String getContentTextForCount(int count, Context context) {
        if (count > 1) {
            if (content != null) {
                String contentType = content.getType();
                if (PushNotificationContent.TYPE_NEW_CHAT_MESSAGE.equals(contentType)) {
                    return context.getString(R.string.notification_text_chat_message, count);
                }
                if (PushNotificationContent.TYPE_NEW_JOIN_REQUEST.equals(contentType)) {
                    String notificationText = "";
                    if (content.isEntourageRelated()) {
                        notificationText = context.getString(R.string.notification_text_join_request_entourage_multiple, count, content.getFeedItemName());
                    } else {
                        notificationText = context.getString(R.string.notification_text_join_request_tour_multiple, count);
                    }
                    return notificationText;
                }
                return object;
            } else {
                return object;
            }
        } else {
            if (content != null) {
                String contentType = content.getType();
                if (PushNotificationContent.TYPE_NEW_JOIN_REQUEST.equals(contentType)) {
                    String notificationText = "";
                    if (content.isEntourageRelated()) {
                        notificationText = context.getString(R.string.notification_text_join_request_entourage_single, author, content.getFeedItemName());
                    } else {
                        notificationText = context.getString(R.string.notification_text_join_request_tour_single, author);
                    }
                    return notificationText;
                }
                return object;
            } else {
                return object;
            }
        }
    }

}
