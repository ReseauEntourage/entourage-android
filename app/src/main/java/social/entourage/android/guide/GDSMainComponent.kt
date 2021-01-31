package social.entourage.android.guide

import dagger.Component
import social.entourage.android.EntourageComponent
import social.entourage.android.base.ActivityScope

/**
 * Component linked to GDSMainActivity lifecycle
 * @see GDSMainActivity
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class])
interface GDSMainComponent {
    fun inject(activity: GDSMainActivity?)
}