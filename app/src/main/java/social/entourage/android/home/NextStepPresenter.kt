package social.entourage.android.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.NextStep
import social.entourage.android.api.model.NextStepResponse

class NextStepPresenter : ViewModel() {

    val nextStep = MutableLiveData<NextStep?>()
    val isLoading = MutableLiveData<Boolean>(false)
    val actionSuccess = MutableLiveData<Boolean>()

    fun loadNextStep() {
        isLoading.value = true
        EntourageApplication.get().apiModule.nextStepRequest
            .getNextStep()
            .enqueue(object : Callback<NextStepResponse> {
                override fun onResponse(
                    call: Call<NextStepResponse>,
                    response: Response<NextStepResponse>
                ) {
                    isLoading.value = false
                    if (response.isSuccessful) {
                        nextStep.value = response.body()?.nextStep
                    }
                }

                override fun onFailure(call: Call<NextStepResponse>, t: Throwable) {
                    isLoading.value = false
                }
            })
    }

    fun completeStep(id: Int) {
        EntourageApplication.get().apiModule.nextStepRequest
            .completeNextStep(id)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        nextStep.value = null
                        actionSuccess.value = true
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    actionSuccess.value = false
                }
            })
    }

    fun dismissStep(id: Int) {
        EntourageApplication.get().apiModule.nextStepRequest
            .dismissNextStep(id)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        loadNextStep()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                }
            })
    }

    fun tapPush() {
        // Fire and forget
        EntourageApplication.get().apiModule.nextStepRequest
            .tapPush()
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {}
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
            })
    }
}
