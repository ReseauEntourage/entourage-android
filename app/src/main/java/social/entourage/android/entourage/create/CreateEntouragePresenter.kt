package social.entourage.android.entourage.create

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.api.request.EntourageRequest
import social.entourage.android.api.request.EntourageResponse
import social.entourage.android.api.request.EntourageWrapper
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.BaseEntourage.Companion.create
import social.entourage.android.api.model.LocationPoint
import social.entourage.android.api.tape.Events.OnEntourageCreated
import social.entourage.android.api.tape.Events.OnEntourageUpdated
import social.entourage.android.tools.EntBus
import javax.inject.Inject

/**
 * Created by mihaiionescu on 28/04/16.
 */
class CreateEntouragePresenter @Inject constructor(
        private val fragment: CreateEntourageFragment?,
        private val entourageRequest: EntourageRequest) {

    // ----------------------------------
    // Methods
    // ----------------------------------
    fun createEntourage(
        actionGroupType: String?, category: String?,
        title: String, description: String, location: LocationPoint, recipientConsentObtained: Boolean,
        groupType: String?, metadata: BaseEntourage.Metadata?, isPublic: Boolean,
        portrait_photo_url:String?, landscape_photo_url:String?) {
        val entourage = create(groupType, actionGroupType, category, title, description, location)
        entourage.metadata = metadata
        entourage.metadata?.portrait_url = portrait_photo_url
        entourage.metadata?.landscape_url = landscape_photo_url
        entourage.isRecipientConsentObtained = recipientConsentObtained
        entourage.isPublic = isPublic
        val entourageWrapper = EntourageWrapper(entourage)
        entourageRequest.createEntourage(entourageWrapper).enqueue(object : Callback<EntourageResponse> {
            override fun onResponse(call: Call<EntourageResponse>, response: Response<EntourageResponse>) {
                if (response.isSuccessful) {
                    response.body()?.entourage?.let { receivedEntourage ->
                        receivedEntourage.isNewlyCreated = true
                        fragment?.onEntourageCreated(receivedEntourage)
                        EntBus.post(OnEntourageCreated(receivedEntourage))
                    } ?: run {
                        fragment?.onEntourageCreationFailed()
                    }
                } else {
                    fragment?.onEntourageCreationFailed()
                }
            }

            override fun onFailure(call: Call<EntourageResponse>, t: Throwable) {
                fragment?.onEntourageCreationFailed()
            }
        })
    }

    fun editEntourage(entourage: BaseEntourage) {
        val uuid = entourage.uuid ?: run {
            fragment?.onEntourageEditionFailed()
            return
        }
        val entourageWrapper = EntourageWrapper(entourage)
        entourageRequest.editEntourage(uuid, entourageWrapper).enqueue(object : Callback<EntourageResponse> {
            override fun onResponse(call: Call<EntourageResponse>, response: Response<EntourageResponse>) {
                if (response.isSuccessful) {
                    response.body()?.entourage?.let { receivedEntourage ->
                        fragment?.onEntourageEdited(receivedEntourage)
                        EntBus.post(OnEntourageUpdated(receivedEntourage))
                    } ?: run {
                        fragment?.onEntourageEditionFailed()
                    }
                } else {
                    fragment?.onEntourageEditionFailed()
                }
            }

            override fun onFailure(call: Call<EntourageResponse>, t: Throwable) {
                fragment?.onEntourageEditionFailed()
            }
        })
    }

}