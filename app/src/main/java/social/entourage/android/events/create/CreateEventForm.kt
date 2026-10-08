package social.entourage.android.events.create

import androidx.annotation.StringRes
import social.entourage.android.R
import social.entourage.android.tools.utils.Const
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * Liste unique des étapes de l'assistant. Le nombre d'étapes affiché ("Étape N sur X"), le
 * nombre de pages du ViewPager et la progression en sont tous dérivés : ajouter ou retirer une
 * étape ici suffit. L'écran d'aperçu vient après la dernière étape et n'est pas compté.
 */
enum class CreateEventStep {
    PRESENTATION,
    WHEN,
    WHERE_AND_FOR_WHOM,
    CATEGORIES,
    SHARING
}

/** Champs validés, chacun rattaché à l'étape qui l'affiche. */
enum class CreateEventField(val step: CreateEventStep) {
    NAME(CreateEventStep.PRESENTATION),
    DESCRIPTION(CreateEventStep.PRESENTATION),
    PHOTO(CreateEventStep.PRESENTATION),
    DATE(CreateEventStep.WHEN),
    START_TIME(CreateEventStep.WHEN),
    END_TIME(CreateEventStep.WHEN),
    PLACE(CreateEventStep.WHERE_AND_FOR_WHOM),
    PLACE_LIMIT(CreateEventStep.WHERE_AND_FOR_WHOM),
    CATEGORIES(CreateEventStep.CATEGORIES)
}

/**
 * État complet du formulaire de création / d'édition. C'est aussi le format du brouillon local
 * (sérialisé en JSON) : tout champ ajouté ici est donc conservé dans le brouillon, sauf s'il est
 * marqué `@Transient`.
 */
