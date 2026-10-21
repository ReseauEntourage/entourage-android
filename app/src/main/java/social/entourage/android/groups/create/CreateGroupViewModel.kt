package social.entourage.android.groups.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import social.entourage.android.api.model.Group

/**
 * État partagé de l'assistant de création / d'édition de groupe, porté par l'activité. Il remplace
 * l'ancien `CommunicationHandlerViewModel` :
 *
 * - [form] contient tout le formulaire ;
 * - [steps] est la liste unique des étapes dont tout le reste est dérivé ;
 * - [errors] contient les messages affichés sous les champs, pour les étapes sur lesquelles
 *   l'utilisateur a déjà appuyé sur « Continuer » (puis ils suivent les modifications).
 */
class CreateGroupViewModel : ViewModel() {

    val steps: List<CreateGroupStep> = CreateGroupStep.values().toList()

    val previewPageIndex: Int get() = steps.size

    var editedGroup: Group? = null
        private set
    val isEdition: Boolean get() = editedGroup != null

    var form: CreateGroupForm = CreateGroupForm()
        private set

    var isInitialized = false
        private set

    private var initialSnapshot: CreateGroupForm = form.snapshot()

    private val _currentPage = MutableLiveData(0)
    val currentPage: LiveData<Int> = _currentPage

    private val _errors = MutableLiveData<Map<CreateGroupField, Int>>(emptyMap())
    val errors: LiveData<Map<CreateGroupField, Int>> = _errors

    private val flaggedSteps = mutableSetOf<CreateGroupStep>()

    /** À appeler une seule fois : [edited] est le groupe à modifier, ou null en création. */
    fun initialize(edited: Group?) {
        if (isInitialized) return
        isInitialized = true
        editedGroup = edited
        if (edited != null) form = CreateGroupForm.from(edited)
        initialSnapshot = form.snapshot()
    }

    fun setCurrentPage(page: Int) {
        _currentPage.value = page
    }

    /** Valide une étape ; en cas d'échec, les messages sont publiés dans [errors]. */
    fun validateStep(step: CreateGroupStep): Boolean {
        flaggedSteps.add(step)
        publishErrors()
        return form.validate(isEdition).keys.none { it.step == step }
    }

    /** À appeler à chaque modification d'un champ : met à jour les messages déjà affichés. */
    fun onFormChanged() {
        if (flaggedSteps.isNotEmpty()) publishErrors()
    }

    private fun publishErrors() {
        _errors.value = form.validate(isEdition).filterKeys { it.step in flaggedSteps }
    }

    /** Vrai si le formulaire a été modifié depuis l'ouverture (sert à confirmer la sortie). */
    fun isDirty(): Boolean = form != initialSnapshot
}
