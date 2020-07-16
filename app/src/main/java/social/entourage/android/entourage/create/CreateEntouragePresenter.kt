package social.entourage.android.entourage.create

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.api.EntourageRequest
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.BaseEntourage.Companion.create
import social.entourage.android.api.model.BaseEntourage.EntourageWrapper
import social.entourage.android.api.model.LocationPoint
import social.entourage.android.api.tape.Events.OnEntourageCreated
import social.entourage.android.api.tape.Events.OnEntourageUpdated
import social.entourage.android.tools.BusProvider.instance
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
            groupType: String?, metadata: BaseEntourage.Metadata?, joinRequestTypePublic: Boolean) {
        val entourage = create(groupType, actionGroupType, category, title, description, location)
        entourage.metadata = metadata
        entourage.isRecipientConsentObtained = recipientConsentObtained
        entourage.isJoinRequestPublic = joinRequestTypePublic
        val entourageWrapper = EntourageWrapper()
        entourageWrapper.entourage = entourage
        entourageRequest.createEntourage(entourageWrapper)?.enqueue(object : Callback<EntourageWrapper> {
            override fun onResponse(call: Call<EntourageWrapper>, response: Response<EntourageWrapper>) {
                if (response.isSuccessful) {
                    response.body()?.entourage?.let { receivedEntourage ->
                        receivedEntourage.isNewlyCreated = true
                        fragment?.onEntourageCreated(receivedEntourage)
                        instance.post(OnEntourageCreated(receivedEntourage))
                    } ?: run {
                        fragment?.onEntourageCreationFailed()
                    }
                } else {
                    fragment?.onEntourageCreationFailed()
                }
            }

            override fun onFailure(call: Call<EntourageWrapper>, t: Throwable) {
                fragment?.onEntourageCreationFailed()
            }
        })
    }

    fun editEntourage(entourage: BaseEntourage) {
        val entourageWrapper = EntourageWrapper()
        entourageWrapper.entourage = entourage
        entourageRequest.editEntourage(entourage.uuid, entourageWrapper)?.enqueue(object : Callback<EntourageWrapper> {
            override fun onResponse(call: Call<EntourageWrapper>, response: Response<EntourageWrapper>) {
                if (response.isSuccessful) {
                    response.body()?.entourage?.let { receivedEntourage ->
                        fragment?.onEntourageEdited(receivedEntourage)
                        instance.post(OnEntourageUpdated(receivedEntourage))
                    } ?: run {
                        fragment?.onEntourageEditionFailed()
                    }
                } else {
                    fragment?.onEntourageEditionFailed()
                }
            }

            override fun onFailure(call: Call<EntourageWrapper>, t: Throwable) {
                fragment?.onEntourageEditionFailed()
            }
        })
    }

}