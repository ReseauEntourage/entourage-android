package social.entourage.android.api.model

import android.content.Context
import com.google.gson.annotations.SerializedName
import social.entourage.android.R
import java.io.Serializable

class Organization : BaseOrganization(), Serializable {

    override var name: String? = null
        private set
    var description: String? = null
    var phone: String? = null
    var address: String? = null
    @SerializedName("logo_url")
    override var largeLogoUrl: String? = null

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------

    override fun getTypeAsString(context: Context): String {
        return context.getString(R.string.member_type_organization)
    }

    companion object {
        private const val serialVersionUID = 987327632138435972L
    }

}