package social.entourage.android.api.request

import retrofit2.Call
import retrofit2.http.GET
import social.entourage.android.api.model.SharingEntourage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

/**
 * Created by Jr (MJ-DEVS) on 09/09/2020.
 */

@KSerializable
class SharingResponse(@SerialName("sharing")val sharing: List<SharingEntourage> = emptyList())

interface SharingRequest {
    @GET("sharing/groups")
    fun retrieveSharing(): Call<SharingResponse>

}