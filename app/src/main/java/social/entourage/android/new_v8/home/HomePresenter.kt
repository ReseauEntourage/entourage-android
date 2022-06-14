package social.entourage.android.new_v8.home

import androidx.collection.ArrayMap
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.SummaryResponse
import social.entourage.android.new_v8.models.Summary

class HomePresenter {
    var getSummarySuccess = MutableLiveData<Boolean>()
    var summary = MutableLiveData<Summary>()

    fun getSummary() {
        EntourageApplication.get().apiModule.summaryRequest
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
}