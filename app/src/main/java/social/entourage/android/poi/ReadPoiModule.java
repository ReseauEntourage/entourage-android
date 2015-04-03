package social.entourage.android.poi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import social.entourage.android.EntourageModule;

/**
 * Module handling all ui related dependencies
 */
@Module(
        injects = {
                ReadPoiActivity.class
        },
        addsTo = EntourageModule.class,
        complete = false,
        library = true
)
public final class ReadPoiModule {
    private final ReadPoiActivity activity;

    public ReadPoiModule(final ReadPoiActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    public ReadPoiPresenter providesMainPresenter() {
        return new ReadPoiPresenter(activity);
    }
}
