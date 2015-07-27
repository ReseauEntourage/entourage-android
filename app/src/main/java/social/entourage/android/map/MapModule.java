package social.entourage.android.map;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to MapEntourageFragment
 * @see MapEntourageFragment
 */
@Module
final class MapModule {
    private final MapEntourageFragment fragment;

    public MapModule(final MapEntourageFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public MapEntourageFragment providesMapEntourageFragment() {
        return fragment;
    }
}
