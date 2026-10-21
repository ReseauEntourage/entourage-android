package social.entourage.android.groups.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.Image
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.choosePhoto.ChooseGalleryPhotoModalFragment
import social.entourage.android.groups.choosePhoto.ImagesType
import social.entourage.android.groups.details.feed.CreatePostGroupActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.parcelableCompat

/**
 * Conteneur de l'assistant de création de groupe. L'interface (barre du haut, progression,
 * « Étape N sur X », étapes, aperçu, pied de page Retour / Continuer) est en Compose
 * ([CreateGroupScreen]) ; ce fragment garde ce qui dépend du système : sélecteur de photo,
 * navigation vers le sélecteur d'adresse, appels réseau et sortie.
 *
 * La modification d'un groupe ([social.entourage.android.groups.edit.EditGroupFragment]) réutilise
 * le même assistant : elle ne fait que fournir l'identifiant du groupe à modifier.
 */
open class CreateGroupFragment : Fragment() {

    private val viewModel: CreateGroupViewModel by activityViewModels()

    private lateinit var uiState: CreateGroupUiState

    private var composeView: ComposeView? = null

    /** Page affichée : étape (0 à N-1) ou aperçu (N). */
    private var page by mutableIntStateOf(0)

    /** Faux tant que le groupe à modifier n'est pas chargé. */
    private var ready by mutableStateOf(false)

    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

    private var isAlreadySend = false

    /** Identifiant du groupe à modifier, ou null en création. */
    protected open val editedGroupId: Int? get() = null

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (composeView != null && page > 0) {
                goToPage(page - 1)
            } else {
                confirmExit()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        uiState = CreateGroupUiState(viewModel)
        val view = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                if (ready) {
                    CreateGroupScreen(
                        state = uiState,
                        page = page,
                        actions = CreateGroupActions(
                            onClose = ::onCloseClicked,
                            onPrevious = ::onPreviousClicked,
                            onNext = ::onNextClicked,
                            onChoosePhoto = ::openPhotoChooser,
                            onPickAddress = ::openAddressPicker,
                        )
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(Color.White))
                }
            }
        }
        composeView = view
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.setFragmentResultListener(
            Const.REQUEST_KEY_CHOOSE_PHOTO, viewLifecycleOwner
        ) { _, bundle -> onPhotoChosen(bundle) }
        groupPresenter.newGroupCreated.observe(viewLifecycleOwner, ::handleCreateGroupResponse)
        groupPresenter.isGroupUpdated.observe(viewLifecycleOwner, ::handleUpdateGroupResponse)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        val groupId = editedGroupId
        if (groupId == null) {
            viewModel.initialize(null)
            showAssistant()
        } else if (viewModel.isInitialized) {
            showAssistant()
        } else {
            AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_GROUP_OPTION_EDITION)
            groupPresenter.getGroup.observe(viewLifecycleOwner) { group ->
                group?.let {
                    viewModel.initialize(it)
                    showAssistant()
                }
            }
            groupPresenter.getGroup(groupId)
        }
    }

    private fun showAssistant() {
        // La vue est recréée au retour de la sélection d'adresse : on reprend là où on était.
        page = viewModel.currentPage.value ?: 0
        ready = true
        logStepViewed(page)
    }

    // --- Pages ------------------------------------------------------------------------------------

    private fun goToPage(newPage: Int) {
        hideKeyboard()
        page = newPage
        viewModel.setCurrentPage(newPage)
        logStepViewed(newPage)
    }

    /** Trace analytique de l'étape affichée (création uniquement). */
    private fun logStepViewed(step: Int) {
        if (viewModel.isEdition) return
        when (step) {
            0 -> AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_NEW_GROUP_STEP1)
            1 -> AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_NEW_GROUP_STEP2)
            2 -> AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_NEW_GROUP_STEP3)
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(composeView?.windowToken, 0)
    }

    private fun onCloseClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEW_GROUP_BACK_ARROW)
        confirmExit()
    }

    private fun onPreviousClicked() {
        if (page > 0) {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEW_GROUP_PREVIOUS)
            goToPage(page - 1)
        }
    }

    private fun onNextClicked() {
        val current = page
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEW_GROUP_NEXT)
        if (current < viewModel.steps.size) {
            if (!viewModel.validateStep(viewModel.steps[current])) return
            goToPage(current + 1)
        } else {
            submit()
        }
    }

    // --- Photo et adresse -------------------------------------------------------------------------

    private fun openPhotoChooser() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEW_GROUP_STEP3_ADD_PICTURE)
        ChooseGalleryPhotoModalFragment.newInstance(ImagesType.GROUPS)
            .show(childFragmentManager, ChooseGalleryPhotoModalFragment.TAG)
    }

    private fun onPhotoChosen(bundle: Bundle) {
        bundle.parcelableCompat<Image>(Const.CHOOSE_PHOTO_PATH)?.let { image ->
            with(viewModel.form) {
                imageId = image.id
                photoUrl = image.imageUrl ?: image.landscapeUrl ?: image.portraitUrl
            }
            viewModel.onFormChanged()
            uiState.refresh()
        }
    }

    private fun openAddressPicker() {
        findNavController().navigate(R.id.action_create_group_fragment_to_edit_action_zone_fragment)
    }

    // --- Envoi ----------------------------------------------------------------------------------

    private fun submit() {
        if (isAlreadySend) return
        isAlreadySend = true
        val edited = viewModel.editedGroup
        if (edited != null) {
            edited.id?.let { groupPresenter.updateGroup(it, viewModel.form.toUpdateBody()) }
        } else {
            groupPresenter.createGroup(viewModel.form.toGroup())
        }
    }

    private fun handleCreateGroupResponse(groupCreated: Group?) {
        if (groupCreated == null) {
            isAlreadySend = false
            Utils.showToast(requireContext(), getString(R.string.error_create_group))
        } else {
            groupPresenter.newGroupCreated.value?.id?.let {
                CreatePostGroupActivity.idGroupForPost = it
                val action =
                    CreateGroupFragmentDirections.actionCreateGroupFragmentToCreateGroupSuccessFragment(it)
                findNavController().navigate(action)
            }
        }
    }

    private fun handleUpdateGroupResponse(isGroupUpdated: Boolean) {
        if (isGroupUpdated) {
            Utils.showToast(requireContext(), getString(R.string.group_updated))
            requireActivity().finish()
            RefreshController.shouldRefreshFragment = true
        } else {
            isAlreadySend = false
            Utils.showToast(requireContext(), getString(R.string.group_error_updated))
        }
    }

    // --- Sortie ---------------------------------------------------------------------------------

    /** Quitter sans enregistrer : les modifications sont perdues (création comme édition). */
    private fun confirmExit() {
        if (!ready || !viewModel.isDirty()) {
            requireActivity().finish()
            return
        }
        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_NEW_GROUP_CANCEL_POP)
        CustomAlertDialog.showWithCancelFirst(
            requireContext(),
            getString(R.string.back_create_group_title),
            getString(if (viewModel.isEdition) R.string.back_edit_group_content else R.string.back_create_group_content),
            getString(R.string.exit),
            onNo = {
                AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEW_GROUP_CANCEL_POP_CANCEL)
            }
        ) {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEW_GROUP_CANCEL_POP_LEAVE)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        composeView = null
    }
}
