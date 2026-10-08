package social.entourage.android.events.create

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Image
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

/**
 * État partagé de l'assistant de création / d'édition d'événement. Il est porté par l'activité et
 * remplace l'ancien singleton `CommunicationHandler`.
 *
 * - [form] contient tout le formulaire, y compris les cartes de public / accessibilité ;
 * - [steps] est la liste unique des étapes dont tout le reste est dérivé ;
 * - [errors] contient les messages à afficher sous les champs, pour les étapes sur lesquelles
 *   l'utilisateur a déjà appuyé sur « Continuer » (le message ne s'affiche qu'à ce moment-là, puis
 *   suit les modifications).
 */
class CreateEventViewModel : ViewModel(), EventImageUploadView {

    val steps: List<CreateEventStep> = CreateEventStep.values().toList()

    /** Étapes + écran d'aperçu. */
    val pageCount: Int get() = steps.size + 1
    val previewPageIndex: Int get() = steps.size

    var editedEvent: Events? = null
        private set
    val isEdition: Boolean get() = editedEvent != null

    var form: CreateEventForm = CreateEventForm()
        private set

    private var initialSnapshot: String = gson.toJson(form)
    private var initialized = false

    /** Date de l'événement édité (ISO), pour ne pas refuser une date passée laissée telle quelle. */
    private var originalDate: String? = null

    /** True quand un brouillon a été restauré à l'ouverture. */
    var draftRestored = false
        private set

    private val _currentPage = MutableLiveData(0)
    val currentPage: LiveData<Int> = _currentPage

    private val _isUploading = MutableLiveData(false)
    val isUploading: LiveData<Boolean> = _isUploading

    private val _uploadFailed = MutableLiveData<Boolean>()
    val uploadFailed: LiveData<Boolean> = _uploadFailed

    private val _errors = MutableLiveData<Map<CreateEventField, Int>>(emptyMap())
    val errors: LiveData<Map<CreateEventField, Int>> = _errors

    private val flaggedSteps = mutableSetOf<CreateEventStep>()

    private var uploadPresenter: EventImageUploadPresenter? = null

    // --- Initialisation -------------------------------------------------------------------

    /**
     * À appeler une seule fois, à l'ouverture de l'assistant.
     *
     * @param edited événement à modifier, ou null en création
     * @param draft brouillon à restaurer (création uniquement)
     * @param preselectedGroupId groupe d'où l'on vient (création depuis un groupe), ou null
     */
    fun initialize(edited: Events?, draft: CreateEventForm?, preselectedGroupId: Int?) {
        if (initialized) return
        initialized = true
        editedEvent = edited
        if (edited != null) {
            form = formFromEvent(edited)
            originalDate = form.date
        } else if (draft != null) {
            form = draft
            draftRestored = true
            if (form.wheelchairAccessible && form.online) form.wheelchairAccessible = false
            flagInvalidRestoredFields()
        }
        if (edited == null && preselectedGroupId != null && !form.neighborhoodIds.contains(preselectedGroupId)) {
            form.neighborhoodIds.add(preselectedGroupId)
        }
        initialSnapshot = gson.toJson(form)
    }

    private fun formFromEvent(event: Events): CreateEventForm {
        val metadata = event.metadata
        val startsAt = metadata?.startsAt
        val endsAt = metadata?.endsAt
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return CreateEventForm(
            title = event.title ?: "",
            description = event.description ?: "",
            photoUrl = metadata?.landscapeUrl ?: metadata?.portraitUrl,
            date = startsAt?.let { dateFormat.format(it) },
            startTime = startsAt?.let { timeFormat.format(it) },
            endTime = endsAt?.let { timeFormat.format(it) },
            recurrence = event.recurrence ?: Recurrence.NO_RECURRENCE.value,
            online = event.online == true,
            address = metadata?.displayAddress ?: event.displayAddress,
            latitude = event.location?.latitude?.takeIf { it != 0.0 },
            longitude = event.location?.longitude?.takeIf { it != 0.0 },
            googlePlaceId = metadata?.googlePlaceId,
            eventUrl = event.eventUrl ?: "",
            placeLimit = metadata?.placeLimit?.takeIf { it > 0 },
            reservedFemale = metadata?.reserved_female ?: false,
            interests = event.interests.toMutableList(),
            neighborhoodIds = event.neighborhoods?.mapNotNull { it.id }?.toMutableList()
                ?: mutableListOf(),
        )
    }

