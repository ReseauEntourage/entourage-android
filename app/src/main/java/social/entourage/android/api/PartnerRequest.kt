package social.entourage.android.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import social.entourage.android.api.model.Partner

/**
 * Created by mihaiionescu on 17/01/2017.
 */
interface PartnerRequest {
    @get:GET("partners")
    val allPartners: Call<PartnersResponse>
}

class PartnerResponse(@field:SerializedName("partner") var partner: Partner)

class PartnersResponse {
    @SerializedName("partners")
    var partners: List<Partner>? = null
}