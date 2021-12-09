package social.entourage.android.home.expert

import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.request.EntourageRequest
import social.entourage.android.api.request.EntourageResponse
import social.entourage.android.api.request.TourRequest
import social.entourage.android.api.request.TourResponse
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.entourage.EntourageDisclaimerFragment
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.create.BaseCreateEntourageFragment
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.onboarding.InputNamesFragment
import timber.log.Timber
import javax.inject.Inject

/**
 * Presenter controlling the HomeExpertFragment
 *
 * @see HomeExpertFragment
 */
class HomeExpertPresenter @Inject constructor(
    private val fragment: HomeExpertFragment?,
    internal val authenticationController: AuthenticationController,
    private val entourageRequest: EntourageRequest,
    private val tourRequest: TourRequest) {

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun openFeedItem(feedItem: FeedItem, invitationId: Long, feedRank: Int) {
        try {
            val fragmentManager = fragment?.activity?.supportFragmentManager ?: return
            FeedItemInformationFragment.newInstance(feedItem, invitationId, feedRank,false).show(fragmentManager, FeedItemInformationFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    fun openFeedItemFromUUID(feedItemUUID: String, feedItemType: Int, invitationId: Long) {
        if(feedItemUUID.isBlank()) return
        when (feedItemType) {
            TimestampedObject.ENTOURAGE_CARD -> {
                val call = entourageRequest.retrieveEntourageById(feedItemUUID, 0, 0)
                call.enqueue(object : Callback<EntourageResponse> {
                    override fun onResponse(call: Call<EntourageResponse>, response: Response<EntourageResponse>) {
                        response.body()?.entourage?.let {
                            if (response.isSuccessful) {
                                openFeedItem(it, invitationId, 0)
                            }
                        }
                    }
                    override fun onFailure(call: Call<EntourageResponse>, t: Throwable) {
                    }
                })
            }
            TimestampedObject.TOUR_CARD -> {
                val call = tourRequest.retrieveTourById(feedItemUUID)
                call.enqueue(object : Callback<TourResponse> {
                    override fun onResponse(call: Call<TourResponse>, response: Response<TourResponse>) {
                        response.body()?.tour?.let {
                            if (response.isSuccessful) {
                                openFeedItem(it, invitationId, 0)
                            }
                        }
                    }
                    override fun onFailure(call: Call<TourResponse>, t: Throwable) {
                    }
                })
            }
        }
    }

    fun openFeedItemFromShareURL(feedItemShareURL: String, feedItemType: Int) {
        when (feedItemType) {
            TimestampedObject.ENTOURAGE_CARD -> {
                val call = entourageRequest.retrieveEntourageByShareURL(feedItemShareURL)
                call.enqueue(object : Callback<EntourageResponse> {
                    override fun onResponse(call: Call<EntourageResponse>, response: Response<EntourageResponse>) {
                        response.body()?.entourage?.let {
                            if (response.isSuccessful) {
                                openFeedItem(it,0,0)
                            }
                        }
                    }

                    override fun onFailure(call: Call<EntourageResponse>, t: Throwable) {
                    }
                })
            }
        }
    }

    fun createEntourage(location: LatLng?, groupType: String, category: EntourageCategory?) {
        if (fragment != null && !fragment.isStateSaved) {
            val fragmentManager = fragment.activity?.supportFragmentManager ?: return
            BaseCreateEntourageFragment.newExpertInstance(location, groupType, category).show(fragmentManager, BaseCreateEntourageFragment.TAG)
        }
    }

    fun displayEntourageDisclaimer(groupType: String) {
        if (fragment != null && !fragment.isStateSaved) {
            val fragmentManager = fragment.activity?.supportFragmentManager ?:return
            EntourageDisclaimerFragment.newInstance(groupType).show(fragmentManager, EntourageDisclaimerFragment.TAG)
        }
    }

    fun checkUserNamesInfo() {
        authenticationController.me?.let { user ->
            if (user.firstName.isNullOrEmpty() && user.lastName.isNullOrEmpty()) {
                fragment?.let { InputNamesFragment().show(it.parentFragmentManager,"InputFGTag") }
            }
        }
    }

    fun saveInfo(isNav:Boolean, type:String?) {
        val editor = EntourageApplication.get().sharedPreferences.edit()
        editor.putBoolean("isNavNews",isNav)
        editor.putString("navType",type)
        editor.apply()
    }

    fun storeActionZoneInfo(ignoreAddress: Boolean) {
        authenticationController.isIgnoringActionZone = ignoreAddress
        authenticationController.saveUserPreferences()
    }

}