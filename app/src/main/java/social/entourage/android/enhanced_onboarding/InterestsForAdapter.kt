package social.entourage.android.enhanced_onboarding

data class InterestForAdapter(
    val icon: Int,
    val title: String,
    val subtitle: String,
    var isSelected: Boolean = false,
    val id: String // Ajout d'un identifiant
)
