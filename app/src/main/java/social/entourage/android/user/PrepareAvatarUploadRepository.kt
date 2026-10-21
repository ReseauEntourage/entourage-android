package social.entourage.android.user

import retrofit2.Call
import retrofit2.Callback
import social.entourage.android.EntourageApplication
import social.entourage.android.api.request.UserRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

class PrepareAvatarUploadRepository (private var callback: Callback?) :
    Callback<PrepareAvatarUploadRepository.Response> {
    private val userRequest: UserRequest
        get() = EntourageApplication.get().apiModule.userRequest

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

    @KSerializable
    class Request internal constructor(@SerialName("content_type") val contentType: String)

    @KSerializable
    class Response(
        @SerialName("avatar_key") var avatarKey: String = "",
        @SerialName("presigned_url") val presignedUrl: String = ""
    )

}