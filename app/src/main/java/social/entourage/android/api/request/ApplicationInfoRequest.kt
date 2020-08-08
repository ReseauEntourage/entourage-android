package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PUT
import social.entourage.android.api.model.ApplicationInfo

class ApplicationWrapper(@field:SerializedName("application") var applicationInfo: ApplicationInfo) {
    fun setNotificationStatus(notifStatus: String) {
        applicationInfo.notificationsPermissions = notifStatus
    }
}

interface ApplicationInfoRequest {
    @GET("check.json")
    fun checkForUpdate(): Call<ResponseBody>

    @PUT("applications.json")
    fun updateApplicationInfo(
            @Body applicationWrapper: ApplicationWrapper
    ): Call<ResponseBody>

    @HTTP(method = "DELETE", path = "applications.json", hasBody = true)
    fun deleteApplicationInfo(
            @Body applicationWrapper: ApplicationWrapper
    ): Call<ResponseBody>
}