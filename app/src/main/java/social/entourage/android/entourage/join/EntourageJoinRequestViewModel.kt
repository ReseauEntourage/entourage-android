package social.entourage.android.entourage.join

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.EntourageRequest
import social.entourage.android.api.model.BaseEntourage
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by mihaiionescu on 07/03/16.
 */
class EntourageJoinRequestViewModel : ViewModel() {
    private var entourageRequest: EntourageRequest = EntourageApplication.get().entourageComponent.entourageRequest

    var requestResult:MutableLiveData<Int>  = MutableLiveData<Int>(REQUEST_NORESULT)

    // ----------------------------------
    // API CALLS
    // ----------------------------------
    fun sendMessage(message: String, entourage: BaseEntourage) {
        if (message.isBlank()) {
            requestResult.value = REQUEST_OK
            return
        }
        val me = EntourageApplication.get().me() ?: return
        val info = HashMap<String, Any>()
        val messageHashMap = HashMap<String, String>()
        messageHashMap["message"] = message
        info["request"] = messageHashMap
        val call = entourageRequest.updateUserEntourageStatus(entourage.uuid, me.id, info)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                try {
                    if (response.isSuccessful) {
                        requestResult.value = REQUEST_OK
                    } else {
                        requestResult.value = REQUEST_ERROR
                    }
                } catch (e: IllegalStateException) {
                    Timber.w(e)
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                requestResult.value = REQUEST_ERROR
            }
        })
    }
    companion object {
        const val REQUEST_NORESULT =0
        const val REQUEST_OK =1
        const val REQUEST_ERROR =-1
    }
}