package social.entourage.android.main;

import social.entourage.android.EntourageModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Module handling all ui related dependencies
 */
@Module(
        injects = {
                MainActivity.class
        },
        addsTo = EntourageModule.class,
        complete = false,
        library = true
)
public final class MainModule {
    private final MainActivity activity;

    public MainModule(final MainActivity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    public MainPresenter providesMainPresenter() {
        return new MainPresenter(activity);
    }
}
