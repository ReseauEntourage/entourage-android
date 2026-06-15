package social.entourage.android.badges

import social.entourage.android.R

enum class BadgeKey(val apiKey: String) {
    PREMIER_PAS("bienvenue"),
    PREMIER_LIEN("premier_contact"),
    DIFFUSEUR_LIENS("moteur_rencontres"),
    AS_PAPOTAGE("fidele_papotages"),
    TISSEUR_LIENS("voix_presente");

    companion object {
        fun fromApiKey(key: String): BadgeKey? = values().firstOrNull { it.apiKey == key }
    }
}

data class BadgeDefinition(
    val key: BadgeKey,
    val emoji: String,
    val titleRes: Int,
    val descriptionShortRes: Int,
    val howItWorksRes: Int,
    val whatItMeansRes: Int,
    val mechanismRes: Int,
    val isReversible: Boolean,
    val ctaLabelRes: Int,
    val maxProgress: Int,
    val unlockedMessageRes: Int
)

val ALL_BADGE_DEFINITIONS = listOf(
    BadgeDefinition(
        key = BadgeKey.PREMIER_PAS,
        emoji = "👣",
        titleRes = R.string.badge_premier_pas_title,
        descriptionShortRes = R.string.badge_premier_pas_description_short,
        howItWorksRes = R.string.badge_premier_pas_how_it_works,
        whatItMeansRes = R.string.badge_premier_pas_what_it_means,
        mechanismRes = R.string.badge_mechanism_irreversible,
        isReversible = false,
        ctaLabelRes = R.string.badge_premier_pas_cta,
        maxProgress = 1,
        unlockedMessageRes = R.string.badge_premier_pas_unlocked_message
    ),
    BadgeDefinition(
        key = BadgeKey.PREMIER_LIEN,
        emoji = "🤝",
        titleRes = R.string.badge_premier_lien_title,
        descriptionShortRes = R.string.badge_premier_lien_description_short,
        howItWorksRes = R.string.badge_premier_lien_how_it_works,
        whatItMeansRes = R.string.badge_premier_lien_what_it_means,
        mechanismRes = R.string.badge_mechanism_irreversible,
        isReversible = false,
        ctaLabelRes = R.string.badge_premier_lien_cta,
        maxProgress = 1,
        unlockedMessageRes = R.string.badge_premier_lien_unlocked_message
    ),
    BadgeDefinition(
        key = BadgeKey.DIFFUSEUR_LIENS,
        emoji = "🪁",
        titleRes = R.string.badge_diffuseur_liens_title,
        descriptionShortRes = R.string.badge_diffuseur_liens_description_short,
        howItWorksRes = R.string.badge_diffuseur_liens_how_it_works,
        whatItMeansRes = R.string.badge_diffuseur_liens_what_it_means,
        mechanismRes = R.string.badge_mechanism_reversible,
        isReversible = true,
        ctaLabelRes = R.string.badge_diffuseur_liens_cta,
        maxProgress = 3,
        unlockedMessageRes = R.string.badge_diffuseur_liens_unlocked_message
    ),
    BadgeDefinition(
        key = BadgeKey.AS_PAPOTAGE,
        emoji = "💬",
        titleRes = R.string.badge_as_papotage_title,
        descriptionShortRes = R.string.badge_as_papotage_description_short,
        howItWorksRes = R.string.badge_as_papotage_how_it_works,
        whatItMeansRes = R.string.badge_as_papotage_what_it_means,
        mechanismRes = R.string.badge_mechanism_reversible,
        isReversible = true,
        ctaLabelRes = R.string.badge_as_papotage_cta,
        maxProgress = 3,
        unlockedMessageRes = R.string.badge_as_papotage_unlocked_message
    ),
    BadgeDefinition(
        key = BadgeKey.TISSEUR_LIENS,
        emoji = "🌱",
        titleRes = R.string.badge_tisseur_liens_title,
        descriptionShortRes = R.string.badge_tisseur_liens_description_short,
        howItWorksRes = R.string.badge_tisseur_liens_how_it_works,
        whatItMeansRes = R.string.badge_tisseur_liens_what_it_means,
        mechanismRes = R.string.badge_mechanism_reversible,
        isReversible = true,
        ctaLabelRes = R.string.badge_tisseur_liens_cta,
        maxProgress = 3,
        unlockedMessageRes = R.string.badge_tisseur_liens_unlocked_message
    )
)

data class UserBadgeProgress(
    val definition: BadgeDefinition,
    val isObtained: Boolean,
    val progress: Int,
    val obtainedDate: String? = null
)

// Hardcoded demo progressions — replace with API data when backend is ready
fun buildHardcodedProgress(obtainedKeys: List<String>): List<UserBadgeProgress> {
    val demoInProgress = if (obtainedKeys.isNotEmpty()) mapOf(
        BadgeKey.AS_PAPOTAGE to 2,
        BadgeKey.TISSEUR_LIENS to 2
    ) else emptyMap()

    return ALL_BADGE_DEFINITIONS.map { def ->
        val isObtained = obtainedKeys.contains(def.key.apiKey)
        val progress = when {
            isObtained -> def.maxProgress
            else -> demoInProgress[def.key] ?: 0
        }
        UserBadgeProgress(
            definition = def,
            isObtained = isObtained,
            progress = progress,
            obtainedDate = null
        )
    }
}
