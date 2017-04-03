package social.entourage.android.api.model.map;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by mihaiionescu on 13/10/16.
 */

public class LastMessage implements Serializable {

    private static final long serialVersionUID = -5067887626222236770L;

    @SerializedName("text")
    private String text;

    @SerializedName("author")
    private LastMessageAuthor author;

    public String getText() {
        // If no author, return just the text
        if (author == null) {
            return text;
        }
        StringBuilder fulltext = new StringBuilder();
        // Add the first name
        String firstName = author.getFirstName();
        if (firstName != null && firstName.length() > 0) {
            fulltext.append(firstName);
        }
        // Add the last name
        String lastName = author.getLastName();
        if (lastName != null && lastName.length() > 0) {
            if (fulltext.length() > 0) {
                fulltext.append(" ");
                // only the first letter
                fulltext.append(lastName.charAt(0));
            } else {
                fulltext.append(lastName);
            }
        }
        // Add the text
        if (fulltext.length() > 0) fulltext.append(": ");
        fulltext.append(text);

        return fulltext.toString();
    }

    public void setText(final String text) {
        this.text = text;
    }

}
