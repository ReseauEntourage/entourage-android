package social.entourage.android.api

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import social.entourage.android.BuildConfig
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.BaseEntourage.BaseEntourageJsonAdapter
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.api.model.feed.NewsfeedItem.NewsfeedItemJsonAdapter
import social.entourage.android.api.request.*
import social.entourage.android.authentication.AuthenticationInterceptor
import javax.inject.Singleton

/**
 * Module related to Application
 * Providing API related dependencies
 */
class ApiModule {
    val okHttpClient : OkHttpClient
    val applicationInfoRequest: ApplicationInfoRequest
    val loginRequest: LoginRequest
    val poiRequest: PoiRequest
    val userRequest: UserRequest
    val entourageRequest: EntourageRequest
    val newsfeedRequest: NewsfeedRequest
    val invitationRequest: InvitationRequest
    val partnerRequest: PartnerRequest
    val sharingRequest: SharingRequest
    val photoGalleryRequest: PhotoGalleryRequest
    val conversationsRequest: ConversationsRequest
    val metaDataRequest: MetaDataRequest

    init {
        okHttpClient = providesOkHttpClient()
        val restAdapter = providesRestAdapter(okHttpClient)
        applicationInfoRequest = providesApplicationInfoRequest(restAdapter)
        loginRequest = providesLoginRequest(restAdapter)
        poiRequest= providesPoiRequest(restAdapter)
        userRequest= providesUserRequest(restAdapter)
        entourageRequest= providesEntourageRequest(restAdapter)
        newsfeedRequest= providesNewsfeedRequest(restAdapter)
        invitationRequest= providesInvitationRequest(restAdapter)
        partnerRequest= providesPartnerRequest(restAdapter)
        sharingRequest= providesSharingRequest(restAdapter)
        photoGalleryRequest= providesPhotoGalleryRequest(restAdapter)
        conversationsRequest= providesConversationsRequest(restAdapter)
        metaDataRequest = providesMetaDataRequest(restAdapter)
    }

    fun providesOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(AuthenticationInterceptor)
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(loggingInterceptor)
        }
        return builder.build()
    }

    fun providesRestAdapter(client: OkHttpClient): Retrofit {
        val gson = GsonBuilder()
            .addSerializationExclusionStrategy(object : ExclusionStrategy {
                override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
                    val expose = fieldAttributes.getAnnotation(Expose::class.java) ?: return false
                    return !expose.serialize
                }

                override fun shouldSkipClass(aClass: Class<*>?): Boolean {
                    return false
                }
            }).addDeserializationExclusionStrategy(object : ExclusionStrategy {
                override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
                    val expose = fieldAttributes.getAnnotation(Expose::class.java) ?: return false
                    return !expose.deserialize
                }

                override fun shouldSkipClass(aClass: Class<*>?): Boolean {
                    return false
                }
            })
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(NewsfeedItem::class.java, NewsfeedItemJsonAdapter())
            .registerTypeAdapter(BaseEntourage::class.java, BaseEntourageJsonAdapter())
            .create()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.ENTOURAGE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun providesLoginRequest(restAdapter: Retrofit): LoginRequest {
        return restAdapter.create(LoginRequest::class.java)
    }

    fun providesApplicationInfoRequest(restAdapter: Retrofit): ApplicationInfoRequest {
        return restAdapter.create(ApplicationInfoRequest::class.java)
    }

    fun providesUserRequest(restAdapter: Retrofit): UserRequest {
        return restAdapter.create(UserRequest::class.java)
    }

    fun providesEntourageRequest(restAdapter: Retrofit): EntourageRequest {
        return restAdapter.create(EntourageRequest::class.java)
    }

    fun providesNewsfeedRequest(restAdapter: Retrofit): NewsfeedRequest {
        return restAdapter.create(NewsfeedRequest::class.java)
    }

    fun providesPoiRequest(restAdapter: Retrofit): PoiRequest {
        return restAdapter.create(PoiRequest::class.java)
    }

    fun providesInvitationRequest(restAdapter: Retrofit): InvitationRequest {
        return restAdapter.create(InvitationRequest::class.java)
    }

    fun providesPartnerRequest(restAdapter: Retrofit): PartnerRequest {
        return restAdapter.create(PartnerRequest::class.java)
    }

    fun providesSharingRequest(restAdapter: Retrofit): SharingRequest {
        return restAdapter.create(SharingRequest::class.java)
    }

    fun providesPhotoGalleryRequest(restAdapter: Retrofit): PhotoGalleryRequest {
        return restAdapter.create(PhotoGalleryRequest::class.java)
    }

    fun providesConversationsRequest(restAdapter: Retrofit): ConversationsRequest {
        return restAdapter.create(ConversationsRequest::class.java)
    }

    fun providesMetaDataRequest(restAdapter: Retrofit): MetaDataRequest {
        return restAdapter.create(MetaDataRequest::class.java)
    }
}