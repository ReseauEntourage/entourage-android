package social.entourage.android.tour.encounter

import dagger.Module
import dagger.Provides

/**
 * Module related to ReadEncounterActivity
 * @see CreateEncounterActivity
 */
@Module
internal class CreateEncounterModule(private val activity: CreateEncounterActivity) {
    @Provides
    fun providesActivity(): CreateEncounterActivity {
        return activity
    }

}