package social.entourage.android.new_v8.groups

import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.GroupWrapper
import social.entourage.android.new_v8.models.Group
import timber.log.Timber

class GroupPresenter {

    var isGroupCreated = MutableLiveData<Boolean>()


    fun createGroup(group: Group) {
        EntourageApplication.get().apiModule.groupRequest.createGroup(GroupWrapper(group))
            .enqueue(object : Callback<Group> {
                override fun onResponse(
                    call: Call<Group>,
                    response: Response<Group>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            isGroupCreated.value = true
                            Timber.e(it.toString())
                        } ?: run {
                            isGroupCreated.value = false
                        }
                    } else {
                        isGroupCreated.value = false
                    }
                }

                override fun onFailure(call: Call<Group>, t: Throwable) {
                    isGroupCreated.value = false
                }
            })
    }
}