package social.entourage.android.actions.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import social.entourage.android.api.model.Action

/**
 * État partagé de l'assistant de création / d'édition d'entraide, porté par l'activité. Il remplace
 * l'ancien `CommunicationActionHandlerViewModel` :
 *
 * - [form] contient tout le formulaire ;
 * - [steps] est la liste des étapes, recalculée à chaque lecture : l'étape de partage s'ajoute ou se
 *   retire selon la catégorie choisie et l'existence d'un groupe par défaut (voir [hasSharingStep]) ;
 * - [errors] contient les messages affichés sous les champs, pour les étapes sur lesquelles
 *   l'utilisateur a déjà appuyé sur « Continuer » (puis ils suivent les modifications).
 */
class CreateActionViewModel : ViewModel() {

    var isDemand = false
        private set

    var editedAction: Action? = null
        private set
    val isEdition: Boolean get() = editedAction != null

    var form: CreateActionForm = CreateActionForm()
        private set

    var isInitialized = false
        private set

    /** Vrai quand le groupe par défaut est connu (condition de l'étape de partage). */
    var hasDefaultGroup = false
        private set
    var defaultGroupName: String? = null
        private set

    private var initialSnapshot: CreateActionForm = form.snapshot()

    private val _currentPage = MutableLiveData(0)
    val currentPage: LiveData<Int> = _currentPage

    private val _errors = MutableLiveData<Map<CreateActionField, Int>>(emptyMap())
    val errors: LiveData<Map<CreateActionField, Int>> = _errors

    private val flaggedSteps = mutableSetOf<CreateActionStep>()

    val steps: List<CreateActionStep>
        get() = buildList {
            add(CreateActionStep.CATEGORY)
            add(CreateActionStep.DESCRIPTION)
            add(CreateActionStep.LOCATION)
            if (hasSharingStep()) add(CreateActionStep.SHARING)
        }

    /** Index de l'aperçu : la page qui suit la dernière étape. */
    val previewPageIndex: Int get() = steps.size

    /**
     * L'étape de partage n'existe que pour la catégorie « temps de partage » (`social`) et quand le
     * groupe par défaut est connu : c'est le comportement des deux apps avant la refonte (4 pages
     * pour `social`, 3 sinon).
     */
    private fun hasSharingStep() = hasDefaultGroup && form.isSharingTime()

    /** À appeler une seule fois : [edited] est l'entraide à modifier, ou null en création. */
    fun initialize(demand: Boolean, edited: Action?) {
        if (isInitialized) return
        isInitialized = true
        isDemand = demand
        editedAction = edited
        if (edited != null) form = CreateActionForm.from(edited)
        initialSnapshot = form.snapshot()
    }

    /** Groupe par défaut chargé : l'étape de partage peut être proposée. */
    fun onDefaultGroupLoaded(name: String?) {
        hasDefaultGroup = true
        defaultGroupName = name
    }

    fun setCurrentPage(page: Int) {
        _currentPage.value = page
    }

    /** Valeur de `auto_post_at_create` : oui seulement si l'étape de partage existe et a reçu « Oui ». */
    fun autoPost(): Boolean = hasSharingStep() && form.shareInGroup == true

    /** Valide une étape ; en cas d'échec, les messages sont publiés dans [errors]. */
    fun validateStep(step: CreateActionStep): Boolean {
        flaggedSteps.add(step)
        publishErrors()
        return validate().keys.none { it.step == step }
    }

    /** À appeler à chaque modification d'un champ : met à jour les messages déjà affichés. */
    fun onFormChanged() {
        if (flaggedSteps.isNotEmpty()) publishErrors()
    }

    private fun validate() = form.validate(isEdition, hasSharingStep())

    private fun publishErrors() {
        _errors.value = validate().filterKeys { it.step in flaggedSteps }
    }

    /** Vrai si le formulaire a été modifié depuis l'ouverture (sert à confirmer la sortie). */
    fun isDirty(): Boolean = form != initialSnapshot
}
