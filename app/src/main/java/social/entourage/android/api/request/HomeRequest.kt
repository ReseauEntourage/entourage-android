package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.notification.InAppNotification
import social.entourage.android.api.model.notification.InAppNotificationPermission
import social.entourage.android.api.model.Pedago
import social.entourage.android.api.model.Summary
import social.entourage.android.api.model.*

class SummaryResponse(@field:SerializedName("user") val summary: Summary)
class PedagogicResponse(@field:SerializedName("resources") val pedago: MutableList<Pedago>)
class PedagogicSingleResponse(@field:SerializedName("resource") val pedago: Pedago)

class NotificationsCountResponse(@field:SerializedName("count") val count: Int)
class NotificationPermissionsResponse(@field:SerializedName("notification_permissions") val notifsPermission: InAppNotificationPermission)
class NotificationsInAppResponse(@field:SerializedName("inapp_notifications") val notifs: MutableList<InAppNotification>)
class NotificationInAppResponse(@field:SerializedName("inapp_notification") val notif: InAppNotification)

interface HomeRequest {
    @GET("home/summary")
    fun getSummary(): Call<SummaryResponse>

    @GET("resources")
    fun getPedagogicalResources(@Query("noHtml") noHtml:Boolean): Call<PedagogicResponse>

    @GET("resources/{id}")
    fun getPedagogicalResource(@Path("id") resourceId: Int): Call<PedagogicSingleResponse>

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