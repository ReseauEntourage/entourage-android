package social.entourage.android.base

import javax.inject.Scope

/**
 * Commodity alias for Scope running only in an Activity
 */
@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope