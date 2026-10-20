package social.entourage.android.api.model

import android.content.Context
import java.io.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

/**
 * Created by mihaiionescu on 17/01/2017.
 */
@KSerializable
class Partner : BaseOrganization(), Serializable {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    var id: Long = 0

    override var name: String? = null

    @SerialName("small_logo_url")
    var smallLogoUrl: String? = null

    @SerialName("large_logo_url")
    override var largeLogoUrl: String? = null
    @SerialName("image_url")
    var imageUrl: String? = null
    @SerialName("default")
    var isDefault = false
    val description: String? = null
    val phone: String? = null
    val address: String? = null
    val email: String? = null

    @SerialName("website_url")
    val websiteUrl: String? = null

    @SerialName("user_role_title")
    var userRoleTitle: String? = null

    @SerialName("postal_code")
    var postalCode: String? = null
    var isCreation = false

    @SerialName("donations_needs")
    var donationsNeeds: String? = null

    @SerialName("volunteers_needs")
    var volunteersNeeds: String? = null

    @SerialName("following")
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

@KSerializable
data class PartnerResponse(
    @SerialName("partner") val partner: Partner
)

@KSerializable
data class PartnerCreateBody(
    @SerialName("name")
    val name: String = "",

    @SerialName("image_url")
    var imageUrl: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("phone")
    val phone: String? = null,

    @SerialName("address")
    val address: String? = null,

    @SerialName("latitude")
    val latitude: Double? = null,

    @SerialName("longitude")
    val longitude: Double? = null,

    @SerialName("website_url")
    val websiteUrl: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("donations_needs")
    val donationsNeeds: String? = null,

    @SerialName("volunteers_needs")
    val volunteersNeeds: String? = null,

    @SerialName("staff")
    val staff: Boolean = false
)

@KSerializable
data class PartnerCreateWrapper(
    @SerialName("partner")
    val partner: PartnerCreateBody
)

// Wrapper pour la mise à jour du partenaire
@KSerializable
data class PartnerUpdateWrapper(
    @SerialName("partner")
    val partner: PartnerUpdateData
)

// Données pour la mise à jour du partenaire
// CORRECTION ICI : image_url recevra la key
@KSerializable
data class PartnerUpdateData(
    @SerialName("name") val name: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("website_url") val websiteUrl: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("donations_needs") val donationsNeeds: String? = null,
    @SerialName("volunteers_needs") val volunteersNeeds: String? = null,

    @SerialName("image_url") val imageUrl: String? = null
)

// Classes pour le presigned URL upload
@KSerializable
data class PresignedUploadBody(
    @SerialName("content_type")
    val contentType: String = ""
)

@KSerializable
data class PresignedUrlResponse(
    @SerialName("upload_key")
    val uploadKey: String? = null,
    @SerialName("presigned_url")
    val presignedUrl: String? = null
)

@KSerializable
data class PartnersWrapper(
    @SerialName("partners")
    val partners: List<Partner> = emptyList()
)

@KSerializable
class Event<out T>(private val content: T) {
    private var handled = false
    fun getContentIfNotHandled(): T? {
        if (handled) return null
        handled = true
        return content
    }
    fun peek(): T = content
}