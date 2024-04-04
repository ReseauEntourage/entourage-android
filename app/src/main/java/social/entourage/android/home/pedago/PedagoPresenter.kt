package social.entourage.android.home.pedago

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Pedago
import social.entourage.android.api.request.PedagogicSingleResponse

class PedagoPresenter: ViewModel()  {
    var pedagolSingle = MutableLiveData<Pedago>()

    fun getPedagogicalResource(resourceId:String) {
        EntourageApplication.get().apiModule.homeRequest
            .getPedagogicalResourceWithHash(resourceId)
            .enqueue(object : Callback<PedagogicSingleResponse> {
                override fun onResponse(
                    call: Call<PedagogicSingleResponse>,
                    response: Response<PedagogicSingleResponse>
                ) {
                    if (response.isSuccessful) {
                        pedagolSingle.value = response.body()?.pedago
                    }
                }

                override fun onFailure(call: Call<PedagogicSingleResponse>, t: Throwable) {

                }
            })
    }

}