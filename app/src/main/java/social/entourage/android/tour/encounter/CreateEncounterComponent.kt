package social.entourage.android.tour.encounter

import dagger.Component
import social.entourage.android.ActivityScope
import social.entourage.android.EntourageComponent

/**
 * Component linked to CreateEncounterActivity lifecycle
 * Provide a CreateEncounterPresenter
 * @see CreateEncounterActivity
 *
 * @see CreateEncounterPresenter
 */
@ActivityScope
@Component(dependencies = [EntourageComponent::class], modules = [CreateEncounterModule::class])
interface CreateEncounterComponent {
    fun inject(activity: CreateEncounterActivity?)
    val createEncounterPresenter: CreateEncounterPresenter?
}