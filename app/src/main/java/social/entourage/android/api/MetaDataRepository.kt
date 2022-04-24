package social.entourage.android.api

import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.GroupImage
import social.entourage.android.api.model.Tags
import social.entourage.android.api.request.GroupImagesResponse
import social.entourage.android.api.request.MetaDataResponse

object MetaDataRepository {
    var metaData: MutableLiveData<Tags> = MutableLiveData()
    var groupImages: MutableLiveData<List<GroupImage>> = MutableLiveData()


    fun getMetaData() {
        val metaDataRequest = EntourageApplication.get().apiModule.metaDataRequest
        metaDataRequest.getMetaData().enqueue(object : Callback<MetaDataResponse> {
            override fun onResponse(
                call: Call<MetaDataResponse>,
                response: Response<MetaDataResponse>
            ) {
                if (response.isSuccessful) {
                    metaData.value = response.body()?.tags
                }
            }

            override fun onFailure(call: Call<MetaDataResponse>, t: Throwable) {
            }
        })
    }

    fun getGroupImages() {
        val getImages = EntourageApplication.get().apiModule.groupRequest
        getImages.getGroupImages().enqueue(object : Callback<GroupImagesResponse> {
            override fun onResponse(
                call: Call<GroupImagesResponse>,
                response: Response<GroupImagesResponse>
            ) {
                if (response.isSuccessful) {
                    groupImages.value = response.body()?.groupImages
                }
            }

            override fun onFailure(call: Call<GroupImagesResponse>, t: Throwable) {
            }
        })
    }

}