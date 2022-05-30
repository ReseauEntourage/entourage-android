package social.entourage.android.new_v8.groups

import androidx.collection.ArrayMap
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.request.*
import social.entourage.android.new_v8.groups.details.feed.postPerPage
import social.entourage.android.new_v8.groups.list.groupPerPage
import social.entourage.android.new_v8.models.Group
import social.entourage.android.new_v8.models.Post
import timber.log.Timber

class GroupPresenter {

    var isGroupCreated = MutableLiveData<Boolean>()
    var getGroup = MutableLiveData<Group>()
    var getAllGroups = MutableLiveData<MutableList<Group>>()
    var getGroupsSearch = MutableLiveData<MutableList<Group>>()
    var getAllMyGroups = MutableLiveData<MutableList<Group>>()
    var getMembers = MutableLiveData<MutableList<EntourageUser>>()
    var getMembersSearch = MutableLiveData<MutableList<EntourageUser>>()
    var isGroupUpdated = MutableLiveData<Boolean>()
    var hasUserJoinedGroup = MutableLiveData<Boolean>()
    var hasUserLeftGroup = MutableLiveData<Boolean>()
    var getAllPosts = MutableLiveData<MutableList<Post>>()

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

    fun getGroupMembers(groupId: Int) {
        EntourageApplication.get().apiModule.groupRequest.getMembers(groupId)
            .enqueue(object : Callback<GroupsMembersWrapper> {
                override fun onResponse(
                    call: Call<GroupsMembersWrapper>,
                    response: Response<GroupsMembersWrapper>
                ) {
                    response.body()?.let { allMembersWrapper ->
                        getMembers.value = allMembersWrapper.users
                    }

                }

                override fun onFailure(call: Call<GroupsMembersWrapper>, t: Throwable) {
                }
            })
    }

    fun getGroupPosts(groupId: Int) {
        EntourageApplication.get().apiModule.groupRequest.getGroupPosts(groupId)
            .enqueue(object : Callback<GroupsPostsWrapper> {
                override fun onResponse(
                    call: Call<GroupsPostsWrapper>,
                    response: Response<GroupsPostsWrapper>
                ) {
                    response.body()?.let { allPostsWrapper ->
                        getAllPosts.value = allPostsWrapper.posts
                    }

                }

                override fun onFailure(call: Call<GroupsPostsWrapper>, t: Throwable) {
                }
            })
    }

    fun getGroupMembersSearch(searchTxt: String) {
        val listTmp: MutableList<EntourageUser> = mutableListOf()
        getMembers.value?.forEach {
            if (it.displayName?.lowercase()?.contains(searchTxt.lowercase()) == true) {
                listTmp.add(it)
            }
        }
        getMembersSearch.value = listTmp
    }
}