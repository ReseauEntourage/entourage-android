package social.entourage.android.api;

import retrofit2.Call;
import retrofit2.http.GET;
import social.entourage.android.api.model.Partner;

/**
 * Created by mihaiionescu on 17/01/2017.
 */

public interface PartnerRequest {

    @GET("partners")
    Call<Partner.PartnersWrapper> getAllPartners();

}
