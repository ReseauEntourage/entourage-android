package social.entourage.android.profile

import android.view.View
import social.entourage.android.databinding.ActivityLayoutProfileBinding

/**
 * Affiche ou masque le squelette de la page profil (mon profil et profil d'un autre membre).
 *
 * Le squelette reste visible jusqu'à ce que l'écran réel (`containerProfile`) soit prêt : sans lui,
 * l'écran restait entièrement blanc le temps des appels réseau enchaînés.
 */
internal fun ActivityLayoutProfileBinding.setProfileLoading(loading: Boolean) {
    val skeleton = profileSkeleton
    if (loading) {
        skeleton.root.visibility = View.VISIBLE
        skeleton.shimmerProfileSkeleton.startShimmer()
    } else {
        skeleton.shimmerProfileSkeleton.stopShimmer()
        skeleton.root.visibility = View.GONE
    }
}
