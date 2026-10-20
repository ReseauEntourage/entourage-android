package social.entourage.android.api.request

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import social.entourage.android.api.model.Partner
import social.entourage.android.api.model.PartnerCreateWrapper
import social.entourage.android.api.model.PartnerResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

/**
 * Created by mihaiionescu on 17/01/2017.
 */
interface PartnerRequest {

    @get:GET("partners")
    val allPartners: Call<PartnersResponse>

    @POST("partners")
    fun createPartner(@Body wrapper: PartnerCreateWrapper): Call<PartnerResponse>
}

/**
 * Wrapper pour les requêtes qui attendent un objet Partner dans un champ "partner"
 */
@KSerializable
class PartnerWrapper(
    @SerialName("partner") var partner: Partner
)

/**
 * Réponse liste : { "partners": [ ... ] }
 */
@KSerializable
class PartnersResponse(
    @SerialName("partners") var partners: List<Partner> = emptyList()
)
