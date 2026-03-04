package social.entourage.android.api.request

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import social.entourage.android.api.model.PartnerCreateWrapper
import social.entourage.android.api.model.PartnerResponse
import social.entourage.android.api.model.PartnerUpdateWrapper
import social.entourage.android.api.model.PartnersWrapper
import social.entourage.android.api.model.PresignedUploadBody
import social.entourage.android.api.model.PresignedUrlResponse

interface AssociationsRequest {
    @GET("partners")
    fun getAllAssociations(): Call<PartnersWrapper>

    @GET("partners/{partner_id}")
    fun getPartnerDetail(@Path("partner_id") partnerId: Int): Call<PartnerResponse>

    @POST("partners")
    fun createAssociation(@Body body: PartnerCreateWrapper): Call<PartnerResponse>

    @PUT("partners/{partner_id}")
    fun updateAssociation(
        @Path("partner_id") partnerId: Int,
        @Body body: PartnerUpdateWrapper
    ): Call<PartnerResponse>

    // Ajout de la fonction pour obtenir l'URL de presigned upload
    @POST("partners/presigned_upload")
    fun getPresignedUploadUrl(@Body body: PresignedUploadBody): Call<PresignedUrlResponse>
}