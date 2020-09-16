package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import social.entourage.android.api.model.SharingEntourage

/**
 * Created by Jr (MJ-DEVS) on 09/09/2020.
 */

class SharingResponse(@field:SerializedName("sharing")val sharing: List<SharingEntourage>)

interface SharingRequest {
    @GET("sharing/groups")
    fun retrieveSharing(): Call<SharingResponse>

}