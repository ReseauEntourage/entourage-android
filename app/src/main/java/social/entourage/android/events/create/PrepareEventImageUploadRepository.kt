package social.entourage.android.events.create

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import social.entourage.android.EntourageApplication

class PrepareEventImageUploadRepository(private var callback: Callback?) :
    Callback<PrepareEventImageUploadRepository.Response> {

    fun prepareUpload() {
        val request = Request("image/jpeg")
        EntourageApplication.get().apiModule.eventsRequest.prepareImageUpload(request).enqueue(this)
    }

    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
        if (response.isSuccessful) {
            response.body()?.let {
                callback?.onPrepareUploadSuccess(it.uploadKey, it.presignedUrl)
                return
            }
        }
        callback?.onRepositoryError()
    }

    override fun onFailure(call: Call<Response>, t: Throwable) {
        callback?.onRepositoryError()
    }

    interface Callback {
        fun onPrepareUploadSuccess(uploadKey: String, presignedUrl: String)
        fun onRepositoryError()
    }

    inner class Request internal constructor(
        @field:SerializedName("content_type")
        private val contentType: String
    )

    inner class Response(
        @field:SerializedName("upload_key") var uploadKey: String,
        @field:SerializedName("presigned_url") val presignedUrl: String
    )
}
