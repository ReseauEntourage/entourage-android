package social.entourage.android.map;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to MapFragment
 * @see MapFragment
 */
@Module
public class MapModule {
    private final MapFragment fragment;

    public MapModule(final MapFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public MapFragment providesMapFragment() {
        return fragment;
    }
}
