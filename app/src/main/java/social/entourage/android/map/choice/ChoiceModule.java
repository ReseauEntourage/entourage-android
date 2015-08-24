package social.entourage.android.map.choice;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to ChoiceFragment
 * @see ChoiceFragment
 */
@Module
public class ChoiceModule {
    private final ChoiceFragment fragment;

    public ChoiceModule(final ChoiceFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public ChoiceFragment providesChoiceFragment() {
        return fragment;
    }
}
