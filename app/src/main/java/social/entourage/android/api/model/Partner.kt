package social.entourage.android.api.model

import android.content.Context
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by mihaiionescu on 17/01/2017.
 */
class Partner : BaseOrganization(), Serializable {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    var id: Long = 0

    override var name: String? = null

    @SerializedName("small_logo_url")
    var smallLogoUrl: String? = null

    @SerializedName("large_logo_url")
    override var largeLogoUrl: String? = null
    @SerializedName("image_url")
    var imageUrl: String? = null
    @SerializedName("default")
    var isDefault = false
    val description: String? = null
    val phone: String? = null
    val address: String? = null
    val email: String? = null

    @SerializedName("website_url")
    val websiteUrl: String? = null

    @SerializedName("user_role_title")
    var userRoleTitle: String? = null

    @SerializedName("postal_code")
    var postalCode: String? = null
    var isCreation = false

    @SerializedName("donations_needs")
    var donationsNeeds: String? = null

    @SerializedName("volunteers_needs")
    var volunteersNeeds: String? = null

    @SerializedName("following")
    var isFollowing = false

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    override fun getTypeAsString(context: Context): String {
        return userRoleTitle ?: ""
    }

    fun isSame(partner: Partner?): Boolean {
        if (partner == null) return false
        if (id != partner.id) return false
        return partner.smallLogoUrl == smallLogoUrl
    }

    companion object {
        private const val serialVersionUID = -8314133395611710517L
    }
}

data class PartnerResponse(
    @SerializedName("partner") val partner: Partner
)

data class PartnerCreateBody(
    @SerializedName("name")
    val name: String,

    @SerializedName("image_url")
    var imageUrl: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("website_url")
    val websiteUrl: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("donations_needs")
    val donationsNeeds: String? = null,

    @SerializedName("volunteers_needs")
    val volunteersNeeds: String? = null,

    @SerializedName("staff")
    val staff: Boolean = false
)

data class PartnerCreateWrapper(
    @SerializedName("partner")
    val partner: PartnerCreateBody
)

// Wrapper pour la mise à jour du partenaire
data class PartnerUpdateWrapper(
    @SerializedName("partner")
    val partner: PartnerUpdateData
)

// Données pour la mise à jour du partenaire
// CORRECTION ICI : image_url recevra la key
data class PartnerUpdateData(
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("website_url") val websiteUrl: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("donations_needs") val donationsNeeds: String? = null,
    @SerializedName("volunteers_needs") val volunteersNeeds: String? = null,

    @SerializedName("image_url") val imageUrl: String? = null
)

// Classes pour le presigned URL upload
data class PresignedUploadBody(
    @SerializedName("content_type")
    val contentType: String
)

data class PresignedUrlResponse(
    @SerializedName("upload_key")
    val uploadKey: String? = null,
    @SerializedName("presigned_url")
    val presignedUrl: String? = null
)

data class PartnersWrapper(
    @SerializedName("partners")
    val partners: List<Partner> = emptyList()
)

class Event<out T>(private val content: T) {
    private var handled = false
    fun getContentIfNotHandled(): T? {
        if (handled) return null
        handled = true
        return content
    }
    fun peek(): T = content
}