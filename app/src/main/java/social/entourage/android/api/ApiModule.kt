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
import social.entourage.android.api.model.SmallTalk
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.api.model.feed.NewsfeedItem.NewsfeedItemJsonAdapter
import social.entourage.android.api.request.*
import social.entourage.android.authentication.AuthenticationInterceptor
import social.entourage.android.tools.utils.Const
import java.util.concurrent.TimeUnit

/**
 * Module related to Application
 * Providing API related dependencies
 */
class ApiModule {
    val okHttpClient: OkHttpClient
    val applicationInfoRequest: ApplicationInfoRequest
    val loginRequest: LoginRequest
    val poiRequest: PoiRequest
    val userRequest: UserRequest
    val entourageRequest: EntourageRequest
    val partnerRequest: PartnerRequest
    val sharingRequest: SharingRequest
    val metaDataRequest: MetaDataRequest
    val groupRequest: GroupRequest
    val homeRequest: HomeRequest
    val smallTalkRequest: SmallTalkRequest
    val eventsRequest: EventsRequest
    val actionsRequest : ActionsRequest
    val surveyRequest : SurveyRequest
    val discussionsRequest : DiscussionsRequest
    val appLinksRequest : AppLinksRequest

    init {
        okHttpClient = providesOkHttpClient()
        val restAdapter = providesRestAdapter(okHttpClient)
        applicationInfoRequest = providesApplicationInfoRequest(restAdapter)
        loginRequest = providesLoginRequest(restAdapter)
        poiRequest = providesPoiRequest(restAdapter)
        userRequest = providesUserRequest(restAdapter)
        entourageRequest = providesEntourageRequest(restAdapter)
        partnerRequest = providesPartnerRequest(restAdapter)
        sharingRequest = providesSharingRequest(restAdapter)
        metaDataRequest = providesMetaDataRequest(restAdapter)
        groupRequest = providesGroupRequest(restAdapter)
        homeRequest = providesSummaryRequest(restAdapter)
        smallTalkRequest = providesSmallTalkRequest(restAdapter)
        eventsRequest = providesEventsRequest(restAdapter)
        actionsRequest = providesActionsRequest(restAdapter)
        surveyRequest = providesSurveyRequest(restAdapter)
        discussionsRequest = providesDiscussionsRequest(restAdapter)
        appLinksRequest = providesAppLinksRequest(restAdapter)
    }

    fun providesOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(AuthenticationInterceptor)
            .readTimeout(Const.READ_CONNECT_WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(Const.READ_CONNECT_WRITE_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Const.READ_CONNECT_WRITE_TIMEOUT, TimeUnit.SECONDS)

        builder.addInterceptor(CurlLoggingInterceptor())

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

    fun providesPoiRequest(restAdapter: Retrofit): PoiRequest {
        return restAdapter.create(PoiRequest::class.java)
    }

    fun providesPartnerRequest(restAdapter: Retrofit): PartnerRequest {
        return restAdapter.create(PartnerRequest::class.java)
    }

    fun providesSharingRequest(restAdapter: Retrofit): SharingRequest {
        return restAdapter.create(SharingRequest::class.java)
    }

    fun providesMetaDataRequest(restAdapter: Retrofit): MetaDataRequest {
        return restAdapter.create(MetaDataRequest::class.java)
    }

    fun providesGroupRequest(restAdapter: Retrofit): GroupRequest {
        return restAdapter.create(GroupRequest::class.java)
    }

    fun providesSummaryRequest(restAdapter: Retrofit): HomeRequest {
        return restAdapter.create(HomeRequest::class.java)
    }
    fun providesSmallTalkRequest(restAdapter: Retrofit): SmallTalkRequest {
        return restAdapter.create(SmallTalkRequest::class.java)
    }

    fun providesEventsRequest(restAdapter: Retrofit): EventsRequest {
        return restAdapter.create(EventsRequest::class.java)
    }
    fun providesActionsRequest(restAdapter: Retrofit): ActionsRequest {
        return restAdapter.create(ActionsRequest::class.java)
    }
    fun providesSurveyRequest(restAdapter: Retrofit): SurveyRequest {
        return restAdapter.create(SurveyRequest::class.java)
    }
    fun providesDiscussionsRequest(restAdapter: Retrofit): DiscussionsRequest {
        return restAdapter.create(DiscussionsRequest::class.java)
    }
    fun providesAppLinksRequest(restAdapter: Retrofit): AppLinksRequest {
        return restAdapter.create(AppLinksRequest::class.java)
    }

}