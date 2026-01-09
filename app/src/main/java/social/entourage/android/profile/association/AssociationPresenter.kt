package social.entourage.android.profile.association

import androidx.collection.ArrayMap
import androidx.lifecycle.MutableLiveData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Partner
import social.entourage.android.api.model.PartnerResponse
import social.entourage.android.api.model.PartnerUpdateData
import social.entourage.android.api.model.PartnerUpdateWrapper
import social.entourage.android.api.model.PresignedUploadBody
import social.entourage.android.api.model.PresignedUrlResponse
import social.entourage.android.api.request.PresignedUrlWrapper
import java.util.concurrent.Executors

class AssociationPresenter {
    var getPartnerSuccess = MutableLiveData<Boolean>()
    var followSuccess = MutableLiveData<Boolean>()
    var partner = MutableLiveData<Partner>()

    var updatePartnerSuccess = MutableLiveData<Boolean>()
    var presignedUrl = MutableLiveData<PresignedUrlResponse?>()

    private val http = OkHttpClient()
    private val executor = Executors.newSingleThreadExecutor()

    fun getPartnerInfos(partnerId: Int) {
        EntourageApplication.get().apiModule.userRequest
            .getPartnerDetail(partnerId)
            .enqueue(object : Callback<PartnerResponse> {
                override fun onResponse(call: Call<PartnerResponse>, response: Response<PartnerResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { partner.value = it.partner }
                        getPartnerSuccess.value = true
                    } else {
                        getPartnerSuccess.value = false
                    }
                }

                override fun onFailure(call: Call<PartnerResponse>, t: Throwable) {
                    getPartnerSuccess.value = false
                }
            })
    }

    fun updatePartnerFollow(isFollow: Boolean, partnerId: Long) {
        val params = ArrayMap<String, Any>()
        val isFollowParam = ArrayMap<String, Any>()
        isFollowParam["partner_id"] = partnerId.toString()
        isFollowParam["active"] = isFollow.toString()
        params["following"] = isFollowParam

        EntourageApplication.get().apiModule.userRequest.updateUserPartner(params)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        partner.let {
                            it.value?.isFollowing = isFollow
                            followSuccess.value = true
                        }
                    } else {
                        followSuccess.value = false
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    followSuccess.value = true
                }
            })
    }

    fun updatePartner(partnerId: Int, data: PartnerUpdateData) {
        val requestBody = PartnerUpdateWrapper(data)
        EntourageApplication.get().apiModule.associationsRequest
            .updateAssociation(partnerId, requestBody)
            .enqueue(object : Callback<PartnerResponse> {
                override fun onResponse(call: Call<PartnerResponse>, response: Response<PartnerResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { partner.value = it.partner }
                        updatePartnerSuccess.value = true
                    } else {
                        updatePartnerSuccess.value = false
                    }
                }

                override fun onFailure(call: Call<PartnerResponse>, t: Throwable) {
                    updatePartnerSuccess.value = false
                }
            })
    }

    fun getPresignedUploadUrl(contentType: String) {
        val body = PresignedUploadBody(contentType)

        EntourageApplication.get().apiModule.associationsRequest
            .getPresignedUploadUrl(body)
            .enqueue(object : Callback<PresignedUrlResponse> {
                override fun onResponse(
                    call: Call<PresignedUrlResponse>,
                    response: Response<PresignedUrlResponse>
                ) {
                    if (response.isSuccessful) {
                        presignedUrl.value = response.body()
                    } else {
                        presignedUrl.value = null
                    }
                }

                override fun onFailure(call: Call<PresignedUrlResponse>, t: Throwable) {
                    presignedUrl.value = null
                }
            })
    }


    fun uploadToPresignedUrl(
        uploadUrl: String,
        contentType: String,
        bytes: ByteArray,
        onDone: (Boolean) -> Unit
    ) {
        executor.execute {
            val ok = try {
                val body = RequestBody.create(contentType.toMediaTypeOrNull(), bytes)
                val req = Request.Builder()
                    .url(uploadUrl)
                    .put(body)
                    .addHeader("Content-Type", contentType)
                    .build()
                http.newCall(req).execute().use { resp -> resp.isSuccessful }
            } catch (_: Throwable) {
                false
            }
            onDone(ok)
        }
    }

    fun newPartnerUpdateData(): PartnerUpdateData {
        return PartnerUpdateData()
    }
}
