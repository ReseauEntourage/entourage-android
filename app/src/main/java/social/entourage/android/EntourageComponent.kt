package social.entourage.android

import android.app.Application
import dagger.Component
import okhttp3.OkHttpClient
import social.entourage.android.api.ApiModule
import social.entourage.android.api.PhotoGalleryRequest
import social.entourage.android.api.request.*
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.authentication.AuthenticationModule
import social.entourage.android.authentication.ComplexPreferences
import social.entourage.android.service.EntService
import javax.inject.Singleton

/**
 * Dagger component on Application Level
 * Add a get method to provide some object to components that imports this component
 */
@Singleton
@Component(modules = [EntourageApplicationModule::class, ApiModule::class, AuthenticationModule::class])
interface EntourageComponent {
    fun inject(application: Application?)
    fun inject(service: EntService?)
    val authenticationController: AuthenticationController
    val applicationInfoRequest: ApplicationInfoRequest
    val loginRequest: LoginRequest
    val poiRequest: PoiRequest
    val userRequest: UserRequest
    val entourageRequest: EntourageRequest
    val newsfeedRequest: NewsfeedRequest
    val invitationRequest: InvitationRequest
    val partnerRequest: PartnerRequest
    val sharingRequest: SharingRequest
    val okHttpClient: OkHttpClient
    val complexPreferences: ComplexPreferences?
    val photoGalleryRequest: PhotoGalleryRequest
    val conversationsRequest: ConversationsRequest
    val metaDataRequest: MetaDataRequest
}