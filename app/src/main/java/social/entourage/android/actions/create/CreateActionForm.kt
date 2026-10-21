package social.entourage.android.actions.create

import android.net.Uri
import androidx.annotation.StringRes
import social.entourage.android.R
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ActionSection
import social.entourage.android.api.model.Address
import social.entourage.android.tools.utils.Const

/**
 * Liste des étapes de l'assistant de création / d'édition d'une entraide (contribution ou demande).
 * Elle est calculée par [CreateActionViewModel.steps] : l'étape [SHARING] s'ajoute ou se retire selon
 * la catégorie et l'existence d'un groupe par défaut. L'aperçu vient après la dernière étape.
 */
enum class CreateActionStep {
    CATEGORY,
    DESCRIPTION,
    LOCATION,
    SHARING
}

/** Champs validés, chacun rattaché à l'étape qui l'affiche. */
enum class CreateActionField(val step: CreateActionStep) {
    CATEGORY(CreateActionStep.CATEGORY),
    TITLE(CreateActionStep.DESCRIPTION),
    DESCRIPTION(CreateActionStep.DESCRIPTION),
    LOCATION(CreateActionStep.LOCATION),
    SHARING(CreateActionStep.SHARING)
}

/** État complet du formulaire de création / d'édition d'entraide. */
data class CreateActionForm(
    /** Identifiant de la catégorie (`ActionSection.id`). */
    var sectionId: String? = null,
    var title: String = "",
    var description: String = "",

    var address: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,

    /** Nouvelle image choisie (contribution uniquement), envoyée à la publication. */
    var imageUri: Uri? = null,
    /** Image actuelle de l'entraide éditée (contribution uniquement). */
    var imageUrl: String? = null,

    /** Partage dans le groupe par défaut : null tant que l'utilisateur n'a pas répondu. */
    var shareInGroup: Boolean? = null,
) {

    /** « Temps de partage » : la seule catégorie qui déclenche l'étape de partage dans un groupe. */
    fun isSharingTime(): Boolean = sectionId == ActionSection.social

    /**
     * Valide tous les champs et renvoie le message à afficher sous chaque champ en erreur.
     * Le choix de partage n'est exigé qu'à la création et si l'étape existe ([sharingStep]).
     */
    fun validate(isEdition: Boolean, sharingStep: Boolean): Map<CreateActionField, Int> {
        val errors = linkedMapOf<CreateActionField, Int>()
        if (sectionId == null) {
            errors[CreateActionField.CATEGORY] = R.string.error_categories_create_group
        }
        if (title.isBlank() || title.length < Const.GROUP_NAME_MIN_LENGTH) {
            errors[CreateActionField.TITLE] = R.string.create_action_error_title
        }
        if (description.isBlank() || description.length < Const.GROUP_DESCRIPTION_MIN_LENGTH) {
            errors[CreateActionField.DESCRIPTION] = R.string.create_action_error_description
        }
        if (address.isNullOrBlank()) {
            errors[CreateActionField.LOCATION] = R.string.create_action_error_location
        }
        if (sharingStep && !isEdition && shareInGroup == null) {
            errors[CreateActionField.SHARING] = R.string.create_action_error_sharing
        }
        return errors
    }

    private fun location(): Address? {
        val lat = latitude
        val lng = longitude
        return if (lat != null && lng != null) Address(lat, lng) else null
    }

    /** Corps de création : même contenu qu'avant la refonte (`hasConsent` toujours à vrai). */
    fun toCreateAction(autoPost: Boolean): Action {
        val action = Action().title(title).description(description)
        action.sectionName = sectionId
        action.location = location()
        action.hasConsent = true
        action.autoPostAtCreate = autoPost
        return action
    }

    /**
     * Corps de mise à jour : seuls les champs modifiés sont envoyés (titre, description, catégorie,
     * localisation), comme avant la refonte.
     */
    fun toUpdateAction(edited: Action, autoPost: Boolean): Action {
        val action = Action()
        action.title = title.takeIf { it != edited.title }
        action.description = description.takeIf { it != edited.description }
        action.sectionName = sectionId?.takeIf { it != edited.sectionName }
        val newLocation = location()
        action.location = newLocation?.takeIf {
            it.latitude != edited.location?.latitude || it.longitude != edited.location?.longitude
        }
        action.autoPostAtCreate = autoPost
        action.id = edited.id
        action.uuid = edited.uuid
        return action
    }

    /** Copie pour détecter les modifications (confirmation à la sortie). */
    fun snapshot(): CreateActionForm = copy()

    companion object {
        const val MAX_DESCRIPTION_LENGTH = 800

        fun from(action: Action): CreateActionForm = CreateActionForm(
            sectionId = action.sectionName,
            title = action.title ?: "",
            description = action.description ?: "",
            address = action.metadata?.displayAddress,
            latitude = action.location?.latitude,
            longitude = action.location?.longitude,
            imageUrl = action.imageUrl,
        )
    }
}

@StringRes
internal fun firstErrorOf(errors: Map<CreateActionField, Int>, vararg fields: CreateActionField): Int? =
    fields.firstNotNullOfOrNull { errors[it] }
