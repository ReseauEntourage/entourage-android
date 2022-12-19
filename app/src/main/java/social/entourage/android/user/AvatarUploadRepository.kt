package social.entourage.android.user

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import social.entourage.android.EntourageApplication
import java.io.File
import java.io.IOException

class AvatarUploadRepository : Callback {
    private val client: OkHttpClient
        get() = EntourageApplication.get().apiModule.okHttpClient
    private var callback: Callback? = null
    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun uploadFile(file: File, presignedUrl: String) {
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
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