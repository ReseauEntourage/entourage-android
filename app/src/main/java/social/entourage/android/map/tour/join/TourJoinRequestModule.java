package social.entourage.android.map.tour.join;

import dagger.Module;
import dagger.Provides;

/**
 * Created by mihaiionescu on 07/03/16.
 */
@Module
public class TourJoinRequestModule {
    private final TourJoinRequestFragment fragment;

    public TourJoinRequestModule(final TourJoinRequestFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public TourJoinRequestFragment providesTourJoinRequestFragment() {
        return fragment;
    }
}
