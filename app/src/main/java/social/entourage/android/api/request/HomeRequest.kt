package social.entourage.android.api.request

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.notification.InAppNotification
import social.entourage.android.api.model.notification.InAppNotificationPermission
import social.entourage.android.api.model.Pedago
import social.entourage.android.api.model.Summary
import social.entourage.android.api.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
class SummaryResponse(@SerialName("user") val summary: Summary)
@KSerializable
class PedagogicResponse(@SerialName("resources") val pedago: MutableList<Pedago> = mutableListOf())
@KSerializable
class PedagogicSingleResponse(@SerialName("resource") val pedago: Pedago)

@KSerializable
class NotificationsCountResponse(@SerialName("count") val count: Int = 0)
@KSerializable
class NotificationPermissionsResponse(@SerialName("notification_permissions") val notifsPermission: InAppNotificationPermission)
@KSerializable
class NotificationsInAppResponse(@SerialName("inapp_notifications") val notifs: MutableList<InAppNotification> = mutableListOf())
@KSerializable
class NotificationInAppResponse(@SerialName("inapp_notification") val notif: InAppNotification)

interface HomeRequest {
    @GET("home/summary")
    fun getSummary(): Call<SummaryResponse>

    @GET("resources")
    fun getPedagogicalResources(@Query("noHtml") noHtml:Boolean): Call<PedagogicResponse>

    @GET("resources/home")
    fun getInitialPedagogicalResources(@Query("noHtml") noHtml:Boolean): Call<PedagogicResponse>

    @GET("resources/{id}")
    fun getPedagogicalResource(@Path("id") resourceId: Int): Call<PedagogicSingleResponse>
    @GET("resources/{id}")
    fun getPedagogicalResourceWithHash(@Path("id") resourceId: String): Call<PedagogicSingleResponse>

    @GET("resources/welcome")
    fun getWelcomeResource(): Call<PedagogicSingleResponse>

    @POST("resources/{id}/users")
    fun setPedagogicalContentAsRead(@Path("id") groupId: Int): Call<Boolean>

    @GET("webviews/url")
    fun markRecoWebUrlRead(@Query("url") url:String): Call<ResponseBody>

    //Notifs in app
    @GET("inapp_notifications/count")
    fun getNotificationsCount(): Call<NotificationsCountResponse>

    @GET("inapp_notifications")
    fun getNotifications( @Query("page") page: Int,
                          @Query("per") per: Int) : Call<NotificationsInAppResponse>

    @DELETE("inapp_notifications/{id}")
    fun markReadNotif(@Path("id") notifId: Int) : Call<NotificationInAppResponse>

    //Notifs permissions
    @GET("notification_permissions")
    fun getNotificationsPermissions(): Call<NotificationPermissionsResponse>

    @POST("notification_permissions")
    fun updateNotificationsPermissions(@Body perms:NotificationPermissionsResponse): Call<NotificationPermissionsResponse>

}