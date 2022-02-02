package social.entourage.android.api

import android.app.Application
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.squareup.tape.FileObjectQueue
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import social.entourage.android.BuildConfig
import social.entourage.android.Constants
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.BaseEntourage.BaseEntourageJsonAdapter
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.api.model.feed.NewsfeedItem.NewsfeedItemJsonAdapter
import social.entourage.android.api.request.*
import social.entourage.android.api.tape.EncounterTapeTaskQueue
import social.entourage.android.authentication.AuthenticationInterceptor
import social.entourage.android.tools.GsonConverter
import social.entourage.android.tour.encounter.CreateEncounterPresenter.EncounterUploadTask
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Singleton

/**
 * Module related to Application
 * Providing API related dependencies
 */
@Module
class ApiModule {
    @Provides
    @Singleton
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

    @Provides
    @Singleton
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

    @Provides
    @Singleton
    fun providesLoginService(restAdapter: Retrofit): LoginRequest {
        return restAdapter.create(LoginRequest::class.java)
    }

    @Provides
    @Singleton
    fun providesApplicationInfoRequest(restAdapter: Retrofit): ApplicationInfoRequest {
        return restAdapter.create(ApplicationInfoRequest::class.java)
    }

    @Provides
    @Singleton
    fun providesEncounterService(restAdapter: Retrofit): EncounterRequest {
        return restAdapter.create(EncounterRequest::class.java)
    }

    @Provides
    @Singleton
    fun providesTourRequest(restAdapter: Retrofit): TourRequest {
        return restAdapter.create(TourRequest::class.java)
    }

    @Provides
    @Singleton
    fun providesUserRequest(restAdapter: Retrofit): UserRequest {
        return restAdapter.create(UserRequest::class.java)
    }

    @Provides
    @Singleton
    fun providesEntourageRequest(restAdapter: Retrofit): EntourageRequest {
        return restAdapter.create(EntourageRequest::class.java)
    }

    @Provides
    @Singleton
    fun providesNewsfeedRequest(restAdapter: Retrofit): NewsfeedRequest {
        return restAdapter.create(NewsfeedRequest::class.java)
    }

    @Provides
    @Singleton
    fun providesMapService(restAdapter: Retrofit): PoiRequest {
        return restAdapter.create(PoiRequest::class.java)
    }

    @Provides
    @Singleton
    fun providesInvitationRequest(restAdapter: Retrofit): InvitationRequest {
        return restAdapter.create(InvitationRequest::class.java)
    }

    @Provides
    @Singleton
    fun providesPartnerRequest(restAdapter: Retrofit): PartnerRequest {
        return restAdapter.create(PartnerRequest::class.java)
    }

    @Provides
    @Singleton
    fun providesEncounterTapeTaskQueue(application: Application): EncounterTapeTaskQueue {
        val gson = GsonBuilder().create()
        val converter: FileObjectQueue.Converter<EncounterUploadTask> = GsonConverter(gson, EncounterUploadTask::class.java)
        val queueFile = File(application.applicationContext.filesDir, Constants.FILENAME_TAPE_QUEUE)
        var delegate: FileObjectQueue<EncounterUploadTask>? = null
        try {
            delegate = FileObjectQueue(queueFile, converter)
        } catch (e: IOException) {
            Timber.e(e)
        }
        return EncounterTapeTaskQueue(delegate, application.applicationContext)
    }

    @Provides
    @Singleton
    fun providesSharingEntourageRequest(restAdapter: Retrofit): SharingRequest {
        return restAdapter.create(SharingRequest::class.java)
    }

    @Provides
    @Singleton
    fun providesTourAreaService(restAdapter: Retrofit): TourAreaRequest {
        return restAdapter.create(TourAreaRequest::class.java)
    }

    @Provides
    @Singleton
    fun providesPhotoGalleryService(restAdapter: Retrofit): PhotoGalleryRequest {
        return restAdapter.create(PhotoGalleryRequest::class.java)
    }

    @Provides
    @Singleton
    fun providesConversationsRequest(restAdapter: Retrofit): ConversationsRequest {
        return restAdapter.create(ConversationsRequest::class.java)
    }
}