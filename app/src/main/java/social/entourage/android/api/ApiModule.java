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

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import social.entourage.android.BuildConfig;
import social.entourage.android.Constants;
import social.entourage.android.authentication.AuthenticationInterceptor;
import social.entourage.android.map.encounter.CreateEncounterPresenter;
import social.entourage.android.api.tape.EncounterTapeTaskQueue;

/**
 * Module related to Application
 * Providing API related dependencies
 */
@Module
public class ApiModule {

    @Provides
    @Singleton
    public Endpoint providesEndPoint() {
        return Endpoints.newFixedEndpoint(BuildConfig.ENTOURAGE_URL);
    }

    @Provides
    @Singleton
    public RestAdapter providesRestAdapter(final Endpoint endpoint, final AuthenticationInterceptor interceptor) {
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
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
                .create();

        return new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(interceptor)
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.BASIC)
                .setConverter(new GsonConverter(gson))
                .build();
    }

    @Provides
    @Singleton
    public LoginRequest providesLoginService(final RestAdapter restAdapter) {
        return restAdapter.create(LoginRequest.class);
    }

    @Provides
    @Singleton
    public MapRequest providesMapService(final RestAdapter restAdapter) {
        return restAdapter.create(MapRequest.class);
    }

    @Provides
    @Singleton
    public EncounterRequest providesEncounterService(final RestAdapter restAdapter) {
        return restAdapter.create(EncounterRequest.class);
    }

    @Provides
    @Singleton
    public TourRequest providesTourRequest(final RestAdapter restAdapter) {
        return restAdapter.create(TourRequest.class);
    }

    @Provides
    @Singleton
    public UserRequest providesUserRequest(final RestAdapter restAdapter) {
        return restAdapter.create(UserRequest.class);
    }

    @Provides
    @Singleton
    public EncounterTapeTaskQueue providesEncounterTapeTaskQueue(Application application) {
        Gson gson = new GsonBuilder().create();
        FileObjectQueue.Converter<CreateEncounterPresenter.EncounterUploadTask> converter = new social.entourage.android.tools.GsonConverter<>(gson, CreateEncounterPresenter.EncounterUploadTask.class);

        File queueFile = new File(application.getApplicationContext().getFilesDir(), Constants.FILENAME);
        FileObjectQueue<CreateEncounterPresenter.EncounterUploadTask> delegate = null;

        try {
            delegate = new FileObjectQueue<>(queueFile, converter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new EncounterTapeTaskQueue(delegate, application.getApplicationContext());
    }
}
