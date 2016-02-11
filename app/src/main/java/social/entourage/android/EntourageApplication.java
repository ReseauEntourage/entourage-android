package social.entourage.android;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import net.danlew.android.joda.JodaTimeAndroid;

import social.entourage.android.api.ApiModule;
import social.entourage.android.authentication.AuthenticationModule;

/**
 * Application setup for Flurry, JodaTime and Dagger
 */
public class EntourageApplication extends Application {

    private EntourageComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        setupFlurry();
        JodaTimeAndroid.init(this);
        setupDagger();
        setupImageLoader(getApplicationContext());
    }

    private void setupDagger() {
        component = DaggerEntourageComponent.builder()
                .entourageModule(new EntourageModule(this))
                .apiModule(new ApiModule())
                .authenticationModule(new AuthenticationModule())
                .build();
        component.inject(this);
    }

    private void setupFlurry() {
        FlurryAgent.setLogEnabled(true);
        FlurryAgent.setLogLevel(Log.VERBOSE);
        //FlurryAgent.setReportLocation(true);
        FlurryAgent.setLogEvents(true);
        FlurryAgent.init(this, BuildConfig.FLURRY_API_KEY);
    }

    private void setupImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 *1024 * 1024)
                .denyCacheImageMultipleSizesInMemory()
                .build();
        ImageLoader.getInstance().init(config);
    }

    public EntourageComponent getEntourageComponent() {
        return component;
    }

    public static EntourageApplication get(Context context) {
        return (EntourageApplication) context.getApplicationContext();
    }
}