    /**
     * Un brouillon peut être obsolète (date passée, durée incohérente...) : on ne corrige rien
     * en silence, on affiche les erreurs correspondantes dès l'ouverture.
     */
    private fun flagInvalidRestoredFields() {
        val errors = form.validate(isUploading = false)
        val staleFields = listOf(
            CreateEventField.DATE to form.date,
            CreateEventField.END_TIME to form.endTime,
            CreateEventField.PLACE_LIMIT to form.placeLimit,
        )
        staleFields.forEach { (field, value) ->
            if (value != null && errors.containsKey(field)) flaggedSteps.add(field.step)
        }
        publishErrors()
    }

    // --- Navigation -----------------------------------------------------------------------

    fun setCurrentPage(page: Int) {
        _currentPage.value = page
    }

    // --- Validation -----------------------------------------------------------------------

    /**
     * Valide une étape ; en cas d'échec, les messages sont publiés dans [errors].
     * @return true si l'étape est valide
     */
    fun validateStep(step: CreateEventStep): Boolean {
        flaggedSteps.add(step)
        publishErrors()
        return form.validate(isUploading(), LocalDate.now(), originalDate).keys.none { it.step == step }
    }

    /** À appeler à chaque modification d'un champ : met à jour les messages déjà affichés. */
    fun onFormChanged() {
        if (flaggedSteps.isNotEmpty()) publishErrors()
    }

    private fun publishErrors() {
        val all = form.validate(isUploading(), LocalDate.now(), originalDate)
        _errors.value = all.filterKeys { it.step in flaggedSteps }
    }

    private fun isUploading() = _isUploading.value == true

    /** Vrai si le formulaire a été modifié depuis l'ouverture (sert à confirmer la sortie). */
    fun isDirty(): Boolean = gson.toJson(form) != initialSnapshot

    // --- Photo ----------------------------------------------------------------------------

    fun selectCatalogImage(image: Image) {
        form.entourageImageId = image.id
        form.uploadKey = null
        form.localPhotoPath = null
        form.photoUrl = image.portraitUrl ?: image.landscapeUrl
        _isUploading.value = false
        onFormChanged()
    }

    /**
     * Envoie une photo recadrée. L'upload dépend de l'application et non de l'écran, donc il
     * survit à la navigation entre étapes. [cacheDir] sert à garder une copie affichable : le
     * fichier envoyé est supprimé une fois l'upload terminé.
     */
    fun uploadLocalPhoto(file: File, cacheDir: File) {
        form.entourageImageId = null
        form.uploadKey = null
        form.photoUrl = null
        form.localPhotoPath = try {
            File(cacheDir, "event_preview_${System.currentTimeMillis()}.jpg").also {
                file.copyTo(it, overwrite = true)
            }.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "Copie de la photo impossible, l'aperçu utilisera un repli")
            null
        }
        _isUploading.value = true
        val presenter = EventImageUploadPresenter(this, EventImageUploadRepository())
        uploadPresenter = presenter
        presenter.uploadPhoto(file)
        onFormChanged()
    }

    override fun onUploadSuccess(uploadKey: String) {
        form.uploadKey = uploadKey
        _isUploading.value = false
        onFormChanged()
    }

    override fun onUploadError() {
        _isUploading.value = false
        form.uploadKey = null
        form.localPhotoPath = null
        _uploadFailed.value = true
        onFormChanged()
    }

    fun consumeUploadFailed() {
        _uploadFailed.value = false
    }

    // --- Envoi / brouillon ----------------------------------------------------------------

    fun buildRequestBody(datePattern: String): CreateEvent =
        form.toCreateEvent(
            datePattern = datePattern,
            isEdition = isEdition,
            editedRecurrence = editedEvent?.let { it.recurrence ?: Recurrence.NO_RECURRENCE.value }
        )

    fun saveDraft(context: Context, userId: Int) {
        if (isEdition) return
        CreateEventDraftStore.save(context, userId, form)
    }

    fun deleteDraft(context: Context, userId: Int) {
        CreateEventDraftStore.delete(context, userId)
    }

    companion object {
        private val gson = Gson()
    }
}
