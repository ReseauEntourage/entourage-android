package social.entourage.android.entourage.my;

import dagger.Module;
import dagger.Provides;

/**
 * Created by mihaiionescu on 03/08/16.
 */

@Module
public class MyEntouragesModule {

    private final MyEntouragesFragment fragment;

    public MyEntouragesModule(MyEntouragesFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public MyEntouragesFragment providesMyEntouragesFragment() {
        return fragment;
    }
}
