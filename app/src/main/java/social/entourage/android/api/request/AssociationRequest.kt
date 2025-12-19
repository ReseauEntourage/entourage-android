package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import social.entourage.android.api.model.Partner
import social.entourage.android.api.model.PartnerCreateWrapper
import social.entourage.android.api.model.PartnerResponse

interface AssociationsRequest {
    @GET("partners")
    fun getAllAssociations(): Call<PartnersWrapper>

    @GET("partners/{partner_id}")
    fun getPartnerDetail(@Path("partner_id") partnerId: Int): Call<PartnerResponse>

    @POST("partners")
    fun createAssociation(@Body body: PartnerCreateWrapper): Call<PartnerResponse>
}

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
