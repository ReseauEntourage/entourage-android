package social.entourage.android.map.tour.join.received;

import dagger.Module;
import dagger.Provides;

/**
 * Created by mihaiionescu on 18/03/16.
 */

@Module
public class TourJoinRequestReceivedModule {
    private TourJoinRequestReceivedActivity activity;

    public TourJoinRequestReceivedModule(final TourJoinRequestReceivedActivity activity) {
        this.activity = activity;
    }

    @Provides
    public TourJoinRequestReceivedActivity providesTourJoinRequestReceivedActivity() {
        return activity;
    }
}
