package social.entourage.android.entourage.create

import dagger.Module
import dagger.Provides

/**
 * Created by mihaiionescu on 28/04/16.
 */
@Module
class CreateEntourageModule(private val fragment: CreateEntourageFragment) {
    @Provides
    fun providesCreateEntourageFragment(): CreateEntourageFragment {
        return fragment
    }

}