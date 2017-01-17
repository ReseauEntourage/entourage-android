package social.entourage.android.api;

import android.app.Application;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.squareup.tape.FileObjectQueue;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import social.entourage.android.BuildConfig;
import social.entourage.android.Constants;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.tape.EncounterTapeTaskQueue;
import social.entourage.android.authentication.AuthenticationInterceptor;
import social.entourage.android.map.encounter.CreateEncounterPresenter;

/**
 * Module related to Application
 * Providing API related dependencies
 */
@Module
public class ApiModule {
    @Inject AuthenticationInterceptor interceptor;

    @Provides
    @Singleton
    public OkHttpClient providesOkHttpClient(final AuthenticationInterceptor interceptor) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(interceptor);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        return builder.build();
    }

    @Provides
    @Singleton
    public Retrofit providesRestAdapter(final OkHttpClient client) {
        Gson gson = new GsonBuilder()
            .addSerializationExclusionStrategy(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                    final Expose expose = fieldAttributes.getAnnotation(Expose.class);
                    return expose != null && !expose.serialize();
                }

                @Override
                public boolean shouldSkipClass(Class<?> aClass) {
                    return false;
                }
            }).addDeserializationExclusionStrategy(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                    final Expose expose = fieldAttributes.getAnnotation(Expose.class);
                    return expose != null && !expose.deserialize();
                }

                @Override
                public boolean shouldSkipClass(Class<?> aClass) {
                    return false;
                }
            })
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(Newsfeed.class, new Newsfeed.NewsfeedJsonAdapter())
            .create();

        return new Retrofit.Builder()
            .baseUrl(BuildConfig.ENTOURAGE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    }

    @Provides
    @Singleton
    public LoginRequest providesLoginService(final Retrofit restAdapter) {
        return restAdapter.create(LoginRequest.class);
    }

    @Provides
    @Singleton
    public AppRequest providesAppRequest(final Retrofit restAdapter) {
        return restAdapter.create(AppRequest.class);
    }

    @Provides
    @Singleton
    public MapRequest providesMapService(final Retrofit restAdapter) {
        return restAdapter.create(MapRequest.class);
    }

    @Provides
    @Singleton
    public EncounterRequest providesEncounterService(final Retrofit restAdapter) {
        return restAdapter.create(EncounterRequest.class);
    }

    @Provides
    @Singleton
    public TourRequest providesTourRequest(final Retrofit restAdapter) {
        return restAdapter.create(TourRequest.class);
    }

    @Provides
    @Singleton
    public UserRequest providesUserRequest(final Retrofit restAdapter) {
        return restAdapter.create(UserRequest.class);
    }

    @Provides
    @Singleton
    public EntourageRequest providesEntourageRequest(final Retrofit restAdapter) {
        return restAdapter.create(EntourageRequest.class);
    }

    @Provides
    @Singleton
    public NewsfeedRequest providesNewsfeedRequest(final Retrofit restAdapter) {
        return restAdapter.create(NewsfeedRequest.class);
    }

    @Provides
    @Singleton
    public InvitationRequest providesInvitationRequest(final Retrofit restAdapter) {
        return restAdapter.create(InvitationRequest.class);
    }

    @Provides
    @Singleton
    public PartnerRequest providesPartnerRequest(final Retrofit restAdapter) {
        return restAdapter.create(PartnerRequest.class);
    }

    @Provides
    @Singleton
    public EncounterTapeTaskQueue providesEncounterTapeTaskQueue(Application application) {
        Gson gson = new GsonBuilder().create();
        FileObjectQueue.Converter<CreateEncounterPresenter.EncounterUploadTask> converter = new social.entourage.android.tools.GsonConverter<>(gson, CreateEncounterPresenter.EncounterUploadTask.class);

        File queueFile = new File(application.getApplicationContext().getFilesDir(), Constants.FILENAME_TAPE_QUEUE);
        FileObjectQueue<CreateEncounterPresenter.EncounterUploadTask> delegate = null;

        try {
            delegate = new FileObjectQueue<>(queueFile, converter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new EncounterTapeTaskQueue(delegate, application.getApplicationContext());
    }
}
