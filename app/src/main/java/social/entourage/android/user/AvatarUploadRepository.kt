package social.entourage.android.user

import okhttp3.*
import java.io.File
import java.io.IOException
import javax.inject.Inject

class AvatarUploadRepository @Inject constructor(private val client: OkHttpClient) : Callback {
    private var callback: Callback? = null
    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun uploadFile(file: File, presignedUrl: String) {
        val mediaType = MediaType.parse("image/jpeg")
        val requestBody = RequestBody.create(mediaType, file)
        val request = Request.Builder()
                .url(presignedUrl)
                .put(requestBody)
                .build()
        client.newCall(request).enqueue(this)
    }

    override fun onFailure(call: Call, e: IOException) {
        callback?.onRepositoryError()
    }

    override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) {
            callback?.onUploadSuccess()
        } else {
            callback?.onRepositoryError()
        }
    }

    interface Callback {
        fun onUploadSuccess()
        fun onRepositoryError()
    }

}