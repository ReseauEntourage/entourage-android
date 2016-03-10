package social.entourage.android.map.tour.my;

import dagger.Module;
import dagger.Provides;

/**
 * Created by mihaiionescu on 10/03/16.
 */

@Module
public class MyToursModule {
    private final MyToursFragment fragment;

    public MyToursModule(final MyToursFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public MyToursFragment providesMyToursFragment() {
        return this.fragment;
    }
}
