package social.entourage.android.profile.settings

import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.UserResponse

class SettingsPresenter {

    var accountDeleted: MutableLiveData<Boolean> = MutableLiveData()

    fun deleteAccount() {
        EntourageApplication.get().apiModule.userRequest.deleteUser()
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    accountDeleted.value = true
                    EntourageApplication.get().logOut()
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    accountDeleted.value = false
                }
            })
    }
}