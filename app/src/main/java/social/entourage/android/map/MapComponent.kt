package social.entourage.android.map

import dagger.Component
import social.entourage.android.ActivityScope
import social.entourage.android.EntourageComponent

/**
 * Component linked to MapFragment lifecycle
 * Provide a MapPresenter
 * @see MapFragment
 *
 * @see MapPresenter
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [MapModule::class])
interface MapComponent {
    fun inject(fragment: MapFragment?)
    val mapPresenter: MapPresenter?
}