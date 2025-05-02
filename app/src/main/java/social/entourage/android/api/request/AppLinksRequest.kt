package social.entourage.android.api.request

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface AppLinksRequest {

    @GET("outings/{id}")
    fun getEventFromHash(@Path("id") eventId: String): Call<EventWrapper>

    @GET("contributions/{id}")
    fun getContributionFromHash(@Path("id") contribId: String): Call<ContribWrapper>

    @GET("solicitations/{id}")
    fun getDemandFromHash(@Path("id") demandId: String): Call<DemandWrapper>

    @GET("neighborhoods/{id}")
    fun getGroupFromHash(@Path("id") groupId: String): Call<GroupWrapper>

    @GET("conversations/{id}")
    fun getDiscussionFromHash(@Path("id") groupId: String): Call<DiscussionDetailWrapper>

}