package social.entourage.android.api.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.google.gson.annotations.SerializedName
import social.entourage.android.R
import java.io.Serializable
import java.util.*

/**
 * Created by Mihai Ionescu on 25/05/2018.
 */
class UserMembership : Serializable {
    @SerializedName("id")
    var membershipId = 0

    @SerializedName("title")
    var membershipTitle: String? = null

    @SerializedName("number_of_people")
    var numberOfPeople = 0
    var type: String? = null

    //TODO Return an UUID from the server
    val membershipUUID: String
        get() = membershipId.toString()

    fun getIconDrawable(context: Context?): Drawable? {
        return when {
            BaseEntourage.GROUPTYPE_PRIVATE_CIRCLE.equals(type, ignoreCase = true) -> {
                AppCompatResources.getDrawable(context!!, R.drawable.ic_favorite_border_black_24dp)
            }
            BaseEntourage.GROUPTYPE_NEIGHBORHOOD.equals(type, ignoreCase = true) -> {
                AppCompatResources.getDrawable(context!!, R.drawable.ic_neighborhood)
            }
            else -> null
        }
    }

    class UserMembershipList : Serializable {
        lateinit var type: String
        lateinit var list: ArrayList<UserMembership>

        companion object {
            private const val serialVersionUID = 8567799380837512524L
        }
    }

    companion object {
        private const val serialVersionUID = -1826012193466373939L
    }
}