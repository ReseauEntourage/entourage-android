package social.entourage.android.map.entourage;

import dagger.Module;
import dagger.Provides;

/**
 * Created by mihaiionescu on 28/04/16.
 */

@Module
public class CreateEntourageModule {

    private final CreateEntourageFragment fragment;


    public CreateEntourageModule(final CreateEntourageFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public CreateEntourageFragment providesCreateEntourageFragment() {
        return this.fragment;
    };
}
