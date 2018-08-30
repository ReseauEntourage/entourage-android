package social.entourage.android.guide.poi;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to ReadPoiFragment
 * @see ReadPoiFragment
 */
@Module
final class ReadPoiModule {
    private final ReadPoiFragment fragment;

    public ReadPoiModule(final ReadPoiFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public ReadPoiPresenter providesMainPresenter() {
        return new ReadPoiPresenter(fragment);
    }
}
