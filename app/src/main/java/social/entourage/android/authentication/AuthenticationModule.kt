package social.entourage.android.authentication

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import social.entourage.android.authentication.ComplexPreferences.Companion.getComplexPreferences
import javax.inject.Singleton

/**
 * Module related to Application
 * Providing Authentication related dependencies
 */
@Module
class AuthenticationModule {
    @Provides
    @Singleton
    fun providesAuthenticationController(userSharedPref: ComplexPreferences?): AuthenticationController {
        return AuthenticationController(userSharedPref!!).init()
    }

    @Provides
    @Singleton
    fun providesUserSharedPreferences(application: Application?): ComplexPreferences {
        return getComplexPreferences(application!!, "userPref", Context.MODE_PRIVATE)
    }
}