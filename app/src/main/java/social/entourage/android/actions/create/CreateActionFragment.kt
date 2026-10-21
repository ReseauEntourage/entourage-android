package social.entourage.android.actions.create

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import social.entourage.android.BuildConfig
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.actions.ActionsPresenter
import social.entourage.android.api.model.Action
import social.entourage.android.base.ChoosePhotoModalFragment
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.choosePhoto.ChooseGalleryPhotoModalFragment
import social.entourage.android.home.pedago.PedagoDetailActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.parcelableCompat
import timber.log.Timber

/**
 * Conteneur de l'assistant de création / d'édition d'une entraide (contribution ou demande).
 * L'interface (barre du haut, progression, « Étape N sur X », étapes, aperçu, pied de page) est en
 * Compose ([CreateActionScreen]) ; ce fragment garde ce qui dépend du système : sélecteur de photo,
 * navigation vers le sélecteur d'adresse, appels réseau et sortie.
 */
class CreateActionFragment : Fragment() {

    private val viewModel: CreateActionViewModel by activityViewModels()

    private lateinit var uiState: CreateActionUiState

    private var composeView: ComposeView? = null

    /** Page affichée : étape (0 à N-1) ou aperçu (N). */
    private var page by mutableIntStateOf(0)

    private val actionPresenter: ActionsPresenter by lazy { ActionsPresenter() }
    private lateinit var groupPresenter: GroupPresenter

