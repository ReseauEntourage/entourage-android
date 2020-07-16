package social.entourage.android.user

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import social.entourage.android.api.UserRequest
import javax.inject.Inject

class PrepareAvatarUploadRepository @Inject constructor(private val userRequest: UserRequest) : Callback<PrepareAvatarUploadRepository.Response> {
    private var callback: Callback? = null
    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

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