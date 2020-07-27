package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by mihaiionescu on 13/10/16.
 */
class LastMessage : Serializable {
    @SerializedName("text")
    private var text: String? = null

    @SerializedName("author")
    private var author: LastMessageAuthor? = null

    fun getText(): String? {
        author?.let { author ->
            val fulltext = StringBuilder()
            // Add the first name
            author.firstName?.let { firstName ->
                if (firstName.isNotBlank()) {
                    fulltext.append(firstName)
                }
            }
            // Add the last name
            author.lastName?.let { lastName ->
                if (lastName.isNotBlank()) {
                    if (fulltext.isNotBlank()) {
                        fulltext.append(" ")
                        // only the first letter
                        fulltext.append(Character.toChars(lastName.codePointAt(0)))
                    } else {
                        fulltext.append(lastName)
                    }
                }
            }
            // Add the text
            if (fulltext.isNotBlank()) fulltext.append(": ")
            fulltext.append(text)
            return fulltext.toString()
        } ?: run {
            return text  // If no author, return just the text
        }
    }

    fun setMessage(text: String, author_display_name: String) {
        this.text = "$author_display_name: $text"
        author = null
    }

    companion object {
        private const val serialVersionUID = -5067117626222236770L
    }
}