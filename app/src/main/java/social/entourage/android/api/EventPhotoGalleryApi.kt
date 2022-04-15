package social.entourage.android.api

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import social.entourage.android.EntourageApplication
import java.io.Serializable

/**
 * Created by Jr on 21/05/2021.
 */

class EventPhotoGalleryApi {

    private val photoGalleryService : PhotoGalleryRequest
        get() =  EntourageApplication.get().apiModule.photoGalleryRequest

    fun getPhotoGallery(listener:(photoGallery:List<PhotoGallery>?, error:String?) -> Unit) {
        val call = photoGalleryService.getPhotoGallery()

        call.enqueue(object : Callback<PhotoGalleryResponse> {
            override fun onResponse(call: Call<PhotoGalleryResponse>, response: Response<PhotoGalleryResponse>) {
                if (response.isSuccessful) {
                    listener(response.body()?.photoGallery,null)
                }
                else {
                    if (response.errorBody() != null) {
                        val errorString = response.errorBody()?.string()
                        listener(null,errorString)
                    }
                }
            }

            override fun onFailure(call: Call<PhotoGalleryResponse>, t: Throwable) {
                listener(null,null)
            }
        })
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: EventPhotoGalleryApi? = null

        @Synchronized
        fun getInstance(): EventPhotoGalleryApi {
            return instance ?: EventPhotoGalleryApi().also { instance = it}
        }
    }
}

/*****
 * Model
 */

class PhotoGallery : Serializable {
    @SerializedName("landscape_url")
    var url_image_landscape:String = ""
    @SerializedName("portrait_url")
    var url_image_portrait:String = ""
    @SerializedName("landscape_small_url")
    var url_image_landscape_light:String = ""
}

/*****
 * Interface
 */
interface PhotoGalleryRequest {
    @GET("entourage_images")
    fun getPhotoGallery(): Call<PhotoGalleryResponse>
}

/*****
 * Response
 */
class PhotoGalleryResponse(@field:SerializedName("entourage_images") var photoGallery: List<PhotoGallery>)