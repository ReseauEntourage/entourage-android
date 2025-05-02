package social.entourage.android.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Group
import social.entourage.android.api.request.*
import social.entourage.android.api.model.notification.InAppNotification
import social.entourage.android.api.model.notification.InAppNotificationPermission
import social.entourage.android.api.model.Pedago
import social.entourage.android.api.model.Summary
import social.entourage.android.events.list.EVENTS_PER_PAGE
import social.entourage.android.groups.list.groupPerPage

class HomePresenter: ViewModel() {
    var getSummarySuccess = MutableLiveData<Boolean>()
    var summary = MutableLiveData<Summary>()
    var pedagogicalContent = MutableLiveData<MutableList<Pedago>>()
    var pedagolSingle = MutableLiveData<Pedago>()

    var unreadMessages = MutableLiveData<UnreadMessages?>()

    var notifsCount = MutableLiveData<Int>()
    var notificationsPermission = MutableLiveData<InAppNotificationPermission?>()

    var notificationsInApp = MutableLiveData<MutableList<InAppNotification>?>()
    var notificationInApp = MutableLiveData<InAppNotification?>()

    var getAllMyGroups = MutableLiveData<MutableList<Group>>()
    var getAllEvents = MutableLiveData<MutableList<Events>>()
    var getAllActions = MutableLiveData<MutableList<Action>>()

    var isLoading: Boolean = false
    var isLastPage: Boolean = false
    var isLastPageGroup: Boolean = false
    var isLastPageEvent: Boolean = false
    var isLastPageAction: Boolean = false

