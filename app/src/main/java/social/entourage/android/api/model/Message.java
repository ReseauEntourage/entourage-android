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

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public Message(String author, String object, String content) {
        this.author = author;
        this.object = object;
        Gson gson = new Gson();
        this.content = gson.fromJson(content, PushNotificationContent.class);
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

}
