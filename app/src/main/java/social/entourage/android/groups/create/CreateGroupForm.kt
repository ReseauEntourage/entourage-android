package social.entourage.android.groups.create

import androidx.annotation.StringRes
import androidx.collection.ArrayMap
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.tools.utils.Const

/**
 * Liste unique des étapes de l'assistant de création / d'édition de groupe. Le nombre d'étapes
 * affiché (« Étape N sur X ») et la progression en sont dérivés ; l'aperçu vient après la dernière
 * étape et n'est pas compté.
 */
enum class CreateGroupStep {
    INFO,
    INTERESTS,
    PHOTO
}

/** Champs validés, chacun rattaché à l'étape qui l'affiche. */
enum class CreateGroupField(val step: CreateGroupStep) {
    NAME(CreateGroupStep.INFO),
    DESCRIPTION(CreateGroupStep.INFO),
    LOCATION(CreateGroupStep.INFO),
    INTERESTS(CreateGroupStep.INTERESTS),
    PHOTO(CreateGroupStep.PHOTO)
}

/** État complet du formulaire de création / d'édition de groupe. */
data class CreateGroupForm(
    var name: String = "",
    var description: String = "",
    var welcomeMessage: String = "",

    var address: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,

    var interests: MutableList<String> = mutableListOf(),
    /** Précision saisie pour la catégorie « Autre » (création uniquement). */
    var otherInterest: String = "",

    /** Photo choisie dans le catalogue `neighborhood_images`. */
    var imageId: Int? = null,
    /** URL affichable : image choisie ou image actuelle du groupe édité. */
    var photoUrl: String? = null,
) {

    fun hasPhoto(): Boolean = imageId != null || photoUrl != null

    fun hasLocation(): Boolean =
        latitude != null && longitude != null && latitude != 0.0 && longitude != 0.0

    /**
     * Valide tous les champs et renvoie le message à afficher sous chaque champ en erreur.
     * En édition, le lieu et la photo (déjà enregistrés) ne sont pas exigés, comme avant.
     */
    fun validate(isEdition: Boolean = false): Map<CreateGroupField, Int> {
        val errors = linkedMapOf<CreateGroupField, Int>()
        if (name.isBlank() || name.length < Const.GROUP_NAME_MIN_LENGTH) {
            errors[CreateGroupField.NAME] = R.string.create_group_error_name
        }
        if (description.isBlank() || description.length < Const.GROUP_DESCRIPTION_MIN_LENGTH) {
            errors[CreateGroupField.DESCRIPTION] = R.string.create_group_error_description
        }
        if (!isEdition && !hasLocation()) {
            errors[CreateGroupField.LOCATION] = R.string.create_group_error_location
        }
        if (interests.isEmpty()) {
            errors[CreateGroupField.INTERESTS] = R.string.error_categories_create_group
        }
        if (!isEdition && !hasPhoto()) {
            errors[CreateGroupField.PHOTO] = R.string.error_categories_create_group_image
        }
        return errors
    }

    /** Corps de `POST neighborhoods` : même contenu qu'avant la refonte. */
    fun toGroup(): Group {
        val group = Group().name(name).description(description)
        if (welcomeMessage.isNotEmpty()) group.welcomeMessage(welcomeMessage)
        latitude?.let { group.latitude = it }
        longitude?.let { group.longitude = it }
        group.displayAddress = address
        group.interests(interests.toMutableList())
        if (otherInterest.isNotEmpty() && interests.contains(OTHER_INTEREST)) group.otherInterest(otherInterest)
        group.neighborhoodImageId(imageId)
        return group
    }

    /** Corps de `PATCH neighborhoods/{id}` : même contenu qu'avant la refonte. */
    fun toUpdateBody(): ArrayMap<String, Any> {
        val edited: MutableMap<String, Any> = mutableMapOf()
        edited["name"] = name.trimEndSpaces()
        edited["description"] = description.trimEndSpaces()
        edited["welcome_message"] = welcomeMessage.trimEndSpaces()
        imageId?.let { edited["neighborhood_image_id"] = it }
        edited["interests"] = interests.toList()
        val body: ArrayMap<String, Any> = ArrayMap()
        body["neighborhood"] = edited
        return body
    }

    /** Copie profonde, pour détecter les modifications (confirmation à la sortie). */
    fun snapshot(): CreateGroupForm = copy(interests = interests.toMutableList())

    companion object {
        const val OTHER_INTEREST = "other"
        const val MAX_DESCRIPTION_LENGTH = 200

        private fun String.trimEndSpaces(): String = trim { it <= ' ' }

        fun from(group: Group): CreateGroupForm = CreateGroupForm(
            name = group.name ?: "",
            description = group.description ?: "",
            welcomeMessage = group.welcomeMessage ?: "",
            address = group.displayAddress,
            latitude = group.latitude,
            longitude = group.longitude,
            interests = group.interests.toMutableList(),
            imageId = null,
            photoUrl = group.imageUrl,
        )
    }
}

@StringRes
internal fun firstErrorOf(errors: Map<CreateGroupField, Int>, vararg fields: CreateGroupField): Int? =
    fields.firstNotNullOfOrNull { errors[it] }
