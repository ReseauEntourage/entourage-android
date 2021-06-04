package social.entourage.android.api

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.*
import java.io.Serializable


class TourAreaApi {

    private val tourAreaService : TourAreaRequest
        get() =  EntourageApplication.get().components.tourAreaRequest

    /**********************
     * Create user
     */

    fun getTourAreas(listener:(tourAreas:List<HomeTourArea>?, error:String?) -> Unit) {
        val call = tourAreaService.getTourAreas()

        call.enqueue(object : Callback<TourAreasResponse> {
            override fun onResponse(call: Call<TourAreasResponse>, response: Response<TourAreasResponse>) {
                if (response.isSuccessful) {
                    listener(response.body()?.tourAreas,null)
                }
                else {
                    if (response.errorBody() != null) {
                        val errorString = response.errorBody()?.string()
                        listener(null,errorString)
                    }
                }
            }

            override fun onFailure(call: Call<TourAreasResponse>, t: Throwable) {
               listener(null,null)
            }
        })
    }

    fun sendTourAreaRequest(tourAreaId:Int,listener:(isOk:Boolean, error:String?) -> Unit) {
        val call = tourAreaService.sendTourAreaRequest(tourAreaId)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    listener(true,null)
                }
                else {
                    if (response.errorBody() != null) {
                        val errorString = response.errorBody()?.string()
                        listener(false,errorString)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                listener(false,null)
            }
        })
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: TourAreaApi? = null

        @Synchronized
        fun getInstance(): TourAreaApi {
            return instance ?: TourAreaApi().also { instance = it}
        }
    }
}

class HomeTourArea : Serializable {
    @SerializedName("id")
    var areaId:Int = 0
    @SerializedName("area")
    var areaName:String = ""
    @SerializedName("departement")
    var postalCode:String = ""
    @SerializedName("status")
    var status:String = ""

    fun isActive() : Boolean {
        return status.equals("active")
    }
}