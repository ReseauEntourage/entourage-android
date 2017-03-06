package social.entourage.android.api.model;

import com.google.gson.Gson;

import java.io.Serializable;

public class Message implements Serializable {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private String author;

    private String object;

    private PushNotificationContent content;

    private int pushNotificationId;

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

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }
}
