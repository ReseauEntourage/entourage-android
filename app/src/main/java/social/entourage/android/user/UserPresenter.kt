package social.entourage.android.user

import android.location.Location
import android.util.Log
import androidx.collection.ArrayMap
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
import timber.log.Timber

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

    fun updateUser(willing:Boolean) {
        val user = ArrayMap<String, Any>()
        user["willing_to_engage_locally"] = willing
        val request = ArrayMap<String, Any>()
        request["user"] = user

        val call = EntourageApplication.get().apiModule.userRequest.updateUser(request)
        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    Timber.wtf("User updated for plural discussion")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Timber.e(t)
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
                        isLanguageChanged.value = true
                    } else {
                       isLanguageChanged.value = false
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
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