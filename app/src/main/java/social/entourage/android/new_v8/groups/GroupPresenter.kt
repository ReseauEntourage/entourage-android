package social.entourage.android.new_v8.groups

import androidx.collection.ArrayMap
import androidx.lifecycle.MutableLiveData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.*
import social.entourage.android.new_v8.groups.list.groupPerPage
import social.entourage.android.new_v8.models.Group
import timber.log.Timber
import java.io.File
import java.io.IOException

class GroupPresenter {

    var isGroupCreated = MutableLiveData<Boolean>()
    var getGroup = MutableLiveData<Group>()
    var getAllGroups = MutableLiveData<MutableList<Group>>()
    var getGroupsSearch = MutableLiveData<MutableList<Group>>()
    var getAllMyGroups = MutableLiveData<MutableList<Group>>()
    var isGroupUpdated = MutableLiveData<Boolean>()
    var hasUserJoinedGroup = MutableLiveData<Boolean>()
    var hasUserLeftGroup = MutableLiveData<Boolean>()
    var hasPost = MutableLiveData<Boolean>()

    var isLoading: Boolean = false
    var isLastPage: Boolean = false


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

    fun getGroup(id: Int) {
        EntourageApplication.get().apiModule.groupRequest.getGroup(id)
            .enqueue(object : Callback<GroupWrapper> {
                override fun onResponse(
                    call: Call<GroupWrapper>,
                    response: Response<GroupWrapper>
                ) {
                    Timber.e(response.body().toString())
                    if (response.isSuccessful) {
                        response.body()?.let { groupWrapper ->
                            getGroup.value = groupWrapper.group
                        }
                    }
                }

                override fun onFailure(call: Call<GroupWrapper>, t: Throwable) {
                }
            })
    }

    fun updateGroup(id: Int, userEdited: ArrayMap<String, Any>) {
        EntourageApplication.get().apiModule.groupRequest.updateGroup(id, userEdited)
            .enqueue(object : Callback<GroupWrapper> {
                override fun onResponse(
                    call: Call<GroupWrapper>,
                    response: Response<GroupWrapper>
                ) {
                    isGroupUpdated.value = response.isSuccessful && response.body()?.group != null
                }

                override fun onFailure(call: Call<GroupWrapper>, t: Throwable) {
                    isGroupUpdated.value = false
                }
            })
    }

    fun getAllGroups(page: Int, per: Int) {
        EntourageApplication.get().apiModule.groupRequest.getAllGroups(page, per)
            .enqueue(object : Callback<GroupsListWrapper> {
                override fun onResponse(
                    call: Call<GroupsListWrapper>,
                    response: Response<GroupsListWrapper>
                ) {
                    response.body()?.let { allGroupsWrapper ->
                        if (allGroupsWrapper.allGroups.size < groupPerPage) isLastPage = true
                        getAllGroups.value = allGroupsWrapper.allGroups
                    }
                }

                override fun onFailure(call: Call<GroupsListWrapper>, t: Throwable) {
                }
            })
    }

    fun getGroupsSearch(searchTxt: String) {
        EntourageApplication.get().apiModule.groupRequest.getGroupsSearch(searchTxt)
            .enqueue(object : Callback<GroupsListWrapper> {
                override fun onResponse(
                    call: Call<GroupsListWrapper>,
                    response: Response<GroupsListWrapper>
                ) {
                    response.body()?.let { allGroupsWrapper ->
                        getGroupsSearch.value = allGroupsWrapper.allGroups
                    }
                }

                override fun onFailure(call: Call<GroupsListWrapper>, t: Throwable) {
                }
            })
    }

    fun getMyGroups(page: Int, per: Int, userId: Int) {
        EntourageApplication.get().apiModule.groupRequest.getMyGroups(userId, page, per)
            .enqueue(object : Callback<GroupsListWrapper> {
                override fun onResponse(
                    call: Call<GroupsListWrapper>,
                    response: Response<GroupsListWrapper>
                ) {
                    response.body()?.let { allGroupsWrapper ->
                        if (allGroupsWrapper.allGroups.size < groupPerPage) isLastPage = true
                        getAllMyGroups.value = allGroupsWrapper.allGroups
                    }
                }

                override fun onFailure(call: Call<GroupsListWrapper>, t: Throwable) {
                }
            })
    }

    fun joinGroup(groupId: Int) {
        EntourageApplication.get().apiModule.groupRequest.joinGroup(groupId)
            .enqueue(object : Callback<EntourageUserResponse> {
                override fun onResponse(
                    call: Call<EntourageUserResponse>,
                    response: Response<EntourageUserResponse>
                ) {
                    hasUserJoinedGroup.value =
                        response.isSuccessful && response.body()?.user != null

                }

                override fun onFailure(call: Call<EntourageUserResponse>, t: Throwable) {
                    hasUserJoinedGroup.value = false
                }
            })
    }


    fun leaveGroup(groupId: Int) {
        EntourageApplication.get().apiModule.groupRequest.leaveGroup(groupId)
            .enqueue(object : Callback<EntourageUserResponse> {
                override fun onResponse(
                    call: Call<EntourageUserResponse>,
                    response: Response<EntourageUserResponse>
                ) {
                    hasUserLeftGroup.value =
                        response.isSuccessful && response.body()?.user != null

                }

                override fun onFailure(call: Call<EntourageUserResponse>, t: Throwable) {
                    hasUserLeftGroup.value = false
                }
            })
    }

    fun addPost(file: File, groupId: Int) {
        val request = RequestContent("image/jpeg")
        EntourageApplication.get().apiModule.groupRequest.prepareAddPost(groupId, request)
            .enqueue(object : Callback<PrepareAddPostResponse> {
                override fun onResponse(
                    call: Call<PrepareAddPostResponse>,
                    response: Response<PrepareAddPostResponse>
                ) {
                    Timber.e(response.body().toString())
                    if (response.isSuccessful) {
                        val presignedUrl = response.body()?.presignedUrl
                        var uploadKey = response.body()?.uploadKey
                        presignedUrl?.let {
                            uploadFile(file, presignedUrl)
                        }
                    }
                }

                override fun onFailure(call: Call<PrepareAddPostResponse>, t: Throwable) {
                    hasUserLeftGroup.value = false
                }
            })
    }

    fun addPost(groupId: Int, params: ArrayMap<String, Any>) {
        EntourageApplication.get().apiModule.groupRequest.addPost(groupId, params)
            .enqueue(object : Callback<ChatMessageResponse> {
                override fun onResponse(
                    call: Call<ChatMessageResponse>,
                    response: Response<ChatMessageResponse>
                ) {
                    hasPost.value = response.isSuccessful
                }

                override fun onFailure(call: Call<ChatMessageResponse>, t: Throwable) {
                    hasPost.value = false
                }
            })
    }

    fun uploadFile(file: File, presignedUrl: String) {
        val client: OkHttpClient = EntourageApplication.get().apiModule.okHttpClient
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(presignedUrl)
            .put(requestBody)
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Timber.e("response ${e.message}")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                Timber.e("response ${response.body}")
            }
        })
    }
}