    private var isDemand = false
    private var actionEdited: Action? = null
    private var isAlreadySend = false

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (composeView != null && page > 0) {
                goToPage(page - 1)
            } else {
                confirmExit()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isDemand = CreateActionFragmentArgs.fromBundle(it).isActionDemand
            actionEdited = CreateActionFragmentArgs.fromBundle(it).actionObj
        }
        viewModel.initialize(isDemand, actionEdited)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        uiState = CreateActionUiState(viewModel)
        val view = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CreateActionScreen(
                    state = uiState,
                    page = page,
                    actions = CreateActionActions(
                        onClose = ::confirmExit,
                        onPrevious = { if (page > 0) goToPage(page - 1) },
                        onNext = ::onNextClicked,
                        onChoosePhoto = ::openPhotoChooser,
                        onPickAddress = ::openAddressPicker,
                        onOpenAboutCategories = ::openAboutCategories,
                    )
                )
            }
        }
        composeView = view
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // La vue est recréée au retour de la sélection d'adresse : on reprend là où on était.
        page = viewModel.currentPage.value ?: 0
        logStepViewed(page)

        childFragmentManager.setFragmentResultListener(
            Const.REQUEST_KEY_CHOOSE_PHOTO, viewLifecycleOwner
        ) { _, bundle -> onPhotoChosen(bundle) }
        actionPresenter.isActionUpdated.observe(viewLifecycleOwner, ::isActionUpdated)
        actionPresenter.newActionCreated.observe(viewLifecycleOwner, ::handleCreateActionResponse)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        groupPresenter = ViewModelProvider(requireActivity())[GroupPresenter::class.java]
        groupPresenter.getGroup.observe(viewLifecycleOwner) { group ->
            if (group != null) onDefaultGroupLoaded(group.name)
        }
        groupPresenter.getDefaultGroup()
    }

    /**
     * Le groupe par défaut arrive : l'étape de partage entre dans la liste. Si l'utilisateur est déjà
     * sur l'aperçu, on le ramène sur cette étape pour qu'il fasse son choix (création uniquement).
     */
    private fun onDefaultGroupLoaded(name: String?) {
        val wasPreview = page >= viewModel.previewPageIndex
        viewModel.onDefaultGroupLoaded(name)
        if (wasPreview) {
            val sharingIndex = viewModel.steps.indexOf(CreateActionStep.SHARING)
            page = if (sharingIndex >= 0 && !viewModel.isEdition) sharingIndex else viewModel.previewPageIndex
            viewModel.setCurrentPage(page)
        }
        uiState.refresh()
    }

    // --- Pages ------------------------------------------------------------------------------------

    private fun goToPage(newPage: Int) {
        hideKeyboard()
        page = newPage
        viewModel.setCurrentPage(newPage)
        logStepViewed(newPage)
    }

    /** Trace analytique de l'étape affichée (création uniquement, mêmes événements qu'avant). */
    private fun logStepViewed(index: Int) {
        if (viewModel.isEdition) return
        val step = viewModel.steps.getOrNull(index) ?: return
        val event = when (step) {
            CreateActionStep.DESCRIPTION ->
                if (isDemand) AnalyticsEvents.Help_create_demand_1 else AnalyticsEvents.Help_create_contrib_1
            CreateActionStep.CATEGORY ->
                if (isDemand) AnalyticsEvents.Help_create_demand_2 else AnalyticsEvents.Help_create_contrib_2
            CreateActionStep.LOCATION ->
                if (isDemand) AnalyticsEvents.Help_create_demand_3 else AnalyticsEvents.Help_create_contrib_3
            CreateActionStep.SHARING -> return
        }
        AnalyticsEvents.logEvent(event)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(composeView?.windowToken, 0)
    }

    private fun onNextClicked() {
        val current = page
        val steps = viewModel.steps
        if (current < steps.size) {
            if (!viewModel.validateStep(steps[current])) return
            goToPage(current + 1)
        } else {
            submit()
        }
    }

    // --- Photo, adresse, page pédagogique ---------------------------------------------------------

    private fun openPhotoChooser() {
        ChoosePhotoModalFragment.newInstance()
            .show(childFragmentManager, ChooseGalleryPhotoModalFragment.TAG)
    }

    private fun onPhotoChosen(bundle: Bundle) {
        viewModel.form.imageUri = bundle.parcelableCompat<Uri>(Const.CHOOSE_PHOTO)
        viewModel.onFormChanged()
        uiState.refresh()
    }

    private fun openAddressPicker() {
        findNavController().navigate(R.id.action_create_action_fragment_to_edit_action_place_fragment)
    }

    private fun openAboutCategories() {
        val context = requireContext()
        val intent = Intent(context, PedagoDetailActivity::class.java)
        PedagoDetailActivity.setPedagoId(BuildConfig.PEDAGO_ACTION_SECTION_ID)
        intent.putExtra(Const.ID, BuildConfig.PEDAGO_ACTION_SECTION_ID)
        context.startActivity(intent)
    }

    // --- Envoi ----------------------------------------------------------------------------------

    private fun submit() {
        if (isAlreadySend) return
        isAlreadySend = true
        val form = viewModel.form
        val autoPost = viewModel.autoPost()
        val edited = viewModel.editedAction
        if (edited != null) {
            actionPresenter.updateAction(
                edited, form.toUpdateAction(edited, autoPost), isDemand, form.imageUri, requireContext(), autoPost
            )
        } else {
            actionPresenter.createAction(
                form.toCreateAction(autoPost), isDemand, form.imageUri, requireContext(), autoPost
            )
        }
    }

    private fun actionName() =
        if (isDemand) getString(R.string.action_name_demand) else getString(R.string.action_name_contrib)

    private fun handleCreateActionResponse(actionCreated: Action?) {
        if (actionCreated == null) {
            isAlreadySend = false
            Utils.showToast(requireContext(), getString(R.string.action_error_create_action, actionName()))
        } else {
            actionPresenter.newActionCreated.value?.let {
                if (actionEdited == null) {
                    AnalyticsEvents.logEvent(
                        if (isDemand) AnalyticsEvents.Help_create_demand_end
                        else AnalyticsEvents.Help_create_contrib_end
                    )
                }
                try {
                    val action =
                        CreateActionFragmentDirections.actionCreateActionFragmentToCreateActionSuccessFragment(
                            it.id!!, it.title!!, it.isDemand()
                        )
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }

    private fun isActionUpdated(updated: Boolean) {
        if (updated) {
            Utils.showToast(requireContext(), getString(R.string.action_updated))
            activity?.finish()
            RefreshController.shouldRefreshEventFragment = true
        } else {
            isAlreadySend = false
            Utils.showToast(requireContext(), getString(R.string.action_error_edit_action, actionName()))
        }
    }

    // --- Sortie ---------------------------------------------------------------------------------

    /** Quitter sans enregistrer : une confirmation est demandée dès que des données ont été saisies. */
    private fun confirmExit() {
        if (!viewModel.isDirty()) {
            requireActivity().finish()
            return
        }
        CustomAlertDialog.showWithCancelFirst(
            requireContext(),
            getString(R.string.back_create_action_title),
            getString(R.string.back_create_action_content, actionName()),
            getString(R.string.exit),
            onNo = {}
        ) {
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        composeView = null
    }
}
