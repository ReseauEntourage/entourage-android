package social.entourage.android.user

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.UserRequest

class PrepareAvatarUploadRepository (private var callback: Callback?) : Callback<PrepareAvatarUploadRepository.Response> {
    private val userRequest: UserRequest
        get() = EntourageApplication.get().components.userRequest

    fun prepareUpload() {
        val request = Request("image/jpeg")
        userRequest.prepareAvatarUpload(request).enqueue(this)
    }

    override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
        if (response.isSuccessful) {
            response.body()?.let {
                callback?.onPrepareUploadSuccess(it.avatarKey, it.presignedUrl)
                return
            }
        }
        callback?.onRepositoryError()
    }

    override fun onFailure(call: Call<Response>, t: Throwable) {
        callback?.onRepositoryError()
    }

    interface Callback {
        fun onPrepareUploadSuccess(avatarKey: String, presignedUrl: String)
        fun onRepositoryError()
    }

    inner class Request internal constructor(private val content_type: String)

    inner class Response(@field:SerializedName("avatar_key") var avatarKey: String, @field:SerializedName("presigned_url") val presignedUrl: String)

}