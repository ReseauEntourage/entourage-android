package social.entourage.android.user

import android.util.Log
import androidx.lifecycle.MutableLiveData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.User
import social.entourage.android.api.model.UserReport
import social.entourage.android.api.model.UserReportWrapper
import social.entourage.android.api.request.UserResponse

class UserPresenter {

    var isGetUserSuccess = MutableLiveData<Boolean>()
    var isUserReported = MutableLiveData<Boolean>()
    var user = MutableLiveData<User>()
    var isLanguageChanged = MutableLiveData<Boolean>()


    fun getUser(userId: Int) {
        EntourageApplication.get().apiModule.userRequest.getUser(userId)
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.user?.let { user.value = it }
                        isGetUserSuccess.value = true
                    } else {
                        isGetUserSuccess.value = false
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    isGetUserSuccess.value = false
                }
            })
    }

    fun updateLanguage(userId: Int,lang:String) {
        EntourageApplication.get().apiModule.userRequest.updateLanguage(userId, lang)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Log.wtf("wtf", "eho passed")
                        isLanguageChanged.value = true
                    } else {
                        Log.wtf("wtf", "eho not passed")
                       isLanguageChanged.value = false
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.wtf("wtf", "eho passed")
                    isLanguageChanged.value = true
                }
            })
    }

    fun sendReport(
        entourageId: Int,
        reason: String,
        selectedSignalsIdList: MutableList<String>
    ) {
        val userRequest = EntourageApplication.get().apiModule.userRequest
        val call = userRequest.reportUser(
            entourageId, UserReportWrapper(UserReport(reason, selectedSignalsIdList))
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                isUserReported.value = false
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                isUserReported.value = response.isSuccessful
            }
        })
    }
}