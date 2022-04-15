package social.entourage.android.new_v8.association

import androidx.collection.ArrayMap
import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Partner
import social.entourage.android.api.request.PartnerResponse

class AssociationPresenter {
    var getPartnerSuccess = MutableLiveData<Boolean>()
    var followSuccess = MutableLiveData<Boolean>()
    var partner = MutableLiveData<Partner>()

    fun getPartnerInfos(partnerId: Int) {
        EntourageApplication.get().apiModule.userRequest
            .getPartnerDetail(partnerId)
            .enqueue(object : Callback<PartnerResponse> {
                override fun onResponse(
                    call: Call<PartnerResponse>,
                    response: Response<PartnerResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { partner.value = it.partner }
                        getPartnerSuccess.value = true
                    } else {
                        getPartnerSuccess.value = false
                    }
                }

                override fun onFailure(call: Call<PartnerResponse>, t: Throwable) {
                    getPartnerSuccess.value = false
                }
            })
    }

    fun updatePartnerFollow(isFollow: Boolean, partnerId: Long) {
        val params = ArrayMap<String, Any>()
        val isFollowParam = ArrayMap<String, Any>()
        isFollowParam["partner_id"] = partnerId.toString()
        isFollowParam["active"] = isFollow.toString()
        params["following"] = isFollowParam

        EntourageApplication.get().apiModule.userRequest.updateUserPartner(params)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        partner.let {
                            it.value?.isFollowing = isFollow
                            followSuccess.value = true
                        }
                    } else {
                        followSuccess.value = false
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    followSuccess.value = true
                }
            })
    }
}