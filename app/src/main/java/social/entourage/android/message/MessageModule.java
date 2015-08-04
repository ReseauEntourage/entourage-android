package social.entourage.android.message;

import dagger.Module;
import dagger.Provides;

/**
 * Module related to MessageActivity
 * @see MessageActivity
 */
@Module
public class MessageModule {
    private final MessageActivity activity;

    public MessageModule(final MessageActivity activity) {
        this.activity = activity;
    }

    @Provides
    public MessageActivity providesActivity() {
        return activity;
    }
}
