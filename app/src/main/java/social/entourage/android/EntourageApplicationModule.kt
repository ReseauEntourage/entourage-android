package social.entourage.android

import android.app.Application
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class EntourageApplicationModule(private val app: EntourageApplication) {
    @Provides
    @Singleton
    fun providesApplication(): Application {
        return app
    }

}