package social.entourage.android.entourage.join.received

import dagger.Module
import dagger.Provides

/**
 * Created by mihaiionescu on 18/03/16.
 */
@Module
class EntourageJoinRequestReceivedModule(private val activity: EntourageJoinRequestReceivedActivity) {
    @Provides
    fun providesEntourageJoinRequestReceivedActivity(): EntourageJoinRequestReceivedActivity {
        return activity
    }

}