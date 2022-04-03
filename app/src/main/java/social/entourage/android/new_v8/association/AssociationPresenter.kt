package social.entourage.android.new_v8.association

import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Partner
import social.entourage.android.api.request.PartnerResponse

class AssociationPresenter {
    var getPartnerSuccess = MutableLiveData<Boolean>()
    var partner = MutableLiveData<Partner>()

    fun getPartnerInfos(partnerId: Int) {
        EntourageApplication.get().components.userRequest
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
                    return
                }
            })
    }
}