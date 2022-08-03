package social.entourage.android.new_v8.home

import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.PedagogicResponse
import social.entourage.android.api.request.SummaryResponse
import social.entourage.android.new_v8.models.Pedago
import social.entourage.android.new_v8.models.Summary

class HomePresenter {
    var getSummarySuccess = MutableLiveData<Boolean>()
    var summary = MutableLiveData<Summary>()
    var pedagogicalContent = MutableLiveData<MutableList<Pedago>>()

    fun getSummary() {
        EntourageApplication.get().apiModule.homeRequest
            .getSummary()
            .enqueue(object : Callback<SummaryResponse> {
                override fun onResponse(
                    call: Call<SummaryResponse>,
                    response: Response<SummaryResponse>
                ) {
                    if (response.isSuccessful) {
                        summary.value = response.body()?.summary
                    }
                    getSummarySuccess.value = response.isSuccessful
                }

                override fun onFailure(call: Call<SummaryResponse>, t: Throwable) {
                    getSummarySuccess.value = false
                }
            })
    }

    fun getPedagogicalResources() {
        EntourageApplication.get().apiModule.homeRequest
            .getPedagogicalResources()
            .enqueue(object : Callback<PedagogicResponse> {
                override fun onResponse(
                    call: Call<PedagogicResponse>,
                    response: Response<PedagogicResponse>
                ) {
                    if (response.isSuccessful) {
                        pedagogicalContent.value = response.body()?.pedago
                    }
                }

                override fun onFailure(call: Call<PedagogicResponse>, t: Throwable) {
                    getSummarySuccess.value = false
                }
            })
    }

    fun setPedagogicalContentAsRead(id: Int) {
        EntourageApplication.get().apiModule.homeRequest
            .setPedagogicalContentAsRead(id)
            .enqueue(object : Callback<Boolean> {
                override fun onResponse(
                    call: Call<Boolean>,
                    response: Response<Boolean>
                ) {
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                }
            })
    }
}