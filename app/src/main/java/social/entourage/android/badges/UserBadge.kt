package social.entourage.android.badges

import android.graphics.drawable.PictureDrawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.RawRes
import com.caverock.androidsvg.SVG
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
    @RawRes val svgRes: Int,
    val titleRes: Int,
    val descriptionShortRes: Int,
    val howItWorksRes: Int,
    val whatItMeansRes: Int,
    val mechanismRes: Int,
    val isReversible: Boolean,
    val ctaLabelRes: Int,
    val maxProgress: Int,
    val unlockedMessageRes: Int,
    val progressHintRes: Int
)

fun ImageView.loadBadgeSvg(@RawRes resId: Int) {
    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    val svg = SVG.getFromResource(context, resId)
    setImageDrawable(PictureDrawable(svg.renderToPicture()))
    scaleType = ImageView.ScaleType.FIT_CENTER
}

val ALL_BADGE_DEFINITIONS = listOf(
    BadgeDefinition(
        key = BadgeKey.PREMIER_PAS,
        emoji = "👣",
        svgRes = R.raw.badge_bienvenue,
        titleRes = R.string.badge_premier_pas_title,
        descriptionShortRes = R.string.badge_premier_pas_description_short,
        howItWorksRes = R.string.badge_premier_pas_how_it_works,
        whatItMeansRes = R.string.badge_premier_pas_what_it_means,
        mechanismRes = R.string.badge_mechanism_irreversible,
        isReversible = false,
        ctaLabelRes = R.string.badge_premier_pas_cta,
        maxProgress = 1,
        unlockedMessageRes = R.string.badge_premier_pas_unlocked_message,
        progressHintRes = R.string.badge_premier_pas_progress_hint
    ),
    BadgeDefinition(
        key = BadgeKey.PREMIER_LIEN,
        emoji = "🤝",
        svgRes = R.raw.badge_premier_contact,
        titleRes = R.string.badge_premier_lien_title,
        descriptionShortRes = R.string.badge_premier_lien_description_short,
        howItWorksRes = R.string.badge_premier_lien_how_it_works,
        whatItMeansRes = R.string.badge_premier_lien_what_it_means,
        mechanismRes = R.string.badge_mechanism_irreversible,
        isReversible = false,
        ctaLabelRes = R.string.badge_premier_lien_cta,
        maxProgress = 1,
        unlockedMessageRes = R.string.badge_premier_lien_unlocked_message,
        progressHintRes = R.string.badge_premier_lien_progress_hint
    ),
    BadgeDefinition(
        key = BadgeKey.DIFFUSEUR_LIENS,
        emoji = "🪁",
        svgRes = R.raw.badge_moteur_rencontres,
        titleRes = R.string.badge_diffuseur_liens_title,
        descriptionShortRes = R.string.badge_diffuseur_liens_description_short,
        howItWorksRes = R.string.badge_diffuseur_liens_how_it_works,
        whatItMeansRes = R.string.badge_diffuseur_liens_what_it_means,
        mechanismRes = R.string.badge_mechanism_reversible,
        isReversible = true,
        ctaLabelRes = R.string.badge_diffuseur_liens_cta,
        maxProgress = 3,
        unlockedMessageRes = R.string.badge_diffuseur_liens_unlocked_message,
        progressHintRes = R.string.badge_diffuseur_liens_progress_hint
    ),
    BadgeDefinition(
        key = BadgeKey.AS_PAPOTAGE,
        emoji = "💬",
        svgRes = R.raw.badge_fidele_papotages,
        titleRes = R.string.badge_as_papotage_title,
        descriptionShortRes = R.string.badge_as_papotage_description_short,
        howItWorksRes = R.string.badge_as_papotage_how_it_works,
        whatItMeansRes = R.string.badge_as_papotage_what_it_means,
        mechanismRes = R.string.badge_mechanism_reversible,
        isReversible = true,
        ctaLabelRes = R.string.badge_as_papotage_cta,
        maxProgress = 3,
        unlockedMessageRes = R.string.badge_as_papotage_unlocked_message,
        progressHintRes = R.string.badge_as_papotage_progress_hint
    ),
    BadgeDefinition(
        key = BadgeKey.TISSEUR_LIENS,
        emoji = "🌱",
        svgRes = R.raw.badge_voix_presente,
        titleRes = R.string.badge_tisseur_liens_title,
        descriptionShortRes = R.string.badge_tisseur_liens_description_short,
        howItWorksRes = R.string.badge_tisseur_liens_how_it_works,
        whatItMeansRes = R.string.badge_tisseur_liens_what_it_means,
        mechanismRes = R.string.badge_mechanism_reversible,
        isReversible = true,
        ctaLabelRes = R.string.badge_tisseur_liens_cta,
        maxProgress = 3,
        unlockedMessageRes = R.string.badge_tisseur_liens_unlocked_message,
        progressHintRes = R.string.badge_tisseur_liens_progress_hint
    )
)

data class UserBadgeProgress(
    val definition: BadgeDefinition,
    val isObtained: Boolean,
    val progress: Int,
    val maxProgress: Int,
    val obtainedDate: String? = null
)

fun buildProgressFromApi(apiBadges: List<ApiBadge>): List<UserBadgeProgress> {
    return ALL_BADGE_DEFINITIONS.map { def ->
        val api = apiBadges.firstOrNull { BadgeKey.fromApiKey(it.name) == def.key }
        val isObtained = api?.active ?: false
        val current = api?.metadata?.current ?: 0
        val target = api?.metadata?.target ?: def.maxProgress
        UserBadgeProgress(
            definition = def,
            isObtained = isObtained,
            progress = if (isObtained) target else current,
            maxProgress = target,
            obtainedDate = api?.awardedAt
        )
    }
}
