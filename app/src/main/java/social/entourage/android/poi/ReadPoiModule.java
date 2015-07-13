package social.entourage.android.poi;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to ReadPoiActivity
 * @see ReadPoiActivity
 */
@Module
public final class ReadPoiModule {
    private final ReadPoiActivity activity;

    public ReadPoiModule(final ReadPoiActivity activity) {
        this.activity = activity;
    }

    @Provides
    public ReadPoiPresenter providesMainPresenter() {
        return new ReadPoiPresenter(activity);
    }
}
