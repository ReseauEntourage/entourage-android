package social.entourage.android.entourage.my

import dagger.Module
import dagger.Provides

/**
 * Created by mihaiionescu on 03/08/16.
 */
@Module
class MyEntouragesModule(private val fragment: MyEntouragesFragment) {
    @Provides
    fun providesMyEntouragesFragment(): MyEntouragesFragment {
        return fragment
    }

}