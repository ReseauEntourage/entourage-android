package social.entourage.android.api.model.feed

import social.entourage.android.api.model.LastMessageAuthor
import java.io.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

/**
 * Created by mihaiionescu on 13/10/16.
 */
@KSerializable
class FeedLastMessage : Serializable {
    @SerialName("text")
    private var text: String? = null

    @SerialName("author")
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