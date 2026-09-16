package social.entourage.android.api

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import timber.log.Timber
import retrofit2.converter.gson.GsonConverterFactory
import social.entourage.android.BuildConfig
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.BaseEntourage.BaseEntourageJsonAdapter
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.api.model.feed.NewsfeedItem.NewsfeedItemJsonAdapter
import social.entourage.android.api.request.ActionsRequest
import social.entourage.android.api.request.AppLinksRequest
import social.entourage.android.api.request.ApplicationInfoRequest
import social.entourage.android.api.request.AssociationsRequest
import social.entourage.android.api.request.DiscussionsRequest
import social.entourage.android.api.request.EntourageRequest
import social.entourage.android.api.request.EventsRequest
import social.entourage.android.api.request.GroupRequest
import social.entourage.android.api.request.HomeRequest
import social.entourage.android.api.request.LoginRequest
import social.entourage.android.api.request.MetaDataRequest
import social.entourage.android.api.request.PartnerRequest
import social.entourage.android.api.request.PoiRequest
import social.entourage.android.api.request.SharingRequest
import social.entourage.android.api.request.SmallTalkRequest
import social.entourage.android.api.request.SuggestionRequest
import social.entourage.android.api.request.SurveyRequest
import social.entourage.android.api.request.UserRequest
import social.entourage.android.authentication.AuthenticationInterceptor
import social.entourage.android.tools.utils.Const
import java.util.concurrent.TimeUnit

class ApiModule {
    val okHttpClient: OkHttpClient
    val retrofit: Retrofit

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
    val actionsRequest: ActionsRequest
    val surveyRequest: SurveyRequest
    val discussionsRequest: DiscussionsRequest
    val appLinksRequest: AppLinksRequest

    val associationsRequest: AssociationsRequest
    val suggestionRequest: SuggestionRequest

    init {
        okHttpClient = providesOkHttpClient()
        retrofit = providesRestAdapter(okHttpClient)

        applicationInfoRequest = providesApplicationInfoRequest(retrofit)
        loginRequest = providesLoginRequest(retrofit)
        poiRequest = providesPoiRequest(retrofit)
        userRequest = providesUserRequest(retrofit)
        entourageRequest = providesEntourageRequest(retrofit)
        partnerRequest = providesPartnerRequest(retrofit)
        sharingRequest = providesSharingRequest(retrofit)
        metaDataRequest = providesMetaDataRequest(retrofit)
        groupRequest = providesGroupRequest(retrofit)
        homeRequest = providesSummaryRequest(retrofit)
        smallTalkRequest = providesSmallTalkRequest(retrofit)
        eventsRequest = providesEventsRequest(retrofit)
        actionsRequest = providesActionsRequest(retrofit)
        surveyRequest = providesSurveyRequest(retrofit)
        discussionsRequest = providesDiscussionsRequest(retrofit)
        appLinksRequest = providesAppLinksRequest(retrofit)

        associationsRequest = providesAssociationsRequest(retrofit)
        suggestionRequest = providesSuggestionRequest(retrofit)
    }

    fun providesOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(AuthenticationInterceptor)
            .addInterceptor(ApiErrorInterceptor)
            .addInterceptor(HmacInterceptor())
            .readTimeout(Const.READ_CONNECT_WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(Const.READ_CONNECT_WRITE_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Const.READ_CONNECT_WRITE_TIMEOUT, TimeUnit.SECONDS)

        builder.addInterceptor(CurlLoggingInterceptor())

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                val maxLogLength = 4000
                if (message.length > maxLogLength) {
                    Timber.tag("OkHttp").d("%s... [TRUNCATED]", message.substring(0, maxLogLength))
                } else {
                    Timber.tag("OkHttp").d(message)
                }
            }
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

    fun providesAssociationsRequest(restAdapter: Retrofit): AssociationsRequest {
        return restAdapter.create(AssociationsRequest::class.java)
    }

    fun providesSuggestionRequest(restAdapter: Retrofit): SuggestionRequest {
        return restAdapter.create(SuggestionRequest::class.java)
    }
}
