package social.entourage.android.api.model

import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.io.Serializable

/**
 * Created by mihaiionescu on 13/10/16.
 */
class LastMessage : Serializable {
    @SerializedName("text")
    private var text: String? = null

    @SerializedName("author")
    private var author: LastMessageAuthor? = null

    fun getText(userId:Int): String? {
        author?.let { author ->
            val fulltext = StringBuilder()
            // Add display name
            if (author.authorId == userId ) {
                fulltext.append("Vous")
            }
            else {
                author.displayName?.let { displayName ->
                    if (displayName.isNotBlank()) {
                        fulltext.append(displayName)
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