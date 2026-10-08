package social.entourage.android.events.create

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.yalantis.ucrop.UCrop
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.Image
import social.entourage.android.events.EventsPresenter
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.choosePhoto.ChooseGalleryPhotoModalFragment
import social.entourage.android.groups.choosePhoto.ImagesType
import social.entourage.android.groups.list.groupPerPage
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.parcelableCompat
import social.entourage.android.tools.utils.serializableExtra
import timber.log.Timber
import java.io.File

/**
 * Conteneur de l'assistant de création / d'édition. L'interface (barre du haut, progression,
 * libellé « Étape N sur X », étapes, aperçu et pied de page Retour / Continuer) est entièrement en
 * Compose ([CreateEventScreen]) ; ce fragment garde ce qui dépend du système : sélecteur de photo et
 * recadrage, navigation vers le sélecteur d'adresse, appels réseau, brouillon et sortie.
 * Les étapes partagent le [CreateEventViewModel] de l'activité.
 */
class CreateEventFragment : Fragment() {

    private val viewModel: CreateEventViewModel by activityViewModels()

    private lateinit var uiState: CreateEventUiState

    private var composeView: ComposeView? = null

    /** Page affichée : étape (0 à N-1) ou aperçu (N). */
    private var page by mutableIntStateOf(0)

    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }

    private var isAlreadySend = false

    // --- Groupes de l'étape 5 (chargés par pages, conservés entre deux affichages) ---------------
    private val groupsList = mutableStateListOf<Group>()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private var groupsPage = 0

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (composeView != null && page > 0) {
                goToPage(page - 1)
            } else {
                confirmExit()
            }
        }
    }

    // --- Photo : recadrage puis envoi -------------------------------------------------------------

    private val cropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                UCrop.getOutput(data)?.path?.let { path ->
                    viewModel.uploadLocalPhoto(File(path), requireContext().cacheDir)
                    uiState.refresh()
                    Utils.showToast(requireContext(), getString(R.string.create_event_photo_uploading_wait))
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR && data != null) {
                UCrop.getError(data)?.printStackTrace()
            }
        }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Nom de fichier unique : empêche Glide de resservir l'ancienne image depuis son cache mémoire.
                val uniqueFileName = "cropped_event_image_${System.currentTimeMillis()}.jpg"
                val destinationUri = Uri.fromFile(File(requireContext().cacheDir, uniqueFileName))

                val options = UCrop.Options()
                options.setToolbarTitle(getString(R.string.group_choose_photo))
                options.setCircleDimmedLayer(false)
                options.setHideBottomControls(true)
                options.setFreeStyleCropEnabled(false)

                val intent = UCrop.of(it, destinationUri)
                    .withAspectRatio(16f, 9f)
                    .withOptions(options)
                    .getIntent(requireContext())
                cropLauncher.launch(intent)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        uiState = CreateEventUiState(viewModel)
        val view = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CreateEventScreen(
                    state = uiState,
                    page = page,
                    groups = groupsList,
                    actions = CreateEventActions(
                        onClose = ::confirmExit,
                        onSaveDraft = ::saveDraftAndExit,
                        onPrevious = { if (page > 0) goToPage(page - 1) },
                        onNext = ::onNextClicked,
                        onChoosePhoto = ::openPhotoChooser,
                        onPickAddress = ::openAddressPicker,
                        onGroupsShown = ::loadGroupsIfNeeded,
                        onLoadMoreGroups = ::loadMoreGroups,
                    )
                )
            }
        }
        composeView = view
        // Marges système (barre d'état, encoche, barre de navigation) gérées par CreateEventScreen.
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViewModel(savedInstanceState == null)
        // La vue est recréée au retour de la sélection d'adresse : on reprend là où on était.
        page = viewModel.currentPage.value ?: 0
        logStepViewed(page)
        childFragmentManager.setFragmentResultListener(
            Const.REQUEST_KEY_CHOOSE_PHOTO, viewLifecycleOwner
        ) { _, bundle -> onPhotoChosen(bundle) }
        groupPresenter.getAllMyGroups.observe(viewLifecycleOwner, ::handleResponseGetGroups)
        eventPresenter.newEventCreated.observe(viewLifecycleOwner, ::handleCreateEventResponse)
        eventPresenter.isEventUpdated.observe(viewLifecycleOwner, ::isEventUpdated)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    private fun initializeViewModel(announceDraft: Boolean) {
        val activity = requireActivity()
        val edited = activity.intent?.serializableExtra<Events>(Const.EVENT_UI)
        // Pas de brouillon en édition : il n'est ni lu ni écrit.
        // Les brouillons sont désactivés côté utilisateur : rien n'est restauré (le code reste en place).
        val draft: CreateEventForm? = null
        val groupId = activity.intent?.getIntExtra(Const.GROUP_ID, Const.DEFAULT_VALUE)
            ?.takeIf { it != Const.DEFAULT_VALUE }
        viewModel.initialize(edited, draft, groupId)
        if (announceDraft && viewModel.draftRestored) {
            Utils.showToast(requireContext(), getString(R.string.create_event_draft_restored))
        }
    }

    // --- Pages ------------------------------------------------------------------------------------

    private fun showPage(newPage: Int) {
        page = newPage
        viewModel.setCurrentPage(newPage)
        logStepViewed(newPage)
    }

    /** Trace analytique de l'étape affichée (création uniquement). */
    private fun logStepViewed(step: Int) {
        if (viewModel.isEdition) return
        when (step) {
            0 -> AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_1)
            1 -> AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_2)
            2 -> AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_3)
            3 -> AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_4)
            4 -> AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_5)
        }
    }

    private fun goToPage(newPage: Int) {
        hideKeyboard()
        showPage(newPage)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(composeView?.windowToken, 0)
    }

    // --- Photo, adresse, groupes -----------------------------------------------------------------

    private fun openPhotoChooser() {
        ChooseGalleryPhotoModalFragment.newInstance(ImagesType.EVENTS)
            .show(childFragmentManager, ChooseGalleryPhotoModalFragment.TAG)
    }

    private fun onPhotoChosen(bundle: Bundle) {
        if (bundle.getBoolean("is_add_photo", false)) {
            getContent.launch("image/*")
        } else {
            bundle.parcelableCompat<Image>(Const.CHOOSE_PHOTO_PATH)?.let {
                viewModel.selectCatalogImage(it)
                uiState.refresh()
            }
        }
    }

    private fun openAddressPicker() {
        findNavController().navigate(R.id.action_create_event_fragment_to_edit_action_zone_fragment)
    }

    private fun loadGroupsIfNeeded() {
        if (groupsPage == 0) loadGroups()
    }

    private fun loadGroups() {
        groupsPage++
        EntourageApplication.me(activity)?.id?.let { groupPresenter.getMyGroups(groupsPage, groupPerPage, it) }
    }

    /** Charge la page suivante quand on atteint le bas de la liste (même règle qu'avant). */
    private fun loadMoreGroups() {
        if (!groupPresenter.isLoading && !groupPresenter.isLastPage && groupsList.size >= groupPerPage) {
            loadGroups()
        }
    }

    private fun handleResponseGetGroups(allGroups: MutableList<Group>?) {
        // La vue peut être recréée (retour du sélecteur d'adresse) : on ne ré-ajoute pas deux fois un groupe.
        allGroups?.filter { new -> groupsList.none { it.id == new.id } }?.let { groupsList.addAll(it) }
    }

    private fun onNextClicked() {
        val page = page
        if (page < viewModel.steps.size) {
            if (!viewModel.validateStep(viewModel.steps[page])) return
            val nextIsPreview = page + 1 == viewModel.previewPageIndex
            if (nextIsPreview && viewModel.isUploading.value == true) {
                // L'aperçu ne s'ouvre pas tant que la photo n'est pas envoyée.
                Utils.showToast(requireContext(), getString(R.string.create_event_photo_uploading_wait))
                return
            }
            goToPage(page + 1)
        } else {
            publish()
        }
    }

    // --- Envoi ----------------------------------------------------------------------------

    private fun publish() {
        if (viewModel.isUploading.value == true) {
            Utils.showToast(requireContext(), getString(R.string.create_event_photo_uploading_wait))
            return
        }
        val edited = viewModel.editedEvent
        if (edited != null) {
            if (edited.recurrence != null) {
                showAlertDialogUpdateEventWithRecurrence()
            } else {
                updateEventWithoutRecurrence()
            }
        } else {
            if (isAlreadySend) return
            isAlreadySend = true
            eventPresenter.createEvent(requestBody())
        }
    }

    private fun requestBody(): CreateEvent =
        viewModel.buildRequestBody(getString(R.string.event_date_formatter_to_string))

    private fun updateEventWithRecurrence() {
        if (isAlreadySend) return
        isAlreadySend = true
        viewModel.editedEvent?.id?.let {
            eventPresenter.updateEventSiblings(it, requestBody())
        }
    }

    private fun updateEventWithoutRecurrence() {
        if (isAlreadySend) return
        isAlreadySend = true
        viewModel.editedEvent?.id?.let {
            eventPresenter.updateEvent(it, requestBody())
        }
    }

    private fun showAlertDialogUpdateEventWithRecurrence() {
        val layoutInflater = LayoutInflater.from(requireContext())
        val customDialog: View =
            layoutInflater.inflate(R.layout.layout_custom_alert_dialog_cancel_event, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(customDialog)
        val alertDialog = builder.create()
        val cancelOneEvent = customDialog.findViewById<RadioButton>(R.id.one_event)
        val cancelAllEvents =
            customDialog.findViewById<RadioButton>(R.id.all_events_recurrent)
        val radioGroup = customDialog.findViewById<android.widget.RadioGroup>(R.id.recurrence)

        val btnYes = customDialog.findViewById<Button>(R.id.yes)
        with(btnYes) {
            this.background = requireContext().getDrawable(R.drawable.btn_shape_light_orange)
            text = getString(R.string.validate)
            isEnabled = false

            setOnClickListener {
                if (cancelOneEvent.isChecked) updateEventWithoutRecurrence()
                if (cancelAllEvents.isChecked) updateEventWithRecurrence()
                alertDialog.dismiss()
                activity?.finish()
            }
        }

        radioGroup.setOnCheckedChangeListener { _, _ ->
            btnYes.isEnabled = true
            btnYes.background = requireContext().getDrawable(R.drawable.btn_shape_orange_alert_dialog)
        }
        customDialog.findViewById<TextView>(R.id.title).text =
            getString(R.string.event_edit_recurrent_event)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

    private fun isEventUpdated(updated: Boolean) {
        if (updated) {
            Utils.showToast(requireContext(), getString(R.string.group_updated))
            activity?.finish()
            RefreshController.shouldRefreshEventFragment = true
        } else {
            isAlreadySend = false
            Utils.showToast(requireContext(), getString(R.string.group_error_updated))
        }
    }

    private fun handleCreateEventResponse(eventCreated: Events?) {
        if (eventCreated == null) {
            isAlreadySend = false
            Utils.showToast(requireContext(), getString(R.string.error_create_group))
        } else {
            eventPresenter.newEventCreated.value?.id?.let { eventID ->
                if (!viewModel.isEdition) {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_end)
                    // Publication réussie : le brouillon n'a plus de raison d'être.
                    EntourageApplication.me(requireContext())?.id?.let {
                        viewModel.deleteDraft(requireContext(), it)
                    }
                }
                val action =
                    CreateEventFragmentDirections.actionCreateEventFragmentToCreateEventSuccessFragment(
                        eventID
                    )
                try {
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    Utils.showToast(requireContext(), getString(R.string.error_create_group))
                    Timber.e(e, "Navigation error in CreateEventFragment")
                }
            }
        }
    }

    // --- Sortie ---------------------------------------------------------------------------

    /** Quitter sans enregistrer : les modifications sont perdues (création comme édition). */
    private fun confirmExit() {
        if (!viewModel.isDirty()) {
            requireActivity().finish()
            return
        }
        CustomAlertDialog.showWithCancelFirst(
            requireContext(),
            getString(R.string.back_create_group_title),
            getString(R.string.back_create_event_content),
            getString(R.string.exit)
        ) {
            requireActivity().finish()
        }
    }

    private fun saveDraftAndExit() {
        if (viewModel.isEdition) return
        val userId = EntourageApplication.me(requireContext())?.id ?: return
        viewModel.saveDraft(requireContext(), userId)
        Utils.showToast(requireContext(), getString(R.string.create_event_draft_saved))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        composeView = null
    }
}