    fun getSummary() {
        EntourageApplication.get().apiModule.homeRequest
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

    fun getMyGroups(page: Int, per: Int, userId: Int) {
        EntourageApplication.get().apiModule.groupRequest.getMyGroups(userId, page, per)
            .enqueue(object : Callback<GroupsListWrapper> {
                override fun onResponse(
                    call: Call<GroupsListWrapper>,
                    response: Response<GroupsListWrapper>
                ) {
                    response.body()?.let { allGroupsWrapper ->
                        if (allGroupsWrapper.allGroups.size < groupPerPage) isLastPageGroup = true
                        getAllMyGroups.value = allGroupsWrapper.allGroups
                    }
                }

                override fun onFailure(call: Call<GroupsListWrapper>, t: Throwable) {
                }
            })
    }

    fun getAllEvents(page: Int, per: Int,distance:Int?,latitude:Double?,longitude:Double?,period:String) {
        EntourageApplication.get().apiModule.eventsRequest.getAllEvents(page, per,distance,latitude,longitude,period)
            .enqueue(object : Callback<EventsListWrapper> {
                override fun onResponse(
                    call: Call<EventsListWrapper>,
                    response: Response<EventsListWrapper>
                ) {

                    response.body()?.let { allEventsWrapper ->
                        if (allEventsWrapper.allEvents.size < EVENTS_PER_PAGE) isLastPageEvent = true
                        getAllEvents.postValue(allEventsWrapper.allEvents)

                    }
                }

                override fun onFailure(call: Call<EventsListWrapper>, t: Throwable) {
                }
            })
    }

    fun getAllContribs(page: Int, per: Int,distance:Int?,latitude:Double?,longitude:Double?,sections: String?) {
        EntourageApplication.get().apiModule.actionsRequest.getAllActionsContrib(page,per,sections,distance,latitude,longitude)
            .enqueue(object : Callback<ContribsListWrapper> {
                override fun onResponse(
                    call: Call<ContribsListWrapper>,
                    response: Response<ContribsListWrapper>
                ) {
                    response.body()?.let { allActionsWrapper ->
                        if (allActionsWrapper.allActions.size < EVENTS_PER_PAGE) isLastPageAction = true
                        getAllActions.value = allActionsWrapper.allActions
                    }
                }
                override fun onFailure(call: Call<ContribsListWrapper>, t: Throwable) {}
            })
    }

    fun getAllDemands(page: Int, per: Int,distance:Int?,latitude:Double?,longitude:Double?,sections: String?) {
        EntourageApplication.get().apiModule.actionsRequest.getAllActionsDemandWithoutMine(page,per,sections,distance,latitude,longitude, true)
            .enqueue(object : Callback<DemandsListWrapper> {
                override fun onResponse(
                    call: Call<DemandsListWrapper>,
                    response: Response<DemandsListWrapper>
                ) {
                    response.body()?.let { allActionsWrapper ->
                        if (allActionsWrapper.allActions.size < EVENTS_PER_PAGE) isLastPageAction = true
                        getAllActions.value = allActionsWrapper.allActions
                    }
                }
                override fun onFailure(call: Call<DemandsListWrapper>, t: Throwable) {}
            })
    }

    fun getPedagogicalResources() {
        EntourageApplication.get().apiModule.homeRequest
            .getPedagogicalResources(true)
            .enqueue(object : Callback<PedagogicResponse> {
                override fun onResponse(
                    call: Call<PedagogicResponse>,
                    response: Response<PedagogicResponse>
                ) {
                    if (response.isSuccessful) {
                        pedagogicalContent.value = response.body()?.pedago
                    }
                }

                override fun onFailure(call: Call<PedagogicResponse>, t: Throwable) {
                    getSummarySuccess.value = false
                }
            })
    }



    fun getPedagogicalResource(resourceId:Int) {
        EntourageApplication.get().apiModule.homeRequest
            .getPedagogicalResource(resourceId)
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

    fun getUnreadCount() {
        EntourageApplication.get().apiModule.userRequest.getUnreadCountForUser()
            .enqueue(object : Callback<UnreadCountWrapper> {
                override fun onResponse(
                    call: Call<UnreadCountWrapper>,
                    response: Response<UnreadCountWrapper>
                ) {
                    if (response.isSuccessful) {
                        unreadMessages.value = response.body()?.unreadMessages
                    }
                }

                override fun onFailure(call: Call<UnreadCountWrapper>, t: Throwable) {
                    unreadMessages.value = null
                }
            })
    }

    fun setPedagogicalContentAsRead(id: Int) {
        EntourageApplication.get().apiModule.homeRequest
            .setPedagogicalContentAsRead(id)
            .enqueue(object : Callback<Boolean> {
                override fun onResponse(
                    call: Call<Boolean>,
                    response: Response<Boolean>
                ) {
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                }
            })
    }

    //Notifs
    fun getNotificationsCount() {
        EntourageApplication.get().apiModule.homeRequest.getNotificationsCount()
            .enqueue(object : Callback<NotificationsCountResponse> {
                override fun onResponse(
                    call: Call<NotificationsCountResponse>,
                    response: Response<NotificationsCountResponse>
                ) {
                    if (response.isSuccessful) {
                        notifsCount.value = response.body()?.count
                    }
                }

                override fun onFailure(call: Call<NotificationsCountResponse>, t: Throwable) {
                    notifsCount.value = 0
                }
            })
    }

    fun getNotifications(page: Int, per: Int) {
        isLoading = true
        EntourageApplication.get().apiModule.homeRequest.getNotifications(page, per)
            .enqueue(object : Callback<NotificationsInAppResponse> {
                override fun onResponse(
                    call: Call<NotificationsInAppResponse>,
                    response: Response<NotificationsInAppResponse>
                ) {
                    isLoading = false
                    if (response.isSuccessful) {
                        if ((response.body()?.notifs?.size ?: 0) < per) isLastPage = true
                        notificationsInApp.value = response.body()?.notifs
                    }
                }

                override fun onFailure(call: Call<NotificationsInAppResponse>, t: Throwable) {
                    isLoading = false
                    notificationsInApp.value = null
                }
            })
    }

    fun markReadNotification(notifId:Int) {
        EntourageApplication.get().apiModule.homeRequest.markReadNotif(notifId)
            .enqueue(object : Callback<NotificationInAppResponse> {
                override fun onResponse(
                    call: Call<NotificationInAppResponse>,
                    response: Response<NotificationInAppResponse>
                ) {
                    if (response.isSuccessful) {
                        notificationInApp.value = response.body()?.notif
                    }
                }

                override fun onFailure(call: Call<NotificationInAppResponse>, t: Throwable) {
                    notificationInApp.value = null
                }
            })
    }

    //Notif permissions
    fun getNotificationsPermissions() {
        EntourageApplication.get().apiModule.homeRequest.getNotificationsPermissions()
            .enqueue(object : Callback<NotificationPermissionsResponse> {
                override fun onResponse(
                    call: Call<NotificationPermissionsResponse>,
                    response: Response<NotificationPermissionsResponse>
                ) {
                    if (response.isSuccessful) {
                        notificationsPermission.value = response.body()?.notifsPermission
                    }
                }

                override fun onFailure(call: Call<NotificationPermissionsResponse>, t: Throwable) {
                    notificationsPermission.value = null
                }
            })
    }

    fun updateNotificationsPermissions() {
        if (notificationsPermission.value == null) return
        EntourageApplication.get().apiModule.homeRequest.updateNotificationsPermissions(
            NotificationPermissionsResponse(notificationsPermission.value!!))
            .enqueue(object : Callback<NotificationPermissionsResponse> {
                override fun onResponse(
                    call: Call<NotificationPermissionsResponse>,
                    response: Response<NotificationPermissionsResponse>
                ) {
                    if (response.isSuccessful) {
                        notificationsPermission.value = response.body()?.notifsPermission
                    }
                }

                override fun onFailure(call: Call<NotificationPermissionsResponse>, t: Throwable) {
                    notificationsPermission.value = null
                }
            })
    }
    fun updateNotificationsPermissionsFromValue(notifPermData: InAppNotificationPermission) {
        if (notificationsPermission.value == null) return
        EntourageApplication.get().apiModule.homeRequest.updateNotificationsPermissions(
            NotificationPermissionsResponse(notifPermData))
            .enqueue(object : Callback<NotificationPermissionsResponse> {
                override fun onResponse(
                    call: Call<NotificationPermissionsResponse>,
                    response: Response<NotificationPermissionsResponse>
                ) {
                    if (response.isSuccessful) {
                        notificationsPermission.value = response.body()?.notifsPermission
                    }
                }

                override fun onFailure(call: Call<NotificationPermissionsResponse>, t: Throwable) {
                    notificationsPermission.value = null
                }
            })
    }
}