data class CreateEventForm(
    var title: String = "",
    var description: String = "",

    // Photo : une seule des trois sources est renseignée à la fois.
    var entourageImageId: Int? = null,
    var uploadKey: String? = null,
    /** URL affichable (image du catalogue ou image de l'événement édité). */
    var photoUrl: String? = null,
    /** Copie locale d'une photo envoyée. Jamais conservée dans le brouillon. */
    @Transient var localPhotoPath: String? = null,

    /** Date au format ISO `yyyy-MM-dd`. */
    var date: String? = null,
    /** Heures au format `HH:mm`. */
    var startTime: String? = null,
    var endTime: String? = null,
    /** 0 = juste une fois, sinon valeur de [Recurrence]. */
    var recurrence: Int = Recurrence.NO_RECURRENCE.value,

    var online: Boolean = false,
    var address: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var googlePlaceId: String? = null,
    var eventUrl: String = "",
    /** null = pas de limite ("Non"). */
    var placeLimit: Int? = null,

    // Cartes de public et d'accessibilité.
    // wheelchairAccessible -> metadata.pmr, familyFriendly -> metadata.kids_friendly
    // (voir [toCreateEvent], seul point de sérialisation).
    var wheelchairAccessible: Boolean = false,
    var familyFriendly: Boolean = false,
    var reservedFemale: Boolean = false,

    var interests: MutableList<String> = mutableListOf(),
    var neighborhoodIds: MutableList<Int> = mutableListOf(),
) {

    fun hasPhoto(): Boolean =
        entourageImageId != null || uploadKey != null || photoUrl != null || localPhotoPath != null

    /** Passage en ligne / présentiel. La carte fauteuil n'a pas de sens en ligne : valeur annulée. */
    fun setOnlineMode(isOnline: Boolean) {
        online = isOnline
        if (isOnline) wheelchairAccessible = false
    }

    /**
     * Valide tous les champs et renvoie, pour chaque champ en erreur, le message à afficher
     * sous le champ.
     *
     * @param unchangedPastDate date d'origine d'un événement édité : si elle n'a pas été modifiée,
     * elle n'est pas refusée même si elle est passée (on peut corriger un événement déjà passé)
     */
    fun validate(
        isUploading: Boolean = false,
        today: LocalDate = LocalDate.now(),
        unchangedPastDate: String? = null
    ): Map<CreateEventField, Int> {
        val errors = linkedMapOf<CreateEventField, Int>()

        if (title.isBlank() || title.length < Const.GROUP_NAME_MIN_LENGTH) {
            errors[CreateEventField.NAME] = R.string.create_event_error_name
        }
        if (description.isBlank() || description.length < Const.GROUP_DESCRIPTION_MIN_LENGTH) {
            errors[CreateEventField.DESCRIPTION] = R.string.create_event_error_description
        }
        if (!hasPhoto()) {
            errors[CreateEventField.PHOTO] = R.string.create_event_error_photo
        } else if (isUploading) {
            errors[CreateEventField.PHOTO] = R.string.create_event_error_photo_uploading
        }

        val parsedDate = parseDate(date)
        when {
            parsedDate == null -> errors[CreateEventField.DATE] = R.string.create_event_error_date_empty
            parsedDate.isBefore(today) && date != unchangedPastDate -> errors[CreateEventField.DATE] = R.string.create_event_error_date_past
        }
        val start = parseTime(startTime)
        val end = parseTime(endTime)
        if (start == null) errors[CreateEventField.START_TIME] = R.string.create_event_error_start_time_empty
        if (end == null) errors[CreateEventField.END_TIME] = R.string.create_event_error_end_time_empty
        if (start != null && end != null && !end.isAfter(start)) {
            errors[CreateEventField.END_TIME] = R.string.create_event_error_end_before_start
        }

        if (online) {
            if (!isValidLink(eventUrl)) {
                errors[CreateEventField.PLACE] = R.string.create_event_error_link
            }
        } else if (address.isNullOrBlank()) {
            errors[CreateEventField.PLACE] = R.string.create_event_error_address
        }
        placeLimit?.let {
            if (it < 1) errors[CreateEventField.PLACE_LIMIT] = R.string.create_event_error_place_limit
        }

        if (interests.isEmpty()) {
            errors[CreateEventField.CATEGORIES] = R.string.create_event_error_categories
        }
        return errors
    }

    /**
     * Construit le corps de requête. `pmr` (fauteuil, false en ligne) et `kids_friendly` (famille)
     * sont toujours envoyés explicitement, comme `reserved_female`.
     *
     * @param datePattern format des champs `starts_at` / `ends_at` (ressource `event_date_formatter_to_string`)
     * @param editedRecurrence récurrence de l'événement édité, ou null en création. En édition, la
     * récurrence n'est envoyée que si l'utilisateur l'a modifiée.
     */
    fun toCreateEvent(
        datePattern: String,
        isEdition: Boolean = false,
        editedRecurrence: Int? = null
    ): CreateEvent {
        val event = CreateEvent()
        event.title = title
        event.description = description
        event.online = online
        event.interests = interests.toMutableList()
        event.neighborhoodIds = neighborhoodIds.toMutableList()

        entourageImageId?.let { event.entourageImageId = it }
        uploadKey?.let { event.imageUrl = it }

        val recurrenceChanged = editedRecurrence == null || recurrence != editedRecurrence
        if (recurrence != Recurrence.NO_RECURRENCE.value && recurrenceChanged) {
            event.recurrence = recurrence
        }

        val metadata = event.metadata ?: Metadata().also { event.metadata = it }
        val formatter = SimpleDateFormat(datePattern, Locale.US)
        buildDate(date, startTime)?.let { metadata.startsAt = formatter.format(it) }
        buildDate(date, endTime)?.let { metadata.endsAt = formatter.format(it) }
        metadata.reserved_female = reservedFemale
        metadata.pmr = wheelchairAccessible && !online
        metadata.kidsFriendly = familyFriendly
        metadata.placeLimit = when {
            placeLimit != null && placeLimit!! > 0 -> placeLimit
            // En édition, 0 efface une limite posée auparavant.
            isEdition -> 0
            else -> null
        }
        if (online) {
            event.eventUrl = eventUrl.trim()
        } else {
            metadata.streetAddress = address
            latitude?.let { event.latitude = it }
            longitude?.let { event.longitude = it }
            metadata.googlePlaceId = googlePlaceId ?: ""
            metadata.placeName = ""
        }
        return event
    }

    companion object {
        fun parseDate(value: String?): LocalDate? = try {
            value?.let { LocalDate.parse(it) }
        } catch (e: DateTimeParseException) {
            null
        }

        fun parseTime(value: String?): LocalTime? = try {
            value?.let { LocalTime.parse(it) }
        } catch (e: DateTimeParseException) {
            null
        }

        fun isValidLink(value: String): Boolean {
            val link = value.trim()
            return link.length > "https://".length && link.startsWith("https://", ignoreCase = true)
        }

        private fun buildDate(date: String?, time: String?): java.util.Date? {
            val day = parseDate(date) ?: return null
            val hour = parseTime(time) ?: return null
            val calendar = java.util.Calendar.getInstance()
            calendar.clear()
            calendar.set(day.year, day.monthValue - 1, day.dayOfMonth, hour.hour, hour.minute, 0)
            return calendar.time
        }
    }
}

@StringRes
internal fun firstErrorOf(errors: Map<CreateEventField, Int>, vararg fields: CreateEventField): Int? =
    fields.firstNotNullOfOrNull { errors[it] }
