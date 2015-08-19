package social.entourage.android.api.model;

import java.io.Serializable;

public class Message implements Serializable {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private String author;

    private String object;

    private String content;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public Message(String author, String object, String content) {
        this.author = author;
        this.object = object;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
