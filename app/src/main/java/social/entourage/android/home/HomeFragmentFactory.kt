package social.entourage.android.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import social.entourage.android.home.compose.HomeComposeFeature

/**
 * Le graphe de navigation référence [HomeFragment] : cette fabrique le remplace par [HomeComposeFragment]
 * quand [HomeComposeFeature.ENABLED] est vrai (comparaison XML / Compose sur la branche `homeCompose`).
 */
class HomeFragmentFactory : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment =
        if (className == HomeFragment::class.java.name && HomeComposeFeature.ENABLED) {
            HomeComposeFragment()
        } else {
            super.instantiate(classLoader, className)
        }
}
