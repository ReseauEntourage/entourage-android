package social.entourage.android.events.create

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Une page par étape de [steps], suivie de l'écran d'aperçu. Le nombre de pages est dérivé de la
 * liste : il n'y a plus de compte codé en dur.
 */
class CreateEventViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val steps: List<CreateEventStep>
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = steps.size + 1

    override fun createFragment(position: Int): Fragment {
        if (position >= steps.size) return CreateEventPreviewFragment()
        return when (steps[position]) {
            CreateEventStep.PRESENTATION -> CreateEventStepOneFragment()
            CreateEventStep.WHEN -> CreateEventStepTwoFragment()
            CreateEventStep.WHERE_AND_FOR_WHOM -> CreateEventStepThreeFragment()
            CreateEventStep.CATEGORIES -> CreateEventStepFourFragment()
            CreateEventStep.SHARING -> CreateEventStepFiveFragment()
        }
    }
}
