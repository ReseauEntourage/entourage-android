package social.entourage.android.api

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.api.model.Image
import social.entourage.android.api.model.Tags
import social.entourage.android.api.request.EventsImagesResponse
import social.entourage.android.api.request.GroupImagesResponse
import social.entourage.android.api.request.MetaDataResponse

object MetaDataRepository {
    var metaData: MutableLiveData<Tags> = MutableLiveData()
    var groupImages: MutableLiveData<List<Image>> = MutableLiveData()
    var eventsImages: MutableLiveData<List<Image>> = MutableLiveData()

    fun getMetaData() {
        val metaDataRequest = EntourageApplication.get().apiModule.metaDataRequest
        metaDataRequest.getMetaData().enqueue(object : Callback<MetaDataResponse> {
            override fun onResponse(
                call: Call<MetaDataResponse>,
                response: Response<MetaDataResponse>
            ) {
                if (response.isSuccessful) {
                    metaData.value = response.body()?.tags
                    MainActivity.reactionsList = response.body()?.reactions
                    MainActivity.interest = response.body()?.interests
                    MainActivity.concerns = response.body()?.concerns
                    MainActivity.involvements = response.body()?.involvements

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

    fun getEventsImages() {
        val getImages = EntourageApplication.get().apiModule.eventsRequest
        getImages.getEventsImages().enqueue(object : Callback<EventsImagesResponse> {
            override fun onResponse(
                call: Call<EventsImagesResponse>,
                response: Response<EventsImagesResponse>
            ) {
                if (response.isSuccessful) {
                    eventsImages.value = response.body()?.eventImages
                }
            }

            override fun onFailure(call: Call<EventsImagesResponse>, t: Throwable) {
            }
        })
    }

    // Sections
    fun getActionSectionNameFromId(id:String?) : String {
       val ret =  metaData.value?.sections?.firstOrNull { it.id == id }
        ret?.name?.let { return it } ?: kotlin.run {
          return  "-"
        }
    }